# ControllerAssist - Sistema de Control de Asistencias

Sistema completo de control y registro de asistencias de personal desarrollado en **Java con Spring Boot**, compatible con **MySQL**, interfaz web responsiva autocontenida, soporte integral para **Postman** y documentación interactiva con **Swagger / OpenAPI**.

Diseñado para ser empaquetado en un único archivo ejecutable `.jar` y desplegarse sin fricción tanto en **Windows Server** como en distribuciones de **Linux**.

---

## 🚀 Características Principales

- **Gestión de Empleados (CRUD)**:
  - Registro de personal con código único (ej. `EMP-101`), nombre, correo, departamento y cargo.
  - Búsqueda en tiempo real por texto y filtros por estado (activo/inactivo).
  - Baja lógica para conservar la integridad histórica de registros.

- **Marcación Inteligente de Asistencias (Kiosco / Punch)**:
  - Detección automática: si el empleado no ha ingresado hoy, registra **Entrada**; si ya ingresó y no ha salido, registra **Salida**.
  - Cálculo automático de puntualidad: clasifica como **A tiempo** o **Tardanza** (según hora límite configurable, por defecto 09:00:00).
  - Opciones de Entrada y Salida explícitas con notas y observaciones.

- **Dashboard Web Integrado**:
  - Interfaz gráfica limpia (Bootstrap 5 + Icons) servida directamente por Spring Boot en `http://localhost:8080`.
  - Reloj digital en vivo.
  - Indicadores en tiempo real (KPIs): Total Empleados, Presentes Hoy, Tardanzas Hoy, Ausentes Estimados.
  - Kiosco de marcación rápida con selector de empleados y lector de código.
  - Tabla de asistencias del día con actualización en vivo.
  - Historial con filtros por rango de fechas, empleado y estado.

- **Documentación API y Pruebas con Postman**:
  - **Swagger UI**: Interfaz gráfica en vivo en `http://localhost:8080/swagger-ui.html`.
  - **OpenAPI v3**: Especificación en `http://localhost:8080/v3/api-docs`.
  - **Colección de Postman**: Archivo listo para importar en `postman/ControllerAssist.postman_collection.json` (también descargable con un clic desde el botón de la barra de navegación web).

---

## 🛠️ Tecnologías Utilizadas

- **Java 21 / 17** (LTS)
- **Spring Boot 3.3.x / 3.4.x** (Spring Web, Spring Data JPA, Spring Validation)
- **Hibernate ORM**
- **MySQL Connector/J** (Base de datos MySQL 8.x)
- **Springdoc OpenAPI 3 (Swagger UI)**
- **Bootstrap 5 & Vanilla JavaScript** (Frontend embebido sin dependencias Node.js)
- **Maven** (Gestión de dependencias y empaquetado)

---

## 🗄️ Base de Datos MySQL

### 1. Crear la Base de Datos
En tu cliente MySQL (Workbench, phpMyAdmin o terminal), ejecuta el script incluido:
```sql
CREATE DATABASE IF NOT EXISTS controller_assist_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```
*(También disponible en [database/init.sql](file:///Users/scl076pcad005/Desktop/Proyectos%20/ControllerAssist/database/init.sql))*.

### 2. Configuración de Conexión
En `src/main/resources/application.yml` o mediante variables de entorno:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/controller_assist_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: tu_password
```

---

## 💻 Ejecución en Desarrollo

### Compilar y Empaquetar
```bash
mvn clean package
```
Generará el archivo `.jar` en `target/controller-assist-0.0.1-SNAPSHOT.jar`.

### Ejecutar la Aplicación
```bash
java -jar target/controller-assist-0.0.1-SNAPSHOT.jar
```

Abre tu navegador en:
- **Página Web / Dashboard**: [http://localhost:8080](http://localhost:8080)
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

*(Al arrancar por primera vez, el sistema precarga automáticamente 4 empleados y registros de prueba para que comiences a explorar de inmediato).*

---

## 📡 Endpoints de la API REST

### Empleados (`/api/employees`)
| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/employees` | Listar empleados (soporta `activeOnly=true` y `search=texto`) |
| `GET` | `/api/employees/{id}` | Obtener empleado por ID |
| `GET` | `/api/employees/code/{code}` | Obtener empleado por Código (ej. `EMP-101`) |
| `POST` | `/api/employees` | Crear nuevo empleado |
| `PUT` | `/api/employees/{id}` | Actualizar datos del empleado |
| `DELETE` | `/api/employees/{id}` | Desactivar empleado (baja lógica) |

### Asistencias (`/api/attendances`)
| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/attendances/punch` | Marcación inteligente (Entrada o Salida automática) |
| `POST` | `/api/attendances/check-in` | Marcación explícita de Entrada |
| `POST` | `/api/attendances/check-out` | Marcación explícita de Salida |
| `GET` | `/api/attendances/today` | Listar asistencias registradas en el día |
| `GET` | `/api/attendances/summary/today` | Resumen de métricas del día (KPIs) |
| `GET` | `/api/attendances` | Consultar asistencias con filtros (`startDate`, `endDate`, `employeeId`, `status`) |

---

## 📦 Despliegue en Servidores

### En Windows Server
Revisa la guía detallada en [deploy/windows/README_WINDOWS.md](file:///Users/scl076pcad005/Desktop/Proyectos%20/ControllerAssist/deploy/windows/README_WINDOWS.md).
- Puedes ejecutar el script [deploy/windows/run.bat](file:///Users/scl076pcad005/Desktop/Proyectos%20/ControllerAssist/deploy/windows/run.bat).
- Para ejecutarlo como servicio de inicio automático del sistema operativo sin sesión abierta, se utiliza **NSSM** o **WinSW**.

### En Linux (Ubuntu / Debian / CentOS / RHEL)
Revisa la guía detallada en [deploy/linux/README_LINUX.md](file:///Users/scl076pcad005/Desktop/Proyectos%20/ControllerAssist/deploy/linux/README_LINUX.md).
- Incluye plantilla de servicio `systemd` en [deploy/linux/controller-assist.service](file:///Users/scl076pcad005/Desktop/Proyectos%20/ControllerAssist/deploy/linux/controller-assist.service).
- Script directo de ejecución en [deploy/linux/run.sh](file:///Users/scl076pcad005/Desktop/Proyectos%20/ControllerAssist/deploy/linux/run.sh).
