package in.lekhai.authentication.service;

import in.lekhai.authentication.config.properties.SuperAdminRoutesProperties.RouteConfig;
import in.lekhai.core.domain.feature.Features;
import in.lekhai.core.repository.feature.FeaturesRepo;
import in.lekhai.error.controller.feature.exception.InvalidSuperAdminRouteException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuperAdminRouteProvisionerTest {

    private static final String ROUTE = "/manage_permission/category_wise_permission";
    private static final RouteConfig CONFIG = new RouteConfig(ROUTE, "Category Permissions", "");

    @Mock
    private FeaturesRepo featuresRepo;
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private SuperAdminRouteProvisioner provisioner;

    @Test
    void ensureRoutes_noRoutes_doesNothing() {
        assertThat(provisioner.ensureRoutes(List.of())).isEmpty();

        verify(featuresRepo, never()).findByRoute(any());
    }

    @Test
    void ensureRoutes_existingRoute_reusesItsBit() {
        when(featuresRepo.findByRoute(ROUTE)).thenReturn(Optional.of(feature(7L, 3)));

        Set<Integer> bits = provisioner.ensureRoutes(List.of(CONFIG));

        assertThat(bits).containsExactly(3);
        verify(featuresRepo, never()).save(any());
    }

    @Test
    void ensureRoutes_missingRoute_createsParentThenScreenWithNextBit() {
        when(featuresRepo.findByRoute(ROUTE)).thenReturn(Optional.empty());
        when(featuresRepo.findByFeatureKeyAndParentId("manage_permission", null)).thenReturn(Optional.empty());
        when(featuresRepo.findMaxDisplayOrderByParentId(any())).thenReturn(-1);
        when(featuresRepo.findNextAvailableBitPosition()).thenReturn(5);
        when(featuresRepo.save(any(Features.class))).thenAnswer(invocation -> {
            Features saved = invocation.getArgument(0);
            saved.setId(saved.getRoute() == null ? 10L : 11L);
            return saved;
        });

        Set<Integer> bits = provisioner.ensureRoutes(List.of(CONFIG));

        assertThat(bits).containsExactly(5);
        ArgumentCaptor<Features> captor = ArgumentCaptor.forClass(Features.class);
        verify(featuresRepo, org.mockito.Mockito.times(2)).save(captor.capture());
        Features parent = captor.getAllValues().get(0);
        Features screen = captor.getAllValues().get(1);
        assertThat(parent.getFeatureKey()).isEqualTo("manage_permission");
        assertThat(parent.getBitPosition()).isNull();
        assertThat(parent.getIcon()).isEmpty();
        assertThat(parent.getParentFeatureId()).isNull();
        assertThat(screen.getRoute()).isEqualTo(ROUTE);
        assertThat(screen.getParentFeatureId()).isEqualTo(10L);
        assertThat(screen.getTitle()).isEqualTo("Category Permissions");
        assertThat(screen.getIcon()).isEmpty();
        assertThat(parent.getTitle()).isEqualTo("Manage Permission");
        assertThat(screen.getBitPosition()).isEqualTo(5);
    }

    @Test
    void ensureRoutes_blankTitle_derivesTitleFromLastSegment() {
        when(featuresRepo.findByRoute("/role_permission")).thenReturn(Optional.empty());
        when(featuresRepo.findMaxDisplayOrderByParentId(isNull())).thenReturn(-1);
        when(featuresRepo.findNextAvailableBitPosition()).thenReturn(0);
        when(featuresRepo.save(any(Features.class))).thenAnswer(invocation -> invocation.getArgument(0));

        provisioner.ensureRoutes(List.of(new RouteConfig("/role_permission", " ", null)));

        ArgumentCaptor<Features> captor = ArgumentCaptor.forClass(Features.class);
        verify(featuresRepo).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Role Permission");
    }

    @Test
    void ensureRoutes_existingParent_isNotRecreated() {
        when(featuresRepo.findByRoute(ROUTE)).thenReturn(Optional.empty());
        when(featuresRepo.findByFeatureKeyAndParentId("manage_permission", null))
                .thenReturn(Optional.of(feature(10L, null)));
        when(featuresRepo.findMaxDisplayOrderByParentId(eq(10L))).thenReturn(0);
        when(featuresRepo.findNextAvailableBitPosition()).thenReturn(2);
        when(featuresRepo.save(any(Features.class))).thenAnswer(invocation -> invocation.getArgument(0));

        provisioner.ensureRoutes(List.of(CONFIG));

        ArgumentCaptor<Features> captor = ArgumentCaptor.forClass(Features.class);
        verify(featuresRepo).save(captor.capture());
        assertThat(captor.getValue().getParentFeatureId()).isEqualTo(10L);
        assertThat(captor.getValue().getDisplayOrder()).isEqualTo(1);
    }

    @Test
    void ensureRoutes_existingRouteWithoutBit_throws() {
        when(featuresRepo.findByRoute(ROUTE)).thenReturn(Optional.of(feature(7L, null)));

        assertThatThrownBy(() -> provisioner.ensureRoutes(List.of(CONFIG)))
                .isInstanceOf(InvalidSuperAdminRouteException.class);
    }

    @Test
    void ensureRoutes_blankRoute_throws() {
        assertThatThrownBy(() -> provisioner.ensureRoutes(List.of(new RouteConfig(" / ", null, null))))
                .isInstanceOf(InvalidSuperAdminRouteException.class);
    }

    private Features feature(Long id, Integer bit) {
        return Features.builder().id(id).bitPosition(bit).build();
    }
}
