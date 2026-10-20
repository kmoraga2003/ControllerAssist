package com.controllerassist.service;

import com.controllerassist.dto.AttendanceResponseDTO;
import com.controllerassist.dto.DashboardSummaryDTO;
import com.controllerassist.dto.PunchRequestDTO;
import com.controllerassist.dto.ReportItemDTO;
import com.controllerassist.exception.BadRequestException;
import com.controllerassist.exception.ResourceNotFoundException;
import com.controllerassist.model.AttendanceRecord;
import com.controllerassist.model.AttendanceStatus;
import com.controllerassist.model.Employee;
import com.controllerassist.model.PunchType;
import com.controllerassist.repository.AttendanceRepository;
import com.controllerassist.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    // Reglas de negocio del caso:
    // RE-01: post 9:30 am es atraso
    // RE-02: antes de las 17:30 pm es salida anticipada
    private final LocalTime expectedArrivalTime;
    private final LocalTime expectedDepartureTime;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            EmployeeRepository employeeRepository,
            @Value("${attendance.arrival-threshold:09:30:00}") String arrivalThreshold,
            @Value("${attendance.departure-threshold:17:30:00}") String departureThreshold) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.expectedArrivalTime = LocalTime.parse(arrivalThreshold);
        this.expectedDepartureTime = LocalTime.parse(departureThreshold);
    }

    /**
     * Marcación inteligente (Punch): detecta Entrada o Salida según estado del día
     */
    public AttendanceResponseDTO punch(PunchRequestDTO request) {
        Employee employee = employeeRepository.findByCode(request.getEmployeeCode().trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún empleado con el código: " + request.getEmployeeCode()));

        if (!employee.isActive()) {
            throw new BadRequestException("El empleado " + employee.getFullName() + " está inactivo en el sistema.");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        Optional<AttendanceRecord> existingOpt = attendanceRepository.findByEmployeeAndAttendanceDate(employee, today);

        if (existingOpt.isEmpty()) {
            // No tiene entrada hoy: registrar Entrada
            if (request.getType() == PunchType.CHECK_OUT) {
                throw new BadRequestException("No es posible registrar salida sin haber registrado entrada primero.");
            }

            // RE-01: post 09:30:00 es atraso
            boolean isLate = now.toLocalTime().isAfter(expectedArrivalTime);
            AttendanceStatus status = isLate ? AttendanceStatus.LATE : AttendanceStatus.ON_TIME;

            AttendanceRecord record = new AttendanceRecord();
            record.setEmployee(employee);
            record.setAttendanceDate(today);
            record.setCheckInTime(now);
            record.setStatus(status);
            record.setNotes(request.getNotes());

            AttendanceRecord saved = attendanceRepository.save(record);
            AttendanceResponseDTO response = AttendanceResponseDTO.fromEntity(saved);
            response.setMessage("ENTRADA registrada a las " + now.format(timeFormatter) +
                    (isLate ? " (Atraso: post 09:30)" : " (A tiempo)"));
            return response;

        } else {
            // Ya tiene registro hoy
            AttendanceRecord record = existingOpt.get();

            if (record.getCheckOutTime() == null) {
                // Registrar Salida
                if (request.getType() == PunchType.CHECK_IN) {
                    throw new BadRequestException("Ya registraste tu ENTRADA hoy a las " +
                            record.getCheckInTime().format(timeFormatter) + ". Ahora corresponde registrar SALIDA.");
                }

                record.setCheckOutTime(now);

                // RE-02: antes de las 17:30 es salida anticipada
                boolean isEarlyExit = now.toLocalTime().isBefore(expectedDepartureTime);
                if (isEarlyExit) {
                    record.setStatus(AttendanceStatus.EARLY_EXIT);
                }

                if (request.getNotes() != null && !request.getNotes().isBlank()) {
                    String prev = record.getNotes() != null ? record.getNotes() + " | " : "";
                    record.setNotes(prev + request.getNotes());
                }

                AttendanceRecord saved = attendanceRepository.save(record);
                AttendanceResponseDTO response = AttendanceResponseDTO.fromEntity(saved);
                response.setMessage("SALIDA registrada a las " + now.format(timeFormatter) +
                        (isEarlyExit ? " (Salida anticipada: antes de 17:30)" : ""));
                return response;
            } else {
                throw new BadRequestException("Jornada completada para hoy (Entrada: " +
                        record.getCheckInTime().format(timeFormatter) +
                        ", Salida: " + record.getCheckOutTime().format(timeFormatter) + ").");
            }
        }
    }

    /**
     * Obtener el estado de hoy para un empleado específico (para su vista de usuario)
     */
    @Transactional(readOnly = true)
    public Optional<AttendanceResponseDTO> getTodayAttendanceForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) return Optional.empty();

        return attendanceRepository.findByEmployeeAndAttendanceDate(employee, LocalDate.now())
                .map(AttendanceResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponseDTO> getTodayAttendances() {
        return attendanceRepository.findByAttendanceDateOrderByCheckInTimeDesc(LocalDate.now())
                .stream()
                .map(AttendanceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponseDTO> filterAttendances(Long employeeId, LocalDate startDate, LocalDate endDate, AttendanceStatus status) {
        return attendanceRepository.filterAttendances(employeeId, startDate, endDate, status)
                .stream()
                .map(AttendanceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * RE-01: Reporte de Atrasos (post 09:30 AM)
     */
    @Transactional(readOnly = true)
    public List<ReportItemDTO> getLateArrivalsReport(LocalDate startDate, LocalDate endDate) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        List<AttendanceRecord> records = attendanceRepository
                .findByAttendanceDateBetweenOrderByAttendanceDateDescCheckInTimeDesc(start, end);

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

        return records.stream()
                .filter(r -> r.getCheckInTime() != null && r.getCheckInTime().toLocalTime().isAfter(expectedArrivalTime))
                .map(r -> {
                    Duration delay = Duration.between(expectedArrivalTime, r.getCheckInTime().toLocalTime());
                    long delayMins = delay.toMinutes();
                    return new ReportItemDTO(
                            r.getEmployee().getId(),
                            r.getEmployee().getCode(),
                            r.getEmployee().getFullName(),
                            r.getEmployee().getDepartment(),
                            r.getAttendanceDate(),
                            r.getCheckInTime().format(timeFmt),
                            "Ingreso a las " + r.getCheckInTime().format(timeFmt) + " (" + delayMins + " min post 09:30)"
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * RE-02: Reporte de Salidas Anticipadas (antes de las 17:30 PM)
     */
    @Transactional(readOnly = true)
    public List<ReportItemDTO> getEarlyExitsReport(LocalDate startDate, LocalDate endDate) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        List<AttendanceRecord> records = attendanceRepository
                .findByAttendanceDateBetweenOrderByAttendanceDateDescCheckInTimeDesc(start, end);

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

        return records.stream()
                .filter(r -> r.getCheckOutTime() != null && r.getCheckOutTime().toLocalTime().isBefore(expectedDepartureTime))
                .map(r -> {
                    Duration diff = Duration.between(r.getCheckOutTime().toLocalTime(), expectedDepartureTime);
                    long minsBefore = diff.toMinutes();
                    return new ReportItemDTO(
                            r.getEmployee().getId(),
                            r.getEmployee().getCode(),
                            r.getEmployee().getFullName(),
                            r.getEmployee().getDepartment(),
                            r.getAttendanceDate(),
                            r.getCheckOutTime().format(timeFmt),
                            "Salida a las " + r.getCheckOutTime().format(timeFmt) + " (" + minsBefore + " min antes de 17:30)"
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * RE-03: Reporte de Inasistencias
     * Empleados activos que no registraron ni entrada ni salida en la fecha especificada
     */
    @Transactional(readOnly = true)
    public List<ReportItemDTO> getAbsencesReport(LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        List<Employee> activeEmployees = employeeRepository.findByActiveTrue();
        List<AttendanceRecord> attendances = attendanceRepository.findByAttendanceDate(targetDate);

        Set<Long> attendedEmployeeIds = attendances.stream()
                .filter(a -> a.getCheckInTime() != null)
                .map(a -> a.getEmployee().getId())
                .collect(Collectors.toSet());

        List<ReportItemDTO> report = new ArrayList<>();
        for (Employee emp : activeEmployees) {
            if (!attendedEmployeeIds.contains(emp.getId())) {
                report.add(new ReportItemDTO(
                        emp.getId(),
                        emp.getCode(),
                        emp.getFullName(),
                        emp.getDepartment(),
                        targetDate,
                        "-",
                        "Sin registro de entrada ni salida en la fecha"
                ));
            }
        }
        return report;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getDashboardSummary() {
        LocalDate today = LocalDate.now();
        long totalEmployees = employeeRepository.countByActiveTrue();
        long presentToday = attendanceRepository.countByAttendanceDate(today);
        long lateToday = attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.LATE);
        long onTimeToday = attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.ON_TIME);
        long absentToday = Math.max(0, totalEmployees - presentToday);

        return new DashboardSummaryDTO(
                totalEmployees,
                presentToday,
                lateToday,
                onTimeToday,
                absentToday,
                LocalDateTime.now()
        );
    }
}
