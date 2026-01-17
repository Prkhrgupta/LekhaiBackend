package in.lekhai.authentication.model;

import in.lekhai.core.enums.Roles;
import in.lekhai.error.controller.role.exception.InvalidRoleException;
import in.lekhai.error.controller.shop.exception.InvalidShopTypeException;
import org.springframework.security.oauth2.jwt.Jwt;

import static in.lekhai.common.JwtConstants.*;

public record JwtClaims(
        String uuid,
        Roles role,
        Integer shopCode,
        String username
) {
    public static JwtClaims fromJwt(Jwt token) {
        return new JwtClaims(
                token.getClaimAsString(UUID),
                parseRole(token),
                parseTenant(token),
                token.getClaimAsString(SUBJECT)
        );
    }

    private static Roles parseRole(Jwt token) {
        String roleStr = token.getClaimAsString(SCOPE);
        if (roleStr == null) {
            throw new InvalidRoleException("Role claim missing in JWT");
        }
        try {
            return Roles.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException(String.format("Invalid role in JWT: %s", roleStr));
        }
    }

    private static Integer parseTenant(Jwt token) {
        Object tenantObj = token.getClaim(SHOP_CODE);
        if (tenantObj instanceof Number) {
//            if(tenantObj.equals(-1)) return null; // SUPER ADMIN
            return ((Number) tenantObj).intValue();
        }
        throw new InvalidShopTypeException(tenantObj.getClass().toString());
    }
}