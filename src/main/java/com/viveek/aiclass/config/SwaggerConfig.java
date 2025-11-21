package com.viveek.aiclass.config;

import com.viveek.aiclass.api.DeprecationInterceptor;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig implements WebMvcConfigurer {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";
    
    private final DeprecationInterceptor deprecationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(deprecationInterceptor)
                .addPathPatterns("/api/**");
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AIClass API")
                        .description("""
                                API for AIClass academic management platform with student performance analytics.
                                
                                ## 🎉 What's New in v2.0 (Phase 3)
                                
                                ### API Versioning
                                - All endpoints now use `/api/v1/*` path prefix for semantic versioning
                                - Deprecated endpoints include `X-API-Deprecation` and `Sunset` headers
                                - Migration guidance provided via `Link` headers
                                
                                ### Performance Improvements
                                - Optimized object mapping with MapStruct
                                - Database query optimization with strategic indexes
                                - Enhanced connection pooling and caching
                                
                                ### Monitoring & Observability
                                - **Health Checks**: `/actuator/health` - Application and dependency health status
                                - **Metrics**: `/actuator/metrics` - Application metrics for monitoring
                                - **Prometheus**: `/actuator/prometheus` - Prometheus-compatible metrics export
                                - **Info**: `/actuator/info` - Application version and build information
                                - Custom business metrics tracked: user creation, enrollments, grade submissions
                                
                                ### Soft Delete Support
                                - Entities are soft-deleted (marked with `deletedAt` timestamp)
                                - Automatically filtered from all queries
                                - No breaking changes to API responses
                                
                                ### AI-Powered Recommendations
                                - **Grade-based recommendations**: Generate personalized AI recommendations for specific assessments
                                - **Class performance recommendations**: Get AI insights for overall class performance (teacher-focused)
                                - **Student performance recommendations**: Get AI insights for individual student performance in a class (teacher-focused)
                                - Recommendations are idempotent - regenerating returns existing recommendation unless `forceRegenerate=true`
                                - Powered by OpenAI GPT models for intelligent, context-aware suggestions
                                
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
                                
                                ## Pagination
                                Most list endpoints support pagination with query parameters:
                                - `page`: Page number (0-indexed, default: 0)
                                - `size`: Items per page (default: 20, max: 100)
                                
                                ## Rate Limiting
                                API requests are rate-limited to prevent abuse:
                                - **Authenticated users**: 100 requests per minute
                                - **Unauthenticated users**: 20 requests per minute
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
