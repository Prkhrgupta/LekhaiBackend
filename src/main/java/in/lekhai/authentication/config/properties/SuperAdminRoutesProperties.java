package in.lekhai.authentication.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Routes (features) that must always be enabled for the default super admin.
 */
@ConfigurationProperties(prefix = "super-admin")
public record SuperAdminRoutesProperties(List<RouteConfig> routes) {

    public SuperAdminRoutesProperties {
        routes = routes == null ? List.of() : List.copyOf(routes);
    }

    /**
     * @param route path whose segments are the feature keys, e.g. /manage_permission/role_permission
     * @param title menu title of the screen; derived from the last segment when blank
     * @param icon  menu icon of the screen
     */
    public record RouteConfig(String route, String title, String icon) {
    }
}
