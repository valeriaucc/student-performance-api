package com.viveek.aiclass.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AIClass API")
                        .description("API para la plataforma de gestión académica AIClass con análisis de rendimiento estudiantil")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("AIClass Team")
                                .email("contact@aiclass.com")
                                .url("https://aiclass.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desarrollo"),
                        new Server()
                                .url("https://api.aiclass.com")
                                .description("Servidor de Producción")
                ));
    }
}
