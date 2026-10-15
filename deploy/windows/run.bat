@echo off
REM ====================================================================
REM ControllerAssist - Script de Inicio para Windows / Windows Server
REM ====================================================================

echo [ControllerAssist] Iniciando aplicacion...

REM Configurar variables de base de datos MySQL (ajustar segun tu servidor)
SET DB_HOST=localhost
SET DB_PORT=3306
SET DB_NAME=controller_assist_db
SET DB_USER=root
SET DB_PASSWORD=
SET PORT=8080

REM Comprobar si existe el archivo JAR
IF NOT EXIST "target\controller-assist-0.0.1-SNAPSHOT.jar" (
    IF NOT EXIST "controller-assist-0.0.1-SNAPSHOT.jar" (
        echo [ERROR] No se encontro el archivo JAR.
        echo Ejecuta primero: mvn clean package
        pause
        exit /b 1
    ) ELSE (
        SET JAR_FILE=controller-assist-0.0.1-SNAPSHOT.jar
    )
) ELSE (
    SET JAR_FILE=target\controller-assist-0.0.1-SNAPSHOT.jar
)

echo [ControllerAssist] Ejecutando: %JAR_FILE%
java -Dspring.datasource.url="jdbc:mysql://%DB_HOST%:%DB_PORT%/%DB_NAME%?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" -Dspring.datasource.username="%DB_USER%" -Dspring.datasource.password="%DB_PASSWORD%" -Dserver.port=%PORT% -jar %JAR_FILE%

pause
