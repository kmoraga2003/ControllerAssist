package com.controllerassist;

import com.controllerassist.dto.*;
import com.controllerassist.model.Role;
import com.controllerassist.service.AttendanceService;
import com.controllerassist.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ControllerAssistApplicationTests {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AttendanceService attendanceService;

    @Test
    void contextLoads() {
        assertNotNull(employeeService);
        assertNotNull(attendanceService);
    }

    @Test
    void testLoginFlow() {
        // Login con usuario admin sembrado en DataInitializer
        LoginRequestDTO loginReq = new LoginRequestDTO("admin@quimica.cl", "admin123");
        LoginResponseDTO loginRes = employeeService.login(loginReq);
        assertNotNull(loginRes);
        assertEquals(Role.ADMIN, loginRes.getRole());
        assertEquals("admin@quimica.cl", loginRes.getEmail());
    }

    @Test
    void testCreateEmployeeAndPunch() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setCode("TEST-999");
        dto.setFirstName("Juan");
        dto.setLastName("Pérez");
        dto.setEmail("juan.perez@quimica.cl");
        dto.setPassword("secreto123");
        dto.setRole(Role.EMPLEADO);
        dto.setDepartment("Planta");
        dto.setPosition("Operador");

        EmployeeResponseDTO created = employeeService.createEmployee(dto);
        assertNotNull(created.getId());
        assertEquals("TEST-999", created.getCode());
        assertEquals(Role.EMPLEADO, created.getRole());

        // Probar marcación punch
        PunchRequestDTO punch = new PunchRequestDTO("TEST-999");
        AttendanceResponseDTO attendance = attendanceService.punch(punch);
        assertNotNull(attendance.getId());
        assertNotNull(attendance.getCheckInTime());
        assertEquals("TEST-999", attendance.getEmployeeCode());
        assertEquals("En curso", attendance.getWorkedHoursFormatted());
    }

    @Test
    void testReportsAndApiFilter() {
        LocalDate today = LocalDate.now();

        // RE-01: Reporte de atrasos (post 09:30 AM)
        List<ReportItemDTO> lateList = attendanceService.getLateArrivalsReport(today, today);
        assertNotNull(lateList);
        // En DataInitializer sembramos a EMP-102 (María González) que llegó a las 09:42 AM
        assertTrue(lateList.stream().anyMatch(r -> "EMP-102".equals(r.getEmployeeCode())));

        // RE-02: Reporte de salidas anticipadas (antes de 17:30 PM)
        List<ReportItemDTO> earlyList = attendanceService.getEarlyExitsReport(today, today);
        assertNotNull(earlyList);
        // En DataInitializer sembramos a EMP-103 (Andrés Morales) que salió a las 16:15 PM
        assertTrue(earlyList.stream().anyMatch(r -> "EMP-103".equals(r.getEmployeeCode())));

        // RE-03: Reporte de inasistencias
        List<ReportItemDTO> absences = attendanceService.getAbsencesReport(today);
        assertNotNull(absences);
        // Los empleados no registrados hoy deben figurar en la lista
        assertFalse(absences.isEmpty());

        // AP-01: Listar empleados filtrados por estado ACTIVO / INACTIVO / TODOS
        List<EmployeeResponseDTO> activeEmps = employeeService.getEmployees("ACTIVO", null);
        assertNotNull(activeEmps);
        assertFalse(activeEmps.isEmpty());

        List<EmployeeResponseDTO> allEmps = employeeService.getEmployees("TODOS", null);
        assertTrue(allEmps.size() >= activeEmps.size());
    }
}
