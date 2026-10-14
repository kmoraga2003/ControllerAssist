package com.controllerassist;

import com.controllerassist.dto.EmployeeRequestDTO;
import com.controllerassist.dto.EmployeeResponseDTO;
import com.controllerassist.dto.PunchRequestDTO;
import com.controllerassist.dto.AttendanceResponseDTO;
import com.controllerassist.service.AttendanceService;
import com.controllerassist.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
    void testCreateEmployeeAndPunch() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setCode("TEST-999");
        dto.setFirstName("Juan");
        dto.setLastName("Pérez");
        dto.setEmail("juan.perez@test.com");
        dto.setDepartment("TI");
        dto.setPosition("Tester");

        EmployeeResponseDTO created = employeeService.createEmployee(dto);
        assertNotNull(created.getId());
        assertEquals("TEST-999", created.getCode());

        // Probar marcación punch
        PunchRequestDTO punch = new PunchRequestDTO("TEST-999");
        AttendanceResponseDTO attendance = attendanceService.punch(punch);
        assertNotNull(attendance.getId());
        assertNotNull(attendance.getCheckInTime());
        assertEquals("TEST-999", attendance.getEmployeeCode());
    }
}
