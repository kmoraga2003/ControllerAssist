package com.controllerassist.service;

import com.controllerassist.dto.EmployeeRequestDTO;
import com.controllerassist.dto.EmployeeResponseDTO;
import com.controllerassist.dto.LoginRequestDTO;
import com.controllerassist.dto.LoginResponseDTO;
import com.controllerassist.exception.BadRequestException;
import com.controllerassist.exception.ResourceNotFoundException;
import com.controllerassist.model.Employee;
import com.controllerassist.model.Role;
import com.controllerassist.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Autenticación de usuarios por correo y contraseña (CA-01)
     */
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO dto) {
        Employee employee = employeeRepository.findByEmail(dto.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Credenciales incorrectas: correo o contraseña no válidos"));

        if (!employee.isActive()) {
            throw new BadRequestException("El usuario se encuentra inactivo. Comunícate con el Administrador.");
        }

        if (!employee.getPassword().equals(dto.getPassword())) {
            throw new BadRequestException("Credenciales incorrectas: correo o contraseña no válidos");
        }

        return LoginResponseDTO.fromEmployee(employee, "Autenticación exitosa");
    }

    /**
     * Listado de empleados con filtro por estado (AP-01)
     * Soporta: status = ACTIVO | INACTIVO | TODOS
     */
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getEmployees(String status, String search) {
        List<Employee> list;

        if (search != null && !search.trim().isEmpty()) {
            list = employeeRepository.searchEmployees(search.trim());
        } else {
            list = employeeRepository.findAll();
        }

        if (status != null && !status.trim().isEmpty()) {
            String s = status.trim().toUpperCase();
            if ("ACTIVO".equals(s) || "TRUE".equals(s)) {
                list = list.stream().filter(Employee::isActive).collect(Collectors.toList());
            } else if ("INACTIVO".equals(s) || "FALSE".equals(s)) {
                list = list.stream().filter(e -> !e.isActive()).collect(Collectors.toList());
            }
            // "TODOS" leaves full list intact
        }

        return list.stream()
                .map(EmployeeResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = findByIdOrThrow(id);
        return EmployeeResponseDTO.fromEntity(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeByCode(String code) {
        Employee employee = employeeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún empleado con código: " + code));
        return EmployeeResponseDTO.fromEntity(employee);
    }

    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
        if (employeeRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Ya existe un empleado con el código: " + dto.getCode());
        }
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Ya existe un empleado con el email: " + dto.getEmail());
        }

        Employee employee = new Employee();
        employee.setCode(dto.getCode().trim().toUpperCase());
        employee.setFirstName(dto.getFirstName().trim());
        employee.setLastName(dto.getLastName().trim());
        employee.setEmail(dto.getEmail().trim().toLowerCase());
        employee.setPassword(dto.getPassword() != null && !dto.getPassword().isBlank() ? dto.getPassword() : "123456");
        employee.setRole(dto.getRole() != null ? dto.getRole() : Role.EMPLEADO);
        employee.setDepartment(dto.getDepartment());
        employee.setPosition(dto.getPosition());
        employee.setActive(dto.getActive() != null ? dto.getActive() : true);

        Employee saved = employeeRepository.save(employee);
        return EmployeeResponseDTO.fromEntity(saved);
    }

    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto) {
        Employee employee = findByIdOrThrow(id);

        if (!employee.getCode().equalsIgnoreCase(dto.getCode()) && employeeRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Ya existe otro empleado con el código: " + dto.getCode());
        }
        if (!employee.getEmail().equalsIgnoreCase(dto.getEmail()) && employeeRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Ya existe otro empleado con el email: " + dto.getEmail());
        }

        employee.setCode(dto.getCode().trim().toUpperCase());
        employee.setFirstName(dto.getFirstName().trim());
        employee.setLastName(dto.getLastName().trim());
        employee.setEmail(dto.getEmail().trim().toLowerCase());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            employee.setPassword(dto.getPassword());
        }
        if (dto.getRole() != null) {
            employee.setRole(dto.getRole());
        }
        employee.setDepartment(dto.getDepartment());
        employee.setPosition(dto.getPosition());
        if (dto.getActive() != null) {
            employee.setActive(dto.getActive());
        }

        Employee updated = employeeRepository.save(employee);
        return EmployeeResponseDTO.fromEntity(updated);
    }

    public void deleteEmployee(Long id) {
        Employee employee = findByIdOrThrow(id);
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    public Employee findByIdOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + id));
    }
}
