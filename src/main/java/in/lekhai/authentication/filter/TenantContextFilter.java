package in.lekhai.authentication.filter;

import in.lekhai.authentication.model.TenantContext;
import in.lekhai.authentication.model.JwtClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
public class TenantContextFilter extends OncePerRequestFilter {
    /*
    Request enters server
    → Filter runs
    → TenantContext.setTenantId(...)
    → Controller / service / repository executes
    → Response is generated
    → doFilter() returns
    → finally block executes
    → ThreadLocal.remove() ← data cleared
    → Thread returned to thread pool
    */

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof BearerTokenAuthenticationFilter)) {
            filterChain.doFilter(request, response);
            return;
        }

        Jwt token = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JwtClaims claims = JwtClaims.fromJwt(token);
        TenantContext.setTenantId(claims.tenant().toString());
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
