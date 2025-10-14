package com.viveek.aiclass.config;

import com.viveek.aiclass.api.DeprecationInterceptor;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SwaggerConfigTest {

    @Mock
    private DeprecationInterceptor deprecationInterceptor;

    @Mock
    private InterceptorRegistry interceptorRegistry;

    private SwaggerConfig config;

    @BeforeEach
    void setUp() {
        config = new SwaggerConfig(deprecationInterceptor);
    }

    @Test
    void addInterceptors_AddsDeprecationInterceptor() {
        when(interceptorRegistry.addInterceptor(deprecationInterceptor))
                .thenReturn(new org.springframework.web.servlet.config.annotation.InterceptorRegistration(deprecationInterceptor));
        
        config.addInterceptors(interceptorRegistry);
        
        verify(interceptorRegistry).addInterceptor(deprecationInterceptor);
    }

    @Test
    void customOpenAPI_ReturnsOpenAPI() {
        OpenAPI openAPI = config.customOpenAPI();
        
        assertThat(openAPI).isNotNull();
    }

    @Test
    void customOpenAPI_HasInfo() {
        OpenAPI openAPI = config.customOpenAPI();
        
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("AIClass API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("2.0.0");
    }

    @Test
    void customOpenAPI_HasDescription() {
        OpenAPI openAPI = config.customOpenAPI();
        
        assertThat(openAPI.getInfo().getDescription()).isNotNull();
        assertThat(openAPI.getInfo().getDescription()).contains("AIClass");
    }

    @Test
    void customOpenAPI_HasContact() {
        OpenAPI openAPI = config.customOpenAPI();
        
        assertThat(openAPI.getInfo().getContact()).isNotNull();
        assertThat(openAPI.getInfo().getContact().getName()).isEqualTo("AIClass Team");
        assertThat(openAPI.getInfo().getContact().getEmail()).isEqualTo("contact@aiclass.com");
    }

    @Test
    void customOpenAPI_HasLicense() {
        OpenAPI openAPI = config.customOpenAPI();
        
        assertThat(openAPI.getInfo().getLicense()).isNotNull();
        assertThat(openAPI.getInfo().getLicense().getName()).isEqualTo("MIT License");
    }

    @Test
    void customOpenAPI_HasServers() {
        OpenAPI openAPI = config.customOpenAPI();
        
        assertThat(openAPI.getServers()).isNotNull();
        assertThat(openAPI.getServers()).hasSize(2);
        assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("http://localhost:8080");
        assertThat(openAPI.getServers().get(1).getUrl()).isEqualTo("https://api.aiclass.com");
    }

    @Test
    void customOpenAPI_HasSecurityScheme() {
        OpenAPI openAPI = config.customOpenAPI();
        
        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("Bearer Authentication");
    }

    @Test
    void customOpenAPI_SecuritySchemeIsBearer() {
        OpenAPI openAPI = config.customOpenAPI();
        
        var securityScheme = openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication");
        assertThat(securityScheme.getType()).isEqualTo(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP);
        assertThat(securityScheme.getScheme()).isEqualTo("bearer");
        assertThat(securityScheme.getBearerFormat()).isEqualTo("JWT");
    }

    @Test
    void customOpenAPI_HasSecurityRequirement() {
        OpenAPI openAPI = config.customOpenAPI();
        
        assertThat(openAPI.getSecurity()).isNotNull();
        assertThat(openAPI.getSecurity()).isNotEmpty();
    }
}

