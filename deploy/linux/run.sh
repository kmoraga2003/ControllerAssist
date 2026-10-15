#!/usr/bin/env bash
# ====================================================================
# ControllerAssist - Script de Inicio para Linux / macOS
# ====================================================================

export DB_HOST=${DB_HOST:-localhost}
export DB_PORT=${DB_PORT:-3306}
export DB_NAME=${DB_NAME:-controller_assist_db}
export DB_USER=${DB_USER:-root}
export DB_PASSWORD=${DB_PASSWORD:-}
export PORT=${PORT:-8080}

JAR_FILE="target/controller-assist-0.0.1-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
    JAR_FILE="controller-assist-0.0.1-SNAPSHOT.jar"
fi

if [ ! -f "$JAR_FILE" ]; then
    echo "[ERROR] No se encontró el archivo JAR. Ejecuta: mvn clean package"
    exit 1
fi

echo "[ControllerAssist] Iniciando aplicación desde $JAR_FILE en puerto $PORT..."
java -Dserver.port=$PORT -jar $JAR_FILE
