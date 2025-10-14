package com.viveek.aiclass.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitingFilter implements Filter {

    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    private static final int GLOBAL_CAPACITY = 100;
    private static final Duration GLOBAL_REFILL_DURATION = Duration.ofMinutes(1);

    private static final int AUTH_CAPACITY = 20;
    private static final Duration AUTH_REFILL_DURATION = Duration.ofMinutes(1);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String ip = getClientIP(httpRequest);
        String path = httpRequest.getRequestURI();

        Bucket bucket = resolveBucket(ip, path);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {} on path: {}", ip, path);
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                "{\"success\":false,\"message\":\"Rate limit exceeded. Please try again later.\",\"data\":null}"
            );
        }
    }

    private Bucket resolveBucket(String ip, String path) {
        String key = ip + ":" + path;
        return ipBuckets.computeIfAbsent(key, k -> createNewBucket(path));
    }

    private Bucket createNewBucket(String path) {
        Bandwidth limit;
        
        if (isAuthEndpoint(path)) {
            limit = Bandwidth.classic(AUTH_CAPACITY, 
                                    Refill.intervally(AUTH_CAPACITY, AUTH_REFILL_DURATION));
        } else {
            limit = Bandwidth.classic(GLOBAL_CAPACITY, 
                                    Refill.intervally(GLOBAL_CAPACITY, GLOBAL_REFILL_DURATION));
        }

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private boolean isAuthEndpoint(String path) {
        return path.contains("/auth") || 
               path.contains("/login") || 
               path.contains("/register");
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
