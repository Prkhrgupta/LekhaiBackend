package in.lekhai.licensing.interceptor;

import in.lekhai.licensing.service.LicenseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LicenseGuardInterceptor implements HandlerInterceptor {

    private final LicenseService licenseService;

    public LicenseGuardInterceptor(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/license") || path.startsWith("/login") || path.startsWith("/actuator") || path.startsWith("/api/test")) {
            return true;
        }

        String method = request.getMethod();
        boolean isRead = HttpMethod.GET.matches(method) || HttpMethod.HEAD.matches(method) || HttpMethod.OPTIONS.matches(method);

        if (!isRead && licenseService.isReadOnlyMode()) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"LICENSE_EXPIRED\",\"message\":\"License has expired. Application is operating in Read-Only Grace Mode.\"}");
            return false;
        }

        return true;
    }
}
