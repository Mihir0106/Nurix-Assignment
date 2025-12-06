package com.mj.Nurix_Assignment.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Security filter for API key authentication.
 * Uses Filter Pattern and follows Single Responsibility Principle.
 * Skips authentication for actuator endpoints.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value("${app.api-key:default-api-key}")
    private String validApiKey;

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String ACTUATOR_PATH = "/actuator";

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // Skip authentication for actuator endpoints
        if (requestPath.startsWith(ACTUATOR_PATH)) {
            log.debug("Skipping API key authentication for actuator endpoint: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Missing API key in request to: {}", requestPath);
            sendUnauthorizedResponse(response, "Missing API key");
            return;
        }

        if (!apiKey.equals(validApiKey)) {
            log.warn("Invalid API key in request to: {}", requestPath);
            sendUnauthorizedResponse(response, "Invalid API key");
            return;
        }

        log.debug("API key authentication successful for: {}", requestPath);
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) 
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message));
    }
}

