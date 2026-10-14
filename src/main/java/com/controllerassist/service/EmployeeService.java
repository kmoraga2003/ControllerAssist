package com.controllerassist.service;

import com.controllerassist.dto.EmployeeRequestDTO;
import com.controllerassist.dto.EmployeeResponseDTO;
import com.controllerassist.exception.BadRequestException;
import com.controllerassist.exception.ResourceNotFoundException;
import com.controllerassist.model.Employee;
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

    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getAllEmployees(Boolean activeOnly, String search) {
        List<Employee> list;

        if (search != null && !search.trim().isEmpty()) {
            list = employeeRepository.searchEmployees(search.trim());
            if (Boolean.TRUE.equals(activeOnly)) {
                list = list.stream().filter(Employee::isActive).collect(Collectors.toList());
            }
        } else if (Boolean.TRUE.equals(activeOnly)) {
            list = employeeRepository.findByActiveTrue();
        } else {
            list = employeeRepository.findAll();
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
        // Desactivación lógica para no romper registros históricos de asistencia
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    public Employee findByIdOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + id));
    }
}
