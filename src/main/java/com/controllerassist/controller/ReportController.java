package com.controllerassist.controller;

import com.controllerassist.dto.ReportItemDTO;
import com.controllerassist.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reportes Administrativos", description = "Reportes requeridos RE-01, RE-02 y RE-03 para el Administrador")
@CrossOrigin(origins = "*")
public class ReportController {

    private final AttendanceService attendanceService;

    public ReportController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/late")
    @Operation(
            summary = "RE-01: Reporte de Atrasos",
            description = "Retorna todos los empleados que registraron entrada después de las 09:30 AM en un rango de fechas."
    )
    public ResponseEntity<List<ReportItemDTO>> getLateArrivalsReport(
            @Parameter(description = "Fecha inicial (formato YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Fecha final (formato YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getLateArrivalsReport(startDate, endDate));
    }

    @GetMapping("/early-exit")
    @Operation(
            summary = "RE-02: Reporte de Salidas Anticipadas",
            description = "Retorna todos los empleados que registraron salida antes de las 17:30 PM en un rango de fechas."
    )
    public ResponseEntity<List<ReportItemDTO>> getEarlyExitsReport(
            @Parameter(description = "Fecha inicial (formato YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Fecha final (formato YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getEarlyExitsReport(startDate, endDate));
    }

    @GetMapping("/absences")
    @Operation(
            summary = "RE-03: Reporte de Inasistencias",
            description = "Retorna todos los empleados que no registraron ni entrada ni salida en la fecha consultada."
    )
    public ResponseEntity<List<ReportItemDTO>> getAbsencesReport(
            @Parameter(description = "Fecha a consultar (formato YYYY-MM-DD, por defecto hoy)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAbsencesReport(date));
    }
}
