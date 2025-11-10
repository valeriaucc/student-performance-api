package com.viveek.aiclass.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeprecationInterceptorTest {

    private DeprecationInterceptor interceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HandlerMethod handlerMethod;

    @BeforeEach
    void setUp() {
        interceptor = new DeprecationInterceptor();
    }

    @Test
    void preHandle_NonHandlerMethod_ReturnsTrue() throws Exception {
        Object handler = new Object();
        
        boolean result = interceptor.preHandle(request, response, handler);
        
        assertThat(result).isTrue();
        verifyNoInteractions(response);
    }

    @Test
    void preHandle_NonDeprecatedMethod_NoHeaders() throws Exception {
        when(handlerMethod.getMethodAnnotation(Deprecated.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);
        
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        assertThat(result).isTrue();
        verify(response, never()).addHeader(anyString(), anyString());
    }

    @Test
    void preHandle_DeprecatedMethod_AddsHeader() throws Exception {
        Deprecated deprecation = mock(Deprecated.class);
        when(deprecation.since()).thenReturn("");
        when(deprecation.alternative()).thenReturn("");
        when(deprecation.description()).thenReturn("");
        when(deprecation.sunset()).thenReturn("");
        
        when(handlerMethod.getMethodAnnotation(Deprecated.class)).thenReturn(deprecation);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");
        
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        assertThat(result).isTrue();
        verify(response).addHeader(eq(ApiVersions.DEPRECATION_HEADER), contains("deprecated"));
    }

    @Test
    void preHandle_DeprecatedWithDetails_AddsCompleteHeader() throws Exception {
        Deprecated deprecation = mock(Deprecated.class);
        when(deprecation.since()).thenReturn("1.0");
        when(deprecation.alternative()).thenReturn("/api/v2/new-endpoint");
        when(deprecation.description()).thenReturn("Use the new API");
        when(deprecation.sunset()).thenReturn("2025-12-31");
        
        when(handlerMethod.getMethodAnnotation(Deprecated.class)).thenReturn(deprecation);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");
        
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        assertThat(result).isTrue();
        verify(response).addHeader(eq(ApiVersions.DEPRECATION_HEADER), 
                contains("deprecated"));
        verify(response).addHeader(eq(ApiVersions.SUNSET_HEADER), eq("2025-12-31"));
    }

    // Test controller class
    private static class TestController {
        public void testMethod() {}
    }
}

