package com.controllerassist.controller;

import com.controllerassist.dto.LoginRequestDTO;
import com.controllerassist.dto.LoginResponseDTO;
import com.controllerassist.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints de inicio de sesión de usuarios (CA-01)")
@CrossOrigin(origins = "*")
public class AuthController {

    private final EmployeeService employeeService;

    public AuthController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida correo y contraseña. Retorna los datos del usuario y su rol (ADMIN o EMPLEADO).")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = employeeService.login(request);
        return ResponseEntity.ok(response);
    }
}
