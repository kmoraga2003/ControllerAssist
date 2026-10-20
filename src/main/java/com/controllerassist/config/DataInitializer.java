package com.controllerassist.config;

import com.controllerassist.model.AttendanceRecord;
import com.controllerassist.model.AttendanceStatus;
import com.controllerassist.model.Employee;
import com.controllerassist.model.Role;
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

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    public DataInitializer(EmployeeRepository employeeRepository, AttendanceRepository attendanceRepository) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    public void run(String... args) {
        if (employeeRepository.count() == 0) {
            logger.info("Inicializando datos para la empresa química (Administrador y Empleados)...");

            // 1. Usuario Administrador (GU-01, CA-01)
            Employee admin = new Employee("ADM-001", "Admin", "General", "admin@quimica.cl", "admin123", Role.ADMIN, "Gerencia General", "Administrador del Sistema");

            // 2. Empleados de la empresa química
            Employee e1 = new Employee("EMP-101", "Carlos", "Silva", "carlos.silva@quimica.cl", "123456", Role.EMPLEADO, "Operaciones Químicas", "Operador de Planta");
            Employee e2 = new Employee("EMP-102", "María", "González", "maria.gonzalez@quimica.cl", "123456", Role.EMPLEADO, "Control de Calidad", "Química Analista");
            Employee e3 = new Employee("EMP-103", "Andrés", "Morales", "andres.morales@quimica.cl", "123456", Role.EMPLEADO, "Ventas", "Ejecutivo Comercial");
            Employee e4 = new Employee("EMP-104", "Valentina", "Pérez", "valentina.perez@quimica.cl", "123456", Role.EMPLEADO, "Bodega y Despacho", "Jefa de Bodega");
            Employee e5 = new Employee("EMP-105", "Roberto", "Muñoz", "roberto.munoz@quimica.cl", "123456", Role.EMPLEADO, "Seguridad Industrial", "Prevencionista de Riesgos");
            Employee e6 = new Employee("EMP-106", "Patricia", "Lagos", "patricia.lagos@quimica.cl", "123456", Role.EMPLEADO, "Compras", "Analista de Abastecimiento");
            Employee e7 = new Employee("EMP-107", "Felipe", "Castro", "felipe.castro@quimica.cl", "123456", Role.EMPLEADO, "Producción", "Técnico de Mezclas");

            employeeRepository.saveAll(List.of(admin, e1, e2, e3, e4, e5, e6, e7));

            // 3. Registros de prueba para validar los reportes de inmediato
            LocalDate today = LocalDate.now();

            // Caso 1: Entrada a tiempo (08:45 AM)
            AttendanceRecord r1 = new AttendanceRecord(
                    e1,
                    today,
                    LocalDateTime.of(today, LocalTime.of(8, 45, 0)),
                    AttendanceStatus.ON_TIME
            );
            r1.setNotes("Ingreso normal turno mañana");

            // Caso 2: Entrada con atraso post 09:30 AM (09:42 AM) -> RE-01
            AttendanceRecord r2 = new AttendanceRecord(
                    e2,
                    today,
                    LocalDateTime.of(today, LocalTime.of(9, 42, 0)),
                    AttendanceStatus.LATE
            );
            r2.setNotes("Llegada 12 minutos post 09:30");

            // Caso 3: Entrada a tiempo (08:30) y salida anticipada antes de 17:30 (16:15) -> RE-02
            AttendanceRecord r3 = new AttendanceRecord(
                    e3,
                    today,
                    LocalDateTime.of(today, LocalTime.of(8, 30, 0)),
                    AttendanceStatus.EARLY_EXIT
            );
            r3.setCheckOutTime(LocalDateTime.of(today, LocalTime.of(16, 15, 0)));
            r3.setNotes("Salida anticipada autorizada por trámite");

            // e4, e5, e6, e7 NO tienen registro hoy -> RE-03 Reporte de inasistencias los listará como ausentes

            attendanceRepository.saveAll(List.of(r1, r2, r3));

            logger.info("Datos iniciales cargados exitosamente.");
        }
    }
}
