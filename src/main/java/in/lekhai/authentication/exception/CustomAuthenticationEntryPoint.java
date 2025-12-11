package in.lekhai.authentication.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.lekhai.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException
    ) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        HttpStatus status;
        String message;

        if (authException instanceof UserDoesNotExistException) {
            status = HttpStatus.NOT_FOUND;
            message = authException.getMessage();
        } else if (authException instanceof BadCredentialsException) {
            status = HttpStatus.UNAUTHORIZED;
            message = "Invalid credentials";
        } else if (authException instanceof InsufficientAuthenticationException) {
            status = HttpStatus.UNAUTHORIZED;
            message = "Full authentication is required to access this resource";
        } else {
            status = HttpStatus.UNAUTHORIZED;
            message = "Authentication failed: " + authException.getMessage();
        }

        response.setStatus(status.value());
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(message)));
    }
}
