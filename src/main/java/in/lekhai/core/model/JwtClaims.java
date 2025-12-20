package in.lekhai.core.model;

import in.lekhai.core.model.enums.Roles;
import org.springframework.security.oauth2.jwt.Jwt;

public record JwtClaims(
        String uuid,
        Roles role,
        Integer tenant,
        String username
) {
    public static JwtClaims fromJwt(Jwt token) {
        return new JwtClaims(
                token.getClaimAsString("uuid"),
                parseRole(token),
                parseTenant(token),
                token.getClaimAsString("subject")
        );
    }

    private static Roles parseRole(Jwt token) {
        String roleStr = token.getClaimAsString("scope");
        if (roleStr == null) {
            throw new IllegalStateException("Role claim missing in JWT");
        }
        try {
            return Roles.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid role in JWT: " + roleStr, e);
        }
    }

    private static Integer parseTenant(Jwt token) {
        Object tenantObj = token.getClaim("tenant");
        if (tenantObj instanceof Number) {
            if(tenantObj.equals(0)) return null; // SUPERADMIN
            return ((Number) tenantObj).intValue();
        }
        throw new IllegalStateException("Invalid tenant type: " + tenantObj.getClass());
    }

    public boolean hasTenant() {
        return tenant != 0;
    }
}