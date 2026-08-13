package in.lekhai.authentication.service;

import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.authentication.model.JwtClaims;
import in.lekhai.common.util.FinancialYearDateUtil;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static in.lekhai.common.JwtConstants.*;

@Service
public class JwtTokenService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final JwtEncoder jwtEncoder;
    private final UsersRepo usersRepo;
    private final UserService userService;
    private final UserShopAccessRepo userShopAccessRepo;
    private final ShopsRepo shopsRepo;
    private final SuperAdminDetailsRepo superAdminDetailsRepo;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            UsersRepo usersRepo,
            UserService userService,
            UserShopAccessRepo userShopAccessRepo,
            SuperAdminDetailsRepo superAdminDetailsRepo,
            ShopsRepo shopsRepo) {
        this.jwtEncoder = jwtEncoder;
        this.usersRepo = usersRepo;
        this.userService = userService;
        this.userShopAccessRepo = userShopAccessRepo;
        this.superAdminDetailsRepo = superAdminDetailsRepo;
        this.shopsRepo = shopsRepo;
    }

    public LoginResponse generateShopJwtToken(Authentication authentication) {
        UserAccounts principal = (UserAccounts) authentication.getPrincipal();
        String uuid = principal.getUuid();

        Optional<SuperAdminDetails> superAdminOptional = superAdminDetailsRepo.findByUuid(uuid);
        if (superAdminOptional.isPresent()) {
            LoginResponse response = new LoginResponse();
            response.setToken(generateLoginJwtToken(Roles.SUPER_ADMIN, uuid, authentication));
            log.info("Generated Token successfully for uuid : {} as {}", uuid, Roles.SUPER_ADMIN);
            return response;
        }

        Users user = usersRepo.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Unexpected error, Neither user nor superAdmin entry found for uuid : {}", uuid);
                    return new RuntimeException(String.format("Can't find any user with uuid : %s", uuid));
                });

        List<UserShopAccess> shopAccessList = userShopAccessRepo.findByUserId(user.getId());
        if (shopAccessList.isEmpty()) {
            log.error("Unexpected error, No shop found for uuid : {}", uuid);
            throw new RuntimeException(String.format("No shop found for uuid : %s", uuid));
        }

        LoginResponse response = new LoginResponse();
        response.setToken(generateLoginJwtToken(Roles.USER, uuid, authentication));
        response.setShopMenu(shopAccessList.stream().map(this::toShopMenu).toList());
        return response;
    }

    public LoginResponse generateShopJwtToken(Authentication authentication, Integer shopCode) {
        String username = JwtClaims.getUsername((Jwt) authentication.getPrincipal());
        UserAccounts principal = userService.loadUserByUsername(username);
        String uuid = principal.getUuid();
        Users user = usersRepo.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Unexpected error, User entry found for uuid : {}", uuid);
                    return new RuntimeException(String.format("Can't find any user with uuid : %s", uuid));
                });
        Shops shop = shopsRepo.findByShopCode(shopCode)
                // TODO : Custom exception
                .orElseThrow(() -> new LekhaiException("No shop found for the specified shopCode"));
        UserShopAccess userShopAccess = userShopAccessRepo.findByUserIdAndShopId(user.getId(), shop.getId())
                .orElseThrow(() -> new RuntimeException("Unexpected, userId: " + user.getId() +
                        "and shopId: " + shop.getId() + "entry should exits in userShopAccess"));
        LoginResponse response = new LoginResponse();
        response.setToken(
                generateShopJwtToken(userShopAccess.getRole(), uuid, shopCode, username));
        response.setShopMenu(List.of(toShopMenu(userShopAccess)));
        return response;
    }

    private String generateLoginJwtToken(Roles role, String uuid, Authentication authentication) {
        Instant now = Instant.now();
        JwtClaimsSet claim = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(JWT_EXPIRY, ChronoUnit.HOURS))
                .claim(SCOPE, role)
                .claim(SUBJECT, authentication.getName())
                .claim(FY_START, FinancialYearDateUtil.getCurrentFinancialYear().toString())
                .claim(UUID, uuid)
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claim)).getTokenValue();
    }

    private String generateShopJwtToken(Roles role, String uuid, Integer shopCode, String username) {
        Instant now = Instant.now();
        JwtClaimsSet claim = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(JWT_EXPIRY, ChronoUnit.HOURS))
                .claim(SCOPE, role)
                .claim(SUBJECT, username)
                .claim(UUID, uuid)
                .claim(SHOP_CODE, shopCode)
                .claim(FY_START, FinancialYearDateUtil.getCurrentFinancialYear().toString())
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claim)).getTokenValue();
    }

    private ShopMenu toShopMenu(UserShopAccess userShopAccess) {
        Shops shop = shopsRepo.findById(userShopAccess.getShopId())
                .orElseThrow(() -> {
                    log.error("Unexpected error, No userShopAccess found with Id, while creating shopMenu: {}",
                            userShopAccess.getShopId());
                    return new RuntimeException("Shop not found");
                });
        ShopMenu shopMenu = new ShopMenu();
        shopMenu.setName(shop.getFirmName());
        shopMenu.setShopCode(shop.getShopCode());
        shopMenu.setRole(ShopMenu.RoleEnum.valueOf(userShopAccess.getRole().toString())); // REVIEW single enum two
                                                                                          // sources
        return shopMenu;
    }
}
