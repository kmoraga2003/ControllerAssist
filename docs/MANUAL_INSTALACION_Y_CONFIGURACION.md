# Manual de Instalación, Configuración de Servidor Web y Despliegue

**Proyecto**: Sistema de Control de Asistencias (ControllerAssist)  
**Cliente**: Empresa de Compra y Venta de Productos Químicos (25 trabajadores)  
**Versión**: 1.0 (MVP)  

---

## 1. Introducción y Arquitectura de la Solución

El sistema ControllerAssist está diseñado para garantizar el cumplimiento normativo laboral y la gestión eficiente de entradas, salidas, atrasos e inasistencias. 

La arquitectura se compone de:
- **Capa de Presentación / Servidor Web**: Interfaz web responsiva desarrollada con Bootstrap 5, servida mediante Nginx (Reverse Proxy) o directamente por el servidor web embebido Apache Tomcat en el puerto `8080`.
- **Capa de Negocio / Backend**: API REST desarrollada en Java 21 con Spring Boot 3.3.
- **Capa de Persistencia / Base de Datos**: Servidor relacional **MySQL 8.0+** con motor InnoDB y juego de caracteres `utf8mb4`.

---

## 2. Requerimientos de Hardware y Software

| Componente | Requisito Mínimo Recomendado |
|---|---|
| **Sistema Operativo** | Ubuntu Server 22.04/24.04 LTS o Windows Server 2019/2022 |
| **CPU** | 2 núcleos vCPU |
| **Memoria RAM** | 2 GB a 4 GB RAM |
| **Almacenamiento** | 20 GB SSD |
| **Java Runtime** | OpenJDK 21 LTS (o JRE 17+) |
| **Base de Datos** | MySQL Server 8.0+ |
| **Servidor Web** | Nginx (Recomendado para Linux) o IIS (Windows) |

---

## 3. Instalación y Configuración del Sistema Operativo

### Opción A: En Linux (Ubuntu Server)

#### 3.1. Actualización del Sistema
```bash
sudo apt update && sudo apt upgrade -y
```

#### 3.2. Configuración de Zona Horaria (Crucial para el control de asistencia)
```bash
sudo timedatectl set-timezone America/Santiago
timedatectl status
```

#### 3.3. Creación de Usuario de Sistema sin privilegios de Root
```bash
sudo useradd -r -s /bin/false appassist
```

#### 3.4. Configuración del Firewall (UFW)
```bash
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP (Nginx)
sudo ufw allow 443/tcp   # HTTPS (Nginx)
sudo ufw allow 8080/tcp  # Spring Boot (opcional para pruebas directas)
sudo ufw enable
```

---

### Opción B: En Windows Server

1. **Configuración de Firewall de Windows**:
   - Abrir *Windows Defender Firewall con Seguridad Avanzada*.
   - Crear una nueva **Regla de Entrada**:
     - Tipo: Puerto.
     - Protocolo: TCP.
     - Puertos locales específicos: `80`, `8080`.
     - Acción: Permitir la conexión.
     - Perfil: Dominio, Privado y Público.
     - Nombre: `ControllerAssist Web`.
2. **Zona Horaria**: Verificar en *Configuración de Fecha y Hora* que esté sincronizada con la zona local (ej. UTC-3 Santiago).

---

## 4. Instalación y Configuración del Servidor de Base de Datos (MySQL)

### 4.1. Instalación de MySQL en Linux
```bash
sudo apt install -y mysql-server
sudo systemctl enable mysql
sudo systemctl start mysql
```

### 4.2. Seguridad Inicial
```bash
sudo mysql_secure_installation
```

