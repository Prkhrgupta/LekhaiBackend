package in.lekhai.authentication.service;

import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.core.domain.shop.Shops;
import in.lekhai.core.domain.users.UserShopAccess;
import in.lekhai.core.domain.users.Users;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.core.repository.users.UserShopAccessRepo;
import in.lekhai.core.repository.users.UsersRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static in.lekhai.common.JwtConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    private JwtEncoder jwtEncoder;
    private UsersRepo usersRepo;
    private UserShopAccessRepo userShopAccessRepo;
    private ShopsRepo shopsRepo;

    @BeforeEach
    void setUp() {
        jwtEncoder = mock(JwtEncoder.class);
        usersRepo = mock(UsersRepo.class);
        userShopAccessRepo = mock(UserShopAccessRepo.class);
        shopsRepo = mock(ShopsRepo.class);

        jwtTokenService = new JwtTokenService(
                jwtEncoder,
                usersRepo,
                userShopAccessRepo,
                shopsRepo);
    }

    @Test
    void generateJwtToken_Success() {
        String uuid = "test-uuid";
        String username = "test-user";
        Long userId = 1L;
        Long shopId = 100L;
        Integer shopCode = 12345;

        Authentication authentication = mock(Authentication.class);
        UserAccounts principal = mock(UserAccounts.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getUuid()).thenReturn(uuid);
        when(authentication.getName()).thenReturn(username);

        Users user = new Users();
        user.setId(userId);
        when(usersRepo.findByUuid(uuid)).thenReturn(Optional.of(user));

        UserShopAccess access = new UserShopAccess();
        access.setRole(Roles.ADMIN);
        access.setShopId(shopId);
        when(userShopAccessRepo.findByUserId(userId)).thenReturn(List.of(access));

        Shops shop = new Shops(null, null, null, null, null, shopCode);
        when(shopsRepo.findById(shopId)).thenReturn(Optional.of(shop));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("generated-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        // Act
        String token = jwtTokenService.generateJwtToken(authentication);

        // Assert
        assertThat(token).isEqualTo("generated-token");

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        JwtClaimsSet claims = captor.getValue().getClaims();

        assertThat((Object) claims.getClaim(SUBJECT)).isEqualTo(username);
        assertThat((Object) claims.getClaim(UUID)).isEqualTo(uuid);
        assertThat((Object) claims.getClaim(TENANT_ID)).isEqualTo(shopCode);
        assertThat((Object) claims.getClaim(SCOPE)).isEqualTo(Roles.ADMIN);
    }

    @Test
    void generateJwtToken_UserNotFound() {
        // Arrange
        String uuid = "test-uuid";
        Authentication authentication = mock(Authentication.class);
        UserAccounts principal = mock(UserAccounts.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getUuid()).thenReturn(uuid);

        when(usersRepo.findByUuid(uuid)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jwtTokenService.generateJwtToken(authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Can't find user with uuid");
    }

    @Test
    void generateJwtToken_NoShopAccess() {
        // Arrange
        String uuid = "test-uuid";
        Long userId = 1L;

        Authentication authentication = mock(Authentication.class);
        UserAccounts principal = mock(UserAccounts.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getUuid()).thenReturn(uuid);

        Users user = new Users();
        user.setId(userId);
        when(usersRepo.findByUuid(uuid)).thenReturn(Optional.of(user));

        when(userShopAccessRepo.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThatThrownBy(() -> jwtTokenService.generateJwtToken(authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User test-uuid has no shop access");
    }

    @Test
    void generateJwtToken_ShopNotFound() {
        // Arrange
        String uuid = "test-uuid";
        Long userId = 1L;
        Long shopId = 100L;

        Authentication authentication = mock(Authentication.class);
        UserAccounts principal = mock(UserAccounts.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getUuid()).thenReturn(uuid);

        Users user = new Users();
        user.setId(userId);
        when(usersRepo.findByUuid(uuid)).thenReturn(Optional.of(user));

        UserShopAccess access = new UserShopAccess();
        access.setShopId(shopId);
        when(userShopAccessRepo.findByUserId(userId)).thenReturn(List.of(access));

        when(shopsRepo.findById(shopId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jwtTokenService.generateJwtToken(authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Shop not found for ID: 100");
    }
}
