package tenant.context.model;

import in.lekhai.error.controller.tenant.exception.InvalidTenantIdException;

import java.util.Objects;

public final class TenantContext {

    private static final ThreadLocal<Integer> tenantContext = new ThreadLocal<>();

    public static void setTenantId(Integer value) {
        if(Objects.isNull(value)) {
            throw new InvalidTenantIdException();
        }
        tenantContext.set(value);
    }

    public static Integer getTenantId() {
        return tenantContext.get();
    }

    public static void clear() {
        tenantContext.remove();
    }
}
