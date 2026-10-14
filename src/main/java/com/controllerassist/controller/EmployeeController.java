package com.controllerassist.controller;

import com.controllerassist.dto.EmployeeRequestDTO;
import com.controllerassist.dto.EmployeeResponseDTO;
import com.controllerassist.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Empleados", description = "Endpoints para la administración del personal")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @Operation(summary = "Listar empleados", description = "Retorna todos los empleados con opciones de filtrado por activos y búsqueda de texto.")
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees(
            @Parameter(description = "Filtrar únicamente empleados activos")
            @RequestParam(required = false) Boolean activeOnly,
            @Parameter(description = "Búsqueda por nombre, código o departamento")
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(employeeService.getAllEmployees(activeOnly, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener empleado por ID", description = "Retorna el detalle de un empleado específico mediante su identificador numérico.")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Obtener empleado por Código", description = "Busca un empleado por su código único (ej. EMP-001).")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeByCode(@PathVariable String code) {
        return ResponseEntity.ok(employeeService.getEmployeeByCode(code));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo empleado", description = "Crea un empleado en la base de datos MySQL validando código y email únicos.")
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO dto) {
        EmployeeResponseDTO created = employeeService.createEmployee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de empleado", description = "Actualiza la información de un empleado existente.")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDTO dto) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar empleado", description = "Realiza una baja lógica del empleado estableciendo active = false.")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
