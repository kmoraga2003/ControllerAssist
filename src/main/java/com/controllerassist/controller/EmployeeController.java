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
@Tag(name = "Empleados / Usuarios", description = "Endpoints de administración de usuarios (GU-01, GU-02, GU-03, AP-01)")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @Operation(
            summary = "AP-01: Listar empleados por estado",
            description = "Retorna un listado de empleados en formato JSON. Acepta parámetro de estado: ACTIVO, INACTIVO o TODOS."
    )
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployees(
            @Parameter(description = "Filtro de estado: ACTIVO, INACTIVO o TODOS")
            @RequestParam(required = false, defaultValue = "TODOS") String status,
            @Parameter(description = "Búsqueda por nombre, código o departamento")
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(employeeService.getEmployees(status, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener empleado por ID", description = "Retorna el detalle de un empleado específico mediante su identificador numérico.")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Obtener empleado por Código", description = "Busca un empleado por su código único (ej. EMP-101).")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeByCode(@PathVariable String code) {
        return ResponseEntity.ok(employeeService.getEmployeeByCode(code));
    }

    @PostMapping
    @Operation(summary = "GU-01: Crear usuario", description = "Crea un usuario/empleado con correo, contraseña y rol (ADMIN o EMPLEADO).")
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO dto) {
        EmployeeResponseDTO created = employeeService.createEmployee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "GU-02: Modificar usuario", description = "Actualiza los datos del usuario en el sistema.")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDTO dto) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "GU-03: Eliminar usuario", description = "Desactiva (baja lógica) al usuario conservando su historial de asistencias.")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
