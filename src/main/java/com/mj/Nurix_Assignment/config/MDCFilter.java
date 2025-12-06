package com.mj.Nurix_Assignment.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MDCFilter implements Filter {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String X_TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String traceId = UUID.randomUUID().toString();

        try {
            MDC.put(TRACE_ID_KEY, traceId);

            if (response instanceof HttpServletResponse httpResponse) {
                httpResponse.setHeader(X_TRACE_ID_HEADER, traceId);
            }

            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
