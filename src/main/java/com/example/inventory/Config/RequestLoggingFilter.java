package com.example.inventory.Config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class RequestLoggingFilter implements Filter {

    private static final Logger apiLogger = LoggerFactory.getLogger("API_LOGGER");
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY_LOGGER");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Generate request ID for tracing
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);

        long startTime = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(formatter);
        String clientIp = getClientIpAddress(httpRequest);
        String method = httpRequest.getMethod();
        String endpoint = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();

        if (queryString != null) {
            endpoint += "?" + queryString;
        }

        // Log security-sensitive endpoints
        if (isSecuritySensitiveEndpoint(endpoint)) {
            securityLogger.warn("Security endpoint accessed - IP: {} | Method: {} | Endpoint: {} | Time: {}",
                    clientIp, method, endpoint, timestamp);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;

            apiLogger.info("REQ_ID: {} | Time: {} | IP: {} | Method: {} | Endpoint: {} | Status: {} | Response Time: {}ms | User-Agent: {}",
                    requestId,
                    timestamp,
                    clientIp,
                    method,
                    endpoint,
                    httpResponse.getStatus(),
                    responseTime,
                    httpRequest.getHeader("User-Agent")
            );

            // Log slow requests
            if (responseTime > 1000) {
                apiLogger.warn("SLOW REQUEST - REQ_ID: {} | Response Time: {}ms | Endpoint: {}",
                        requestId, responseTime, endpoint);
            }

            // Log error responses
            if (httpResponse.getStatus() >= 400) {
                apiLogger.error("ERROR RESPONSE - REQ_ID: {} | Status: {} | Endpoint: {} | IP: {}",
                        requestId, httpResponse.getStatus(), endpoint, clientIp);
            }

            MDC.clear();
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        String xForwarded = request.getHeader("X-Forwarded");
        if (xForwarded != null && !xForwarded.isEmpty() && !"unknown".equalsIgnoreCase(xForwarded)) {
            return xForwarded;
        }

        String forwarded = request.getHeader("Forwarded");
        if (forwarded != null && !forwarded.isEmpty() && !"unknown".equalsIgnoreCase(forwarded)) {
            return forwarded;
        }

        return request.getRemoteAddr();
    }

    private boolean isSecuritySensitiveEndpoint(String endpoint) {
        return endpoint.contains("/auth/") ||
                endpoint.contains("/users/") ||
                endpoint.contains("/login") ||
                endpoint.contains("/register");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        apiLogger.info("RequestLoggingFilter initialized");
    }

    @Override
    public void destroy() {
        apiLogger.info("RequestLoggingFilter destroyed");
    }
}