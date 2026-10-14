package com.controllerassist.config;

import com.controllerassist.model.AttendanceRecord;
import com.controllerassist.model.AttendanceStatus;
import com.controllerassist.model.Employee;
import com.controllerassist.repository.AttendanceRepository;
import com.controllerassist.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.java);

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    public DataInitializer(EmployeeRepository employeeRepository, AttendanceRepository attendanceRepository) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    public void run(String... args) {
        if (employeeRepository.count() == 0) {
            logger.info("Cargando datos iniciales de prueba en la base de datos...");

            Employee e1 = new Employee("EMP-101", "Carlos", "Silva", "carlos.silva@empresa.com", "Tecnología", "Desarrollador Senior");
            Employee e2 = new Employee("EMP-102", "María", "González", "maria.gonzalez@empresa.com", "Recursos Humanos", "Coordinadora de RRHH");
            Employee e3 = new Employee("EMP-103", "Andrés", "Morales", "andres.morales@empresa.com", "Finanzas", "Contador General");
            Employee e4 = new Employee("EMP-104", "Valentina", "Pérez", "valentina.perez@empresa.com", "Operaciones", "Supervisora de Operaciones");

            employeeRepository.saveAll(List.of(e1, e2, e3, e4));

            // Cargar una asistencia de prueba para hoy
            LocalDate today = LocalDate.now();
            AttendanceRecord record1 = new AttendanceRecord(
                    e1,
                    today,
                    LocalDateTime.of(today, LocalTime.of(8, 45, 0)),
                    AttendanceStatus.ON_TIME
            );
            record1.setNotes("Ingreso normal");

            AttendanceRecord record2 = new AttendanceRecord(
                    e2,
                    today,
                    LocalDateTime.of(today, LocalTime.of(9, 15, 0)),
                    AttendanceStatus.LATE
            );
            record2.setNotes("Retraso justificado por tráfico");

            attendanceRepository.saveAll(List.of(record1, record2));

            logger.info("Datos iniciales cargados con éxito (4 empleados y 2 asistencias de prueba).");
        }
    }
}
