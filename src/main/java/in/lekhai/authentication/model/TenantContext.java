package in.lekhai.authentication.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TenantContext {

    private static final Logger log = LoggerFactory.getLogger(TenantContext.class);
    private static final ThreadLocal<String> tenantId = new ThreadLocal<>();

    public static void setTenantId(String value) {
        log.info("Setting tenant id :: {}", value);         // TODO: remove
        tenantId.set(value);
    }

    public static String getTenantId() {
        return tenantId.get();
    }

    public static void clear() {
        log.info("Clearing tenant id");             // TODO: remove
        tenantId.remove();
    }

}
