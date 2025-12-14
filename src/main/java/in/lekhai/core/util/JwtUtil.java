package in.lekhai.core.util;

import in.lekhai.core.model.JwtClaims;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    public JwtClaims extractJwtClaim() {
        Jwt token = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return JwtClaims.fromJwt(token);
    }
}
