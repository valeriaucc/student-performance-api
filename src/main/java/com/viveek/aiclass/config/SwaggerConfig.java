package com.viveek.aiclass.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AIClass API")
                        .description("""
                                API for AIClass academic management platform with student performance analytics.
                                
                                ## Authentication
                                This API uses **JWT Bearer tokens** from Supabase Auth.
                                
                                ### How to authenticate:
                                1. Sign up/Login via Supabase Auth to get a JWT token
                                2. Click the 'Authorize' button below
                                3. Enter your token in the format: `Bearer <your-token>`
                                4. Click 'Authorize' and then 'Close'
                                5. All authenticated endpoints will now include your token
                                
                                ### Role-Based Access:
                                - **TEACHER**: Full access to create/modify classes, grades, and view all data
                                - **STUDENT**: View own enrollments, grades, and recommendations
                                """)
                        .version("2.0.0")
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
                                .description("Development Server"),
                        new Server()
                                .url("https://api.aiclass.com")
                                .description("Production Server")
                ))
                // Add JWT Bearer authentication security scheme
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your Supabase JWT token from the login response")))
                // Apply security globally to all endpoints
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
