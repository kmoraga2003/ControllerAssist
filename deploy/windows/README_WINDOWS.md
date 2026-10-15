# Despliegue en Windows Server

## Requisitos Previos
1. **Java Runtime Environment (JRE o JDK 17 o 21)** instalado en Windows Server.
   - Puedes descargarlo de [Eclipse Temurin](https://adoptium.net/) u [Oracle JDK](https://www.oracle.com/java/technologies/downloads/).
   - Asegúrate de marcar la casilla "Add to PATH" durante la instalación.
2. **MySQL Server 8.0+** instalado y corriendo en Windows Server (o accesible por red).
   - Crear la base de datos ejecutando `database/init.sql` en MySQL Workbench o consola.

---

## Opción 1: Ejecución Directa mediante Script
1. Copia la carpeta del proyecto o el archivo `controller-assist-0.0.1-SNAPSHOT.jar` a un directorio del servidor, por ejemplo `C:\apps\controller-assist\`.
2. Edita `deploy\windows\run.bat` con el usuario y contraseña de tu MySQL.
3. Haz doble clic en `run.bat` o ejecútalo desde el Command Prompt / PowerShell.
4. Abre el navegador en:
   ```
   http://localhost:8080
   ```
   O desde otra máquina en la red local:
   ```
   http://<IP_DEL_WINDOWS_SERVER>:8080
   ```

---

## Opción 2: Ejecutar como Servicio de Windows (Autoinicio con el Sistema)
Para que la aplicación se inicie automáticamente al reiniciar Windows Server sin que haya un usuario conectado:

1. Descarga **NSSM (Non-Sucking Service Manager)** desde `https://nssm.cc/download`.
2. Abre una ventana de **PowerShell como Administrador** y ejecuta:
   ```cmd
   nssm install ControllerAssist
   ```
3. En la interfaz gráfica:
   - **Path**: `C:\Program Files\Eclipse Adoptium\jdk-21...\bin\java.exe`
   - **Startup directory**: `C:\apps\controller-assist`
   - **Arguments**: `-jar target\controller-assist-0.0.1-SNAPSHOT.jar`
4. Haz clic en **Install service**.
5. Inicia el servicio:
   ```cmd
   nssm start ControllerAssist
   ```
6. El servicio quedará corriendo en segundo plano y se reiniciará automáticamente ante cualquier caída.
