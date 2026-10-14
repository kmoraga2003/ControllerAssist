-- Script de inicialización para MySQL
-- ControllerAssist - Sistema de Control de Asistencias

CREATE DATABASE IF NOT EXISTS controller_assist_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE controller_assist_db;

-- Las tablas son creadas automáticamente por Hibernate (spring.jpa.hibernate.ddl-auto: update).
-- Sin embargo, si deseas crearlas manualmente, a continuación se detalla su estructura:

CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    department VARCHAR(100),
    position VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in_time DATETIME,
    check_out_time DATETIME,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(255),
    CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- Índices recomendados para optimización de consultas
CREATE INDEX IF NOT EXISTS idx_employee_code ON employees(code);
CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance_records(attendance_date);
CREATE INDEX IF NOT EXISTS idx_attendance_employee ON attendance_records(employee_id);
