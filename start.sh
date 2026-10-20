#!/usr/bin/env bash
# ====================================================================
# ControllerAssist - Script de Inicio Rápido (macOS / Linux)
# ====================================================================

# Navegar automáticamente a la carpeta donde está este script
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"

# Configurar Java si está instalado vía Homebrew
if [ -d "/opt/homebrew/opt/openjdk/bin" ]; then
    export PATH="/opt/homebrew/opt/openjdk/bin:$PATH"
fi

JAR_FILE="target/controller-assist-0.0.1-SNAPSHOT.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "[ControllerAssist] No se encontró el archivo JAR. Compilando..."
    mvn package -DskipTests
fi

echo "============================================================"
echo "  Iniciando ControllerAssist - Empresa Química"
echo "============================================================"
echo "  URL Web:       http://localhost:8080"
echo "  Swagger UI:    http://localhost:8080/swagger-ui.html"
echo "  Credenciales:"
echo "    - Admin:     admin@quimica.cl   (clave: admin123)"
echo "    - Empleado:  carlos.silva@quimica.cl (clave: 123456)"
echo "============================================================"
echo ""

java -jar "$JAR_FILE"
