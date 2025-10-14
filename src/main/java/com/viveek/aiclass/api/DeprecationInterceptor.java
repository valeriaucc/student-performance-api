package com.viveek.aiclass.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that adds deprecation headers to deprecated API endpoints.
 * Automatically detects @Deprecated annotation and adds appropriate headers.
 */
@Slf4j
@Component
public class DeprecationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            
            // Check for deprecated annotation on method
            Deprecated methodDeprecation = handlerMethod.getMethodAnnotation(Deprecated.class);
            
            // Check for deprecated annotation on class
            Deprecated classDeprecation = handlerMethod.getBeanType().getAnnotation(Deprecated.class);
            
            Deprecated deprecation = methodDeprecation != null ? methodDeprecation : classDeprecation;
            
            if (deprecation != null) {
                // Add deprecation header
                StringBuilder deprecationMessage = new StringBuilder("This API endpoint is deprecated");
                
                if (!deprecation.since().isEmpty()) {
                    deprecationMessage.append(" since version ").append(deprecation.since());
                }
                
                if (!deprecation.alternative().isEmpty()) {
                    deprecationMessage.append(". Use ").append(deprecation.alternative()).append(" instead");
                }
                
                if (!deprecation.description().isEmpty()) {
                    deprecationMessage.append(". ").append(deprecation.description());
                }
                
                response.addHeader(ApiVersions.DEPRECATION_HEADER, deprecationMessage.toString());
                
                // Add sunset header if specified
                if (!deprecation.sunset().isEmpty()) {
                    response.addHeader(ApiVersions.SUNSET_HEADER, deprecation.sunset());
                }
                
                log.warn("Deprecated API endpoint accessed: {} {}", request.getMethod(), request.getRequestURI());
            }
        }
        
        return true;
    }
}

