package com.controllerassist.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ControllerAssist API - Control de Asistencias")
                        .version("1.0.0")
                        .description("API REST para el registro de asistencias de personal, control de horarios, empleados y reportes estadísticos.")
                        .contact(new Contact()
                                .name("Equipo ControllerAssist")
                                .email("soporte@controllerassist.local")))
                .servers(List.of(
                        new Server().url("/").description("Servidor Local / Servidor de Despliegue")
                ));
    }
}
