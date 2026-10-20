package com.controllerassist.controller;

import com.controllerassist.dto.AttendanceResponseDTO;
import com.controllerassist.dto.DashboardSummaryDTO;
import com.controllerassist.dto.PunchRequestDTO;
import com.controllerassist.model.AttendanceStatus;
import com.controllerassist.model.PunchType;
import com.controllerassist.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendances")
@Tag(name = "Asistencias", description = "Endpoints para registro y consulta de asistencias (CA-01)")
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/punch")
    @Operation(
            summary = "CA-01: Marcar Asistencia (Botón Entrada/Salida)",
            description = "Registra Entrada o Salida para el empleado. Detecta automáticamente la acción requerida o acepta el tipo de marcación."
    )
    public ResponseEntity<AttendanceResponseDTO> punch(@Valid @RequestBody PunchRequestDTO request) {
        AttendanceResponseDTO response = attendanceService.punch(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/check-in")
    @Operation(summary = "Marcar Entrada explícita", description = "Registra la entrada del empleado para la jornada de hoy.")
    public ResponseEntity<AttendanceResponseDTO> checkIn(@Valid @RequestBody PunchRequestDTO request) {
        request.setType(PunchType.CHECK_IN);
        AttendanceResponseDTO response = attendanceService.punch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/check-out")
    @Operation(summary = "Marcar Salida explícita", description = "Registra la salida del empleado para la jornada de hoy.")
    public ResponseEntity<AttendanceResponseDTO> checkOut(@Valid @RequestBody PunchRequestDTO request) {
        request.setType(PunchType.CHECK_OUT);
        AttendanceResponseDTO response = attendanceService.punch(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/my-today/{employeeId}")
    @Operation(summary = "Obtener asistencia de hoy de un empleado", description = "Retorna el registro de asistencia del día para el empleado autenticado.")
    public ResponseEntity<AttendanceResponseDTO> getMyTodayAttendance(@PathVariable Long employeeId) {
        return attendanceService.getTodayAttendanceForEmployee(employeeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping
    @Operation(summary = "Consultar historial de asistencias con filtros", description = "Permite filtrar asistencias por empleado, rango de fechas y estado.")
    public ResponseEntity<List<AttendanceResponseDTO>> filterAttendances(
            @Parameter(description = "ID del empleado")
            @RequestParam(required = false) Long employeeId,
            @Parameter(description = "Fecha inicial (formato YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Fecha final (formato YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Estado de asistencia (ON_TIME, LATE, EARLY_EXIT, NORMAL)")
            @RequestParam(required = false) AttendanceStatus status) {
        return ResponseEntity.ok(attendanceService.filterAttendances(employeeId, startDate, endDate, status));
    }

    @GetMapping("/today")
    @Operation(summary = "Listar asistencias de hoy", description = "Retorna la lista de todas las asistencias registradas en la fecha actual.")
    public ResponseEntity<List<AttendanceResponseDTO>> getTodayAttendances() {
        return ResponseEntity.ok(attendanceService.getTodayAttendances());
    }

    @GetMapping("/summary/today")
    @Operation(summary = "Obtener resumen estadístico del día", description = "Retorna contadores de presentes, tardanzas, ausentes y total de empleados.")
    public ResponseEntity<DashboardSummaryDTO> getTodaySummary() {
        return ResponseEntity.ok(attendanceService.getDashboardSummary());
    }
}
