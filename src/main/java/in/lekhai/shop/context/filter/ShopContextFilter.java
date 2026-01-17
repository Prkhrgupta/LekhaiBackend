package in.lekhai.shop.context.filter;

import io.micrometer.common.lang.NonNull;
import in.lekhai.shop.context.model.ShopContext;
import in.lekhai.authentication.model.JwtClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ShopContextFilter extends OncePerRequestFilter {
    /**
    *Request enters server
    *→ Filter runs
    *→ TenantContext.setTenantId(...)
    *→ Controller / service / repository executes
    *→ Response is generated
    *→ doFilter() returns
    *→ finally block executes
    *→ ThreadLocal.remove()
    *→ Thread returned to thread pool
    */

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof JwtAuthenticationToken)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            Jwt token = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            JwtClaims claims = JwtClaims.fromJwt(token);
            ShopContext.setShopCode(claims.shopCode());
            filterChain.doFilter(request, response);
        } finally {
            ShopContext.clear();
        }
    }
}
