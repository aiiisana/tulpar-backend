package kz.diploma.tulpar.config;

import jakarta.servlet.http.HttpServletRequest;
import kz.diploma.tulpar.dto.response.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Automatically wraps every @RestController response body in ApiResponse.
 * Skips: already-wrapped responses, Swagger/actuator paths, error status codes.
 */
@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    private static final String[] SKIP_PREFIXES = {
            "/swagger-ui", "/v3/api-docs", "/actuator", "/admin-ui"
    };

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        // Already wrapped — pass through
        if (body instanceof ApiResponse<?>) {
            return body;
        }

        // Skip Swagger / actuator / admin-ui paths
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpReq = servletRequest.getServletRequest();
            String path = httpReq.getRequestURI();
            for (String prefix : SKIP_PREFIXES) {
                if (path.contains(prefix)) {
                    return body;
                }
            }
        }

        return ApiResponse.ok(body);
    }
}
