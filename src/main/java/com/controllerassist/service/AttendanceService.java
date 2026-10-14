package com.controllerassist.service;

import com.controllerassist.dto.AttendanceResponseDTO;
import com.controllerassist.dto.DashboardSummaryDTO;
import com.controllerassist.dto.PunchRequestDTO;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    // Hora límite para considerar llegada a tiempo (por defecto 09:00:00)
    private final LocalTime expectedArrivalTime;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            EmployeeRepository employeeRepository,
            @Value("${attendance.arrival-threshold:09:00:00}") String arrivalThreshold) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.expectedArrivalTime = LocalTime.parse(arrivalThreshold);
    }

    /**
     * Marcación inteligente (Punch): detecta si el empleado debe marcar entrada o salida
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

            AttendanceStatus status = now.toLocalTime().isAfter(expectedArrivalTime)
                    ? AttendanceStatus.LATE
                    : AttendanceStatus.ON_TIME;

            AttendanceRecord record = new AttendanceRecord();
            record.setEmployee(employee);
            record.setAttendanceDate(today);
            record.setCheckInTime(now);
            record.setStatus(status);
            record.setNotes(request.getNotes());

            AttendanceRecord saved = attendanceRepository.save(record);
            AttendanceResponseDTO response = AttendanceResponseDTO.fromEntity(saved);
            response.setMessage("ENTRADA registrada para " + employee.getFullName() + " a las " + now.format(timeFormatter) +
                    (status == AttendanceStatus.LATE ? " (Tardanza)" : " (A tiempo)"));
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
                if (request.getNotes() != null && !request.getNotes().isBlank()) {
                    String prev = record.getNotes() != null ? record.getNotes() + " | " : "";
                    record.setNotes(prev + request.getNotes());
                }

                AttendanceRecord saved = attendanceRepository.save(record);
                AttendanceResponseDTO response = AttendanceResponseDTO.fromEntity(saved);
                response.setMessage("SALIDA registrada para " + employee.getFullName() + " a las " + now.format(timeFormatter));
                return response;
            } else {
                // Ya completó entrada y salida hoy
                throw new BadRequestException("La jornada de hoy ya fue completada para " + employee.getFullName() +
                        " (Entrada: " + record.getCheckInTime().format(timeFormatter) +
                        ", Salida: " + record.getCheckOutTime().format(timeFormatter) + ").");
            }
        }
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
