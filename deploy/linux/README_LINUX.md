# Despliegue en Linux (Ubuntu / Debian / CentOS / RHEL)

## Requisitos Previos
1. **Java 17 o 21**:
   ```bash
   sudo apt update
   sudo apt install -y openjdk-21-jre-headless
   ```
2. **MySQL Server**:
   ```bash
   sudo apt install -y mysql-server
   sudo mysql < database/init.sql
   ```

---

## Opción 1: Ejecución Directa
1. Dar permisos de ejecución al script:
   ```bash
   chmod +x deploy/linux/run.sh
   ```
2. Ejecutar:
   ```bash
   ./deploy/linux/run.sh
   ```

---

## Opción 2: Como Servicio del Sistema (`systemd`)
1. Copiar el archivo JAR a `/opt/controller-assist/`:
   ```bash
   sudo mkdir -p /opt/controller-assist
   sudo cp target/controller-assist-0.0.1-SNAPSHOT.jar /opt/controller-assist/
   ```
2. Copiar el archivo del servicio:
   ```bash
   sudo cp deploy/linux/controller-assist.service /etc/systemd/system/
   ```
3. Editar las credenciales de la base de datos si es necesario:
   ```bash
   sudo nano /etc/systemd/system/controller-assist.service
   ```
4. Recargar systemd y activar el servicio:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable controller-assist
   sudo systemctl start controller-assist
   ```
5. Comprobar estado y logs:
   ```bash
   sudo systemctl status controller-assist
   sudo journalctl -u controller-assist -f
   ```