### 4.3. Creación de la Base de Datos y Permisos
Acceder a la consola de MySQL:
```bash
sudo mysql -u root -p
```
Ejecutar las siguientes instrucciones SQL:
```sql
-- Crear base de datos para la empresa química
CREATE DATABASE IF NOT EXISTS controller_assist_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Crear usuario exclusivo para la aplicación
CREATE USER 'assist_user'@'localhost' IDENTIFIED BY 'PasswordQuimica2026!';
GRANT ALL PRIVILEGES ON controller_assist_db.* TO 'assist_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 4.4. Cargar Tablas e Índices (Opcional, Hibernate las genera automáticamente)
```bash
mysql -u assist_user -p controller_assist_db < database/init.sql
```

---

## 5. Instalación del Runtime de Java (OpenJDK 21)

### En Linux:
```bash
sudo apt install -y openjdk-21-jre-headless
java -version
```

### En Windows Server:
1. Descargar el instalador MSI de **Eclipse Temurin OpenJDK 21** desde [adoptium.net](https://adoptium.net/).
2. Durante el asistente de instalación, asegurarse de marcar la opción **"Set JAVA_HOME variable"** y **"Add to PATH"**.
3. Validar en PowerShell:
   ```powershell
   java -version
   ```

---

## 6. Configuración del Servidor Web (Nginx Reverse Proxy en Linux)

Nginx actúa como servidor web frontal de alto rendimiento para gestionar el puerto estándar HTTP (80), compresión gzip y seguridad, redirigiendo el tráfico interno al puerto 8080 de Spring Boot.

### 6.1. Instalación de Nginx
```bash
sudo apt install -y nginx
sudo systemctl enable nginx
sudo systemctl start nginx
```

### 6.2. Configuración del Sitio
Crear el archivo de configuración en `/etc/nginx/sites-available/controller-assist`:
```bash
sudo nano /etc/nginx/sites-available/controller-assist
```

Pegar el siguiente bloque de configuración:
```nginx
server {
    listen 80;
    server_name tu-dominio.cl o IP_PUBLICA;

    client_max_body_size 10M;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Optimización de conexiones HTTP
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

### 6.3. Habilitar el Sitio y Reiniciar Nginx
```bash
sudo ln -s /etc/nginx/sites-available/controller-assist /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl restart nginx
```

---

## 7. Implementación de la Aplicación Web

### 7.1. Compilación del Paquete JAR
En la máquina de desarrollo o integración continua:
```bash
mvn clean package -DskipTests
```
Se obtiene el archivo `target/controller-assist-0.0.1-SNAPSHOT.jar`.

### 7.2. Despliegue en Linux con Systemd (Servicio Continuo)
1. Copiar el archivo JAR al directorio de aplicaciones:
   ```bash
   sudo mkdir -p /opt/controller-assist
   sudo cp target/controller-assist-0.0.1-SNAPSHOT.jar /opt/controller-assist/
   sudo chown -R appassist:appassist /opt/controller-assist
   ```

2. Configurar el servicio `systemd` en `/etc/systemd/system/controller-assist.service`:
   ```ini
   [Unit]
   Description=ControllerAssist - Control de Asistencias (Empresa Quimica)
   After=syslog.target network.target mysql.service

   [Service]
   User=appassist
   Group=appassist
   WorkingDirectory=/opt/controller-assist
   ExecStart=/usr/bin/java -jar /opt/controller-assist/controller-assist-0.0.1-SNAPSHOT.jar
   SuccessExitStatus=143
   Restart=always
   RestartSec=10

   # Parámetros de conexión a MySQL
   Environment="DB_HOST=localhost"
   Environment="DB_PORT=3306"
   Environment="DB_NAME=controller_assist_db"
   Environment="DB_USER=assist_user"
   Environment="DB_PASSWORD=PasswordQuimica2026!"
   Environment="PORT=8080"

   [Install]
   WantedBy=multi-user.target
   ```

3. Activar e Iniciar el Servicio:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable controller-assist
   sudo systemctl start controller-assist
   ```

4. Verificar Estado:
   ```bash
   sudo systemctl status controller-assist
   ```

---

### 7.3. Despliegue en Windows Server (Como Servicio con NSSM)
1. Copiar el archivo `controller-assist-0.0.1-SNAPSHOT.jar` a `C:\apps\controller-assist\`.
2. Descargar **NSSM** desde `https://nssm.cc/download`.
3. Abrir PowerShell como Administrador e instalar el servicio:
   ```cmd
   nssm install ControllerAssist
   ```
4. Configurar en la ventana emergente:
   - **Path**: Ruta al ejecutable de Java (ej. `C:\Program Files\Eclipse Adoptium\jdk-21...\bin\java.exe`).
   - **Startup directory**: `C:\apps\controller-assist`.
   - **Arguments**: `-Dspring.datasource.username=root -Dspring.datasource.password=TuClave -jar controller-assist-0.0.1-SNAPSHOT.jar`.
5. Iniciar el servicio:
   ```cmd
   nssm start ControllerAssist
   ```

---

## 8. Verificación y Validación de la Solución

1. **Acceso a la Interfaz Web**:
   Ingresar desde cualquier navegador a `http://<IP_DEL_SERVIDOR>`:
   - Aparece la ventana de **Login**.
   - Acceso con usuario Administrador: `admin@quimica.cl` / `admin123`.
   - Acceso con usuario Empleado: `carlos.silva@quimica.cl` / `123456`.

2. **Verificación de Requerimientos**:
   - **CA-01 (Marcación)**: El empleado marca Entrada o Salida con el botón y visualiza el cálculo de horas trabajadas y confirmación inmediata.
   - **RE-01 (Atrasos)**: El administrador consulta los ingresos posteriores a las 09:30 AM en la pestaña de reportes.
   - **RE-02 (Salidas Anticipadas)**: El administrador consulta las salidas anteriores a las 17:30 PM.
   - **RE-03 (Inasistencias)**: El administrador lista a los colaboradores sin registro de entrada ni salida en la fecha.
   - **GU-01 a GU-03 (Usuarios)**: Creación, modificación y desactivación de usuarios desde el modal.
   - **AP-01 (API REST)**: Consulta `http://<IP_DEL_SERVIDOR>/api/employees?status=ACTIVO`.
   - **Documentación Swagger**: Disponible en `http://<IP_DEL_SERVIDOR>/swagger-ui.html`.
   - **Colección Postman**: Descargable desde el botón superior o en `postman/ControllerAssist.postman_collection.json`.
