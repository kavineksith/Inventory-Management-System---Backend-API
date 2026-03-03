package com.example.inventory.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastRequestTime = new ConcurrentHashMap<>();
    private final int MAX_REQUESTS = 100; // Max requests per minute
    private final long TIME_WINDOW = 60000; // 1 minute in milliseconds

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIP(request);
        long currentTime = System.currentTimeMillis();

        // Clean up old entries
        cleanupOldEntries(currentTime);

        // Check rate limit
        AtomicInteger count = requestCounts.computeIfAbsent(clientIp, k -> new AtomicInteger(0));
        Long lastTime = lastRequestTime.get(clientIp);

        if (lastTime == null || (currentTime - lastTime) > TIME_WINDOW) {
            // Reset counter for new time window
            count.set(1);
            lastRequestTime.put(clientIp, currentTime);
        } else {
            // Increment counter
            int currentCount = count.incrementAndGet();
            if (currentCount > MAX_REQUESTS) {
                response.setStatus(429); // Too Many Requests
                response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
                response.setContentType("application/json");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private void cleanupOldEntries(long currentTime) {
        lastRequestTime.entrySet().removeIf(entry ->
                (currentTime - entry.getValue()) > TIME_WINDOW);
        requestCounts.entrySet().removeIf(entry ->
                !lastRequestTime.containsKey(entry.getKey()));
    }
}