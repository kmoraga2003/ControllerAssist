# ====================================================================
# Dockerfile - ControllerAssist (Spring Boot 3.3 + Java 21)
# ====================================================================

# Etapa 1: Imagen base ligera con Java 21 JRE
FROM eclipse-temurin:21-jre-alpine

# Metadatos
LABEL maintainer="ControllerAssist"
LABEL description="Sistema de Control de Asistencias - Empresa Química"

# Crear directorio de trabajo
WORKDIR /app

# Añadir usuario sin privilegios por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiar el archivo JAR empaquetado
COPY target/controller-assist-0.0.1-SNAPSHOT.jar app.jar

# Variables de entorno por defecto
ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=mysql \
    JAVA_OPTS="-Xms256m -Xmx512m"

# Exponer el puerto de la aplicación
EXPOSE 8080

# Comando de arranque optimizado
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
