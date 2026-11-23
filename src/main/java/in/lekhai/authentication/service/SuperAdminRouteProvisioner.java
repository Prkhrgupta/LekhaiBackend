package in.lekhai.authentication.service;

import in.lekhai.authentication.config.properties.SuperAdminRoutesProperties.RouteConfig;
import in.lekhai.core.domain.feature.Features;
import in.lekhai.core.repository.feature.FeaturesRepo;
import in.lekhai.error.controller.feature.exception.InvalidSuperAdminRouteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Makes sure every configured super-admin route exists as a feature and reports the bits to enable.
 * Bits are never configured; a missing screen feature gets the next free bit, same as FeatureService.
 */
@Component
public class SuperAdminRouteProvisioner {

    // why: arbitrary app-wide key so concurrently starting instances don't both pick MAX(bit_position) + 1
    private static final long ADVISORY_LOCK_KEY = 7_340_001L;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final FeaturesRepo featuresRepo;
    private final JdbcTemplate jdbcTemplate;

    public SuperAdminRouteProvisioner(FeaturesRepo featuresRepo, JdbcTemplate jdbcTemplate) {
        this.featuresRepo = featuresRepo;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Set<Integer> ensureRoutes(List<RouteConfig> routes) {
        Set<Integer> bits = new TreeSet<>();
        if (routes.isEmpty()) {
            return bits;
        }
        jdbcTemplate.queryForList("SELECT pg_advisory_xact_lock(?)", ADVISORY_LOCK_KEY);
        for (RouteConfig routeConfig : routes) {
            bits.add(ensureRoute(routeConfig));
        }
        return bits;
    }

    private Integer ensureRoute(RouteConfig routeConfig) {
        List<String> segments = parseSegments(routeConfig.route());
        String normalizedRoute = "/" + String.join("/", segments);
        Features feature = featuresRepo.findByRoute(normalizedRoute)
                .orElseGet(() -> createRoute(routeConfig, normalizedRoute, segments));
        if (feature.getBitPosition() == null) {
            throw new InvalidSuperAdminRouteException(
                    routeConfig.route(), "feature exists but is not a screen (no bit position)");
        }
        return feature.getBitPosition();
    }

    private Features createRoute(RouteConfig routeConfig, String route, List<String> segments) {
        Long parentId = null;
        for (String parentKey : segments.subList(0, segments.size() - 1)) {
            parentId = ensureParent(parentKey, parentId).getId();
        }
        String leafKey = segments.get(segments.size() - 1);
        String title = routeConfig.title() == null || routeConfig.title().isBlank()
                ? toTitle(leafKey)
                : routeConfig.title();
        Features screen = featuresRepo.save(Features.builder()
                .featureKey(leafKey)
                .parentFeatureId(parentId)
                .title(title)
                .icon(routeConfig.icon())
                .route(route)
                .bitPosition(featuresRepo.findNextAvailableBitPosition())
                .displayOrder(featuresRepo.findMaxDisplayOrderByParentId(parentId) + 1)
                .build());
        log.info("Created super-admin route {} with bit {}", route, screen.getBitPosition());
        return screen;
    }

    private Features ensureParent(String featureKey, Long parentId) {
        return featuresRepo.findByFeatureKeyAndParentId(featureKey, parentId)
                .orElseGet(() -> featuresRepo.save(Features.builder()
                        .featureKey(featureKey)
                        .parentFeatureId(parentId)
                        .title(toTitle(featureKey))
                        .icon("")
                        .displayOrder(featuresRepo.findMaxDisplayOrderByParentId(parentId) + 1)
                        .build()));
    }

    private List<String> parseSegments(String route) {
        List<String> segments = route == null ? List.of() : Arrays.stream(route.split("/"))
                .map(String::trim)
                .filter(segment -> !segment.isEmpty())
                .toList();
        if (segments.isEmpty()) {
            throw new InvalidSuperAdminRouteException(route, "route must have at least one segment");
        }
        return segments;
    }

    private String toTitle(String featureKey) {
        return Arrays.stream(featureKey.split("[_\\-]"))
                .filter(word -> !word.isEmpty())
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
