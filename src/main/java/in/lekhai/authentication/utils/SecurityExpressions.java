package in.lekhai.authentication.utils;

public final class SecurityExpressions {
    public static final String NOT_SUPER_ADMIN = "!hasRole('SUPER_ADMIN')";
    public static final String IS_SUPER_ADMIN = "hasRole('SUPER_ADMIN')";
    public static final String IS_SHOP_OWNER = "hasRole('SHOP_OWNER')";
}
