package in.lekhai.authentication.service;

import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.contract.model.LoginResponse;
import in.lekhai.contract.model.ShopMenu;
import in.lekhai.core.domain.shop.Shops;
import in.lekhai.core.domain.superadmin.SuperAdminDetails;
import in.lekhai.core.domain.users.UserShopAccess;
import in.lekhai.core.domain.users.Users;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.core.repository.superadmin.SuperAdminDetailsRepo;
import in.lekhai.core.repository.users.UserShopAccessRepo;
import in.lekhai.core.repository.users.UsersRepo;
import in.lekhai.error.controller.LekhaiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.mockito.ArgumentCaptor;
import java.util.Map;

import static in.lekhai.common.JwtConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private UsersRepo usersRepo;
    @Mock
    private UserService userService;
    @Mock
    private UserShopAccessRepo userShopAccessRepo;
    @Mock
    private ShopsRepo shopsRepo;
    @Mock
    private SuperAdminDetailsRepo superAdminDetailsRepo;

    @InjectMocks
    private JwtTokenService jwtTokenService;




    private Authentication authentication;
    private UserAccounts userAccount;

    @BeforeEach
    void setUp() {
        authentication = mock(Authentication.class);
        userAccount = new UserAccounts();
        userAccount.setId(1L);
        userAccount.setUuid("test-uuid");
        userAccount.setUsername("testuser");
    }

    private void mockJwtEncoder() {
        org.springframework.security.oauth2.jwt.Jwt encodedJwt = mock(org.springframework.security.oauth2.jwt.Jwt.class);
        when(encodedJwt.getTokenValue()).thenReturn("mocked-jwt-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(encodedJwt);
    }

    @Test
    void testGenerateShopJwtToken_SuperAdmin() {
        when(authentication.getPrincipal()).thenReturn(userAccount);
        when(authentication.getName()).thenReturn("testuser");

        SuperAdminDetails superAdmin = new SuperAdminDetails();
        superAdmin.setId(1);
        superAdmin.setUuid("test-uuid");

        when(superAdminDetailsRepo.findByUuid("test-uuid")).thenReturn(Optional.of(superAdmin));
        mockJwtEncoder();

        LoginResponse response = jwtTokenService.generateShopJwtToken(authentication);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
        assertTrue(response.getShopMenu() == null || response.getShopMenu().isEmpty()); // Based on implementation, shopMenu isn't set for SUPER_ADMIN

        verify(superAdminDetailsRepo, times(1)).findByUuid("test-uuid");

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder, times(1)).encode(captor.capture());

        Map<String, Object> claims = captor.getValue().getClaims().getClaims();
        assertEquals(6, claims.size(), "Super Admin login token should have exactly 6 claims");
        assertTrue(claims.containsKey("iss"), "Missing issuer claim");
        assertTrue(claims.containsKey("iat"), "Missing issuedAt claim");
        assertTrue(claims.containsKey("exp"), "Missing expiresAt claim");
        assertTrue(claims.containsKey(SCOPE), "Missing scope claim");
        assertTrue(claims.containsKey(SUBJECT), "Missing subject claim");
        assertTrue(claims.containsKey(UUID), "Missing uuid claim");
    }

    @Test
    void testGenerateShopJwtToken_User_Success() {
        when(authentication.getPrincipal()).thenReturn(userAccount);
        when(authentication.getName()).thenReturn("testuser");
        when(superAdminDetailsRepo.findByUuid("test-uuid")).thenReturn(Optional.empty());

        Users user = new Users();
        user.setId(1L);
        user.setUuid("test-uuid");
        when(usersRepo.findByUuid("test-uuid")).thenReturn(Optional.of(user));

        UserShopAccess access = new UserShopAccess(1L, 100L, Roles.SHOP_OWNER, true, Collections.emptyList());
        when(userShopAccessRepo.findByUserId(1L)).thenReturn(List.of(access));

        Shops shop = new Shops(1, true, "GST", "Test Firm", "Address", 12345);
        ReflectionTestUtils.setField(shop, "id", 100L);
        when(shopsRepo.findById(100L)).thenReturn(Optional.of(shop));

        mockJwtEncoder();

        LoginResponse response = jwtTokenService.generateShopJwtToken(authentication);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
        assertNotNull(response.getShopMenu());
        assertEquals(1, response.getShopMenu().size());
        assertEquals("Test Firm", response.getShopMenu().get(0).getName());
        assertEquals(12345, response.getShopMenu().get(0).getShopCode());
        assertEquals(ShopMenu.RoleEnum.SHOP_OWNER, response.getShopMenu().get(0).getRole());

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder, times(1)).encode(captor.capture());

        Map<String, Object> claims = captor.getValue().getClaims().getClaims();
        assertEquals(6, claims.size(), "User login token should have exactly 6 claims");
        assertTrue(claims.containsKey("iss"), "Missing issuer claim");
        assertTrue(claims.containsKey("iat"), "Missing issuedAt claim");
        assertTrue(claims.containsKey("exp"), "Missing expiresAt claim");
        assertTrue(claims.containsKey(SCOPE), "Missing scope claim");
        assertTrue(claims.containsKey(SUBJECT), "Missing subject claim");
        assertTrue(claims.containsKey(UUID), "Missing uuid claim");
        assertFalse(claims.containsKey(SHOP_CODE), "Login token must not contain shop_code");
    }

    @Test
    void testGenerateShopJwtToken_User_NoShopAccess() {
        when(authentication.getPrincipal()).thenReturn(userAccount);
        when(superAdminDetailsRepo.findByUuid("test-uuid")).thenReturn(Optional.empty());
        Users user = new Users();
        user.setId(1L);
        when(usersRepo.findByUuid("test-uuid")).thenReturn(Optional.of(user));
        when(userShopAccessRepo.findByUserId(1L)).thenReturn(Collections.emptyList());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            jwtTokenService.generateShopJwtToken(authentication);
        });

        assertEquals("No shop found for uuid : test-uuid", exception.getMessage());
    }

    @Test
    void testGenerateShopJwtToken_UserNotFound() {
        when(authentication.getPrincipal()).thenReturn(userAccount);
        when(superAdminDetailsRepo.findByUuid("test-uuid")).thenReturn(Optional.empty());
        when(usersRepo.findByUuid("test-uuid")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            jwtTokenService.generateShopJwtToken(authentication);
        });

        assertEquals("Can't find any user with uuid : test-uuid", exception.getMessage());
    }

    @Test
    void testGenerateShopJwtToken_WithShopCode_Success() {
        Jwt jwt = mock(Jwt.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString(SUBJECT)).thenReturn("testuser");

        when(userService.loadUserByUsername("testuser")).thenReturn(userAccount);

        Users user = new Users();
        user.setId(1L);
        when(usersRepo.findByUuid("test-uuid")).thenReturn(Optional.of(user));

        Shops shop = new Shops(1, true, "GST", "Test Firm", "Address", 12345);
        ReflectionTestUtils.setField(shop, "id", 100L);
        when(shopsRepo.findByShopCode(12345)).thenReturn(Optional.of(shop));

        UserShopAccess access = new UserShopAccess(1L, 100L, Roles.SHOP_OWNER, true, Collections.emptyList());
        when(userShopAccessRepo.findByUserIdAndShopId(1L, 100L)).thenReturn(Optional.of(access));

        when(shopsRepo.findById(100L)).thenReturn(Optional.of(shop));

        mockJwtEncoder();

        LoginResponse response = jwtTokenService.generateShopJwtToken(authentication, 12345);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
        assertNotNull(response.getShopMenu());
        assertEquals(1, response.getShopMenu().size());
        assertEquals("Test Firm", response.getShopMenu().get(0).getName());

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder, times(1)).encode(captor.capture());

        Map<String, Object> claims = captor.getValue().getClaims().getClaims();
        assertEquals(7, claims.size(), "Shop token should have exactly 7 claims");
        assertTrue(claims.containsKey("iss"), "Missing issuer claim");
        assertTrue(claims.containsKey("iat"), "Missing issuedAt claim");
        assertTrue(claims.containsKey("exp"), "Missing expiresAt claim");
        assertTrue(claims.containsKey(SCOPE), "Missing scope claim");
        assertTrue(claims.containsKey(SUBJECT), "Missing subject claim");
        assertTrue(claims.containsKey(UUID), "Missing uuid claim");
        assertTrue(claims.containsKey(SHOP_CODE), "Missing shop_code claim");
        assertEquals(12345, claims.get(SHOP_CODE));
    }

    @Test
    void testGenerateShopJwtToken_WithShopCode_ShopNotFound() {
        Jwt jwt = mock(Jwt.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString(SUBJECT)).thenReturn("testuser");
        when(userService.loadUserByUsername("testuser")).thenReturn(userAccount);

        Users user = new Users();
        user.setId(1L);
        when(usersRepo.findByUuid("test-uuid")).thenReturn(Optional.of(user));

        when(shopsRepo.findByShopCode(12345)).thenReturn(Optional.empty());

        LekhaiException exception = assertThrows(LekhaiException.class, () -> {
            jwtTokenService.generateShopJwtToken(authentication, 12345);
        });

        assertEquals("No shop found for the specified shopCode", exception.getMessage());
    }

    @Test
    void testGenerateShopJwtToken_WithShopCode_UserAccessNotFound() {
        Jwt jwt = mock(Jwt.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString(SUBJECT)).thenReturn("testuser");
        when(userService.loadUserByUsername("testuser")).thenReturn(userAccount);

        Users user = new Users();
        user.setId(1L);
        when(usersRepo.findByUuid("test-uuid")).thenReturn(Optional.of(user));

        Shops shop = new Shops();
        ReflectionTestUtils.setField(shop, "id", 100L);
        when(shopsRepo.findByShopCode(12345)).thenReturn(Optional.of(shop));

        when(userShopAccessRepo.findByUserIdAndShopId(1L, 100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            jwtTokenService.generateShopJwtToken(authentication, 12345);
        });

        assertTrue(exception.getMessage().contains("entry should exits in userShopAccess"));
    }
}
