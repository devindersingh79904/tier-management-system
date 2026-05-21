package com.devinder.loyalty.filter;

import com.devinder.loyalty.constants.HeaderConstants;
import com.devinder.loyalty.constants.ErrorConstants;
import com.devinder.loyalty.util.CorrelationIdContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String correlationId = request.getHeader(HeaderConstants.CORRELATION_ID);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(CorrelationIdContext.MDC_KEY, correlationId);
        response.setHeader(HeaderConstants.CORRELATION_ID, correlationId);
        request.setAttribute(CorrelationIdContext.MDC_KEY, correlationId);

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 1024 * 1024);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            log.info("Incoming Request: {} {} {}", 
                     requestWrapper.getMethod(), 
                     requestWrapper.getRequestURI(), 
                     requestWrapper.getQueryString() != null ? "?" + requestWrapper.getQueryString() : "");

            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Retrieve request body (if any was read during processing)
            String reqBody = getRequestBody(requestWrapper);
            if (!reqBody.trim().isEmpty()) {
                log.debug("Request Body: {}", reqBody);
            }

            // Retrieve response body (if any was written during processing)
            String resBody = getResponseBody(responseWrapper);
            log.info("Outgoing Response: {} {} ({}ms)", 
                     responseWrapper.getStatus(), 
                     requestWrapper.getRequestURI(), 
                     duration);
            if (!resBody.trim().isEmpty()) {
                log.debug("Response Body: {}", resBody);
            }

            // Crucial: Copy the cache back into the real response stream so the client receives it
            responseWrapper.copyBodyToResponse();
            MDC.clear();
        }
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        if (buf.length > 0) {
            try {
                return new String(buf, 0, buf.length, request.getCharacterEncoding() != null ? request.getCharacterEncoding() : StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                return ErrorConstants.REQ_BODY_READ_ERROR;
            }
        }
        return "";
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] buf = response.getContentAsByteArray();
        if (buf.length > 0) {
            try {
                return new String(buf, 0, buf.length, response.getCharacterEncoding() != null ? response.getCharacterEncoding() : StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                return ErrorConstants.RES_BODY_READ_ERROR;
            }
        }
        return "";
    }
}
