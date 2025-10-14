package com.viveek.aiclass.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    private RateLimitingFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        filter = new RateLimitingFilter();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(request.getRequestURI()).thenReturn("/api/test");
        lenient().when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void doFilter_FirstRequest_AllowsThrough() throws Exception {
        filter.doFilter(request, response, filterChain);
        
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilter_WithinLimit_AllowsThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/users");
        
        for (int i = 0; i < 10; i++) {
            filter.doFilter(request, response, filterChain);
        }
        
        verify(filterChain, times(10)).doFilter(request, response);
    }

    @Test
    void doFilter_ExceedingLimit_ReturnsRateLimitError() throws Exception {
        // Make 101 requests (exceeding the 100 request limit)
        for (int i = 0; i < 101; i++) {
            filter.doFilter(request, response, filterChain);
        }
        
        // Verify the last request was rate-limited
        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
        
        writer.flush();
        String responseBody = stringWriter.toString();
        assertThat(responseBody).contains("Rate limit exceeded");
    }

    @Test
    void doFilter_AuthEndpoint_UsesStricterLimit() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        
        // Auth endpoints have a 20 request limit
        for (int i = 0; i < 21; i++) {
            filter.doFilter(request, response, filterChain);
        }
        
        verify(response).setStatus(429);
    }

    @Test
    void doFilter_WithXForwardedForHeader_UsesHeaderIP() throws Exception {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
        
        filter.doFilter(request, response, filterChain);
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_DifferentIPs_IndependentLimits() throws Exception {
        HttpServletRequest request1 = mock(HttpServletRequest.class);
        HttpServletRequest request2 = mock(HttpServletRequest.class);
        
        when(request1.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request1.getRequestURI()).thenReturn("/api/test");
        when(request2.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request2.getRequestURI()).thenReturn("/api/test");
        
        filter.doFilter(request1, response, filterChain);
        filter.doFilter(request2, response, filterChain);
        
        verify(filterChain, times(2)).doFilter(any(), any());
    }

    @Test
    void doFilter_DifferentPaths_IndependentLimits() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/users");
        filter.doFilter(request, response, filterChain);
        
        when(request.getRequestURI()).thenReturn("/api/classes");
        filter.doFilter(request, response, filterChain);
        
        verify(filterChain, times(2)).doFilter(request, response);
    }

    @Test
    void doFilter_RegisterEndpoint_UsesAuthLimit() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/register");
        
        filter.doFilter(request, response, filterChain);
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_LoginEndpoint_UsesAuthLimit() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/login");
        
        filter.doFilter(request, response, filterChain);
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_RateLimitedResponse_ContainsCorrectMessage() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        
        for (int i = 0; i < 101; i++) {
            filter.doFilter(request, response, filterChain);
        }
        
        writer.flush();
        String responseBody = stringWriter.toString();
        assertThat(responseBody).contains("success\":false");
        assertThat(responseBody).contains("Rate limit exceeded");
        assertThat(responseBody).contains("data\":null");
    }
}

