package in.lekhai.authentication.service;

import in.lekhai.authentication.dto.LoginResponse;
import in.lekhai.authentication.dto.ShopMenu;
import in.lekhai.authentication.entity.UserAccounts;
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
    private final UserShopAccessRepo userShopAccessRepo;
    private final ShopsRepo shopsRepo;
    private final SuperAdminDetailsRepo superAdminDetailsRepo;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            UsersRepo usersRepo,
            UserShopAccessRepo userShopAccessRepo,
            SuperAdminDetailsRepo superAdminDetailsRepo,
            ShopsRepo shopsRepo) {
        this.jwtEncoder = jwtEncoder;
        this.usersRepo = usersRepo;
        this.userShopAccessRepo = userShopAccessRepo;
        this.superAdminDetailsRepo = superAdminDetailsRepo;
        this.shopsRepo = shopsRepo;
    }

    public LoginResponse generateJwtToken(Authentication authentication) {
        UserAccounts principal = (UserAccounts) authentication.getPrincipal();
        String uuid = principal.getUuid();
        /*
            if found in superAdminDetails --> return a token for superAdmin with shopCode = -1
         */
        Optional<SuperAdminDetails> superAdminOptional = superAdminDetailsRepo.findByUuid(uuid);
        if (superAdminOptional.isPresent()) {
            LoginResponse response = new LoginResponse(
                    null,
                    generateJwtToken(Roles.SUPER_ADMIN, uuid, -1, authentication)
            );
            log.info("Generated Token successfully for uuid : {} as {}", uuid, Roles.SUPER_ADMIN);
            return response;
        }

        Users user = usersRepo.findByUuid(uuid)
                .orElseThrow(() -> {
                            log.error("Unexpected error, Neither user nor superAdmin entry found for uuid : {}", uuid);
                            return new RuntimeException(String.format("Can't find any user with uuid : %s", uuid));
                        }
                );

        List<UserShopAccess> shopAccessList = userShopAccessRepo.findByUserId(user.getId());
        if (shopAccessList.isEmpty()) {
            log.error("Unexpected error, No shop found for uuid : {}", uuid);
            throw new RuntimeException(String.format("No shop found for uuid : %s", uuid));
        }

        // case 1 : Only one shop
        if (shopAccessList.size() == 1) {
            UserShopAccess userShopAccess = shopAccessList.getFirst();
            return buildLoginResponseWithToken(userShopAccess, user, authentication);
        }

        // case 2: multiple shop + default shop
        Optional<UserShopAccess> defaultShop = shopAccessList.stream()
                .filter(UserShopAccess::getSelectedAsDefault)
                .findFirst();

        if (defaultShop.isPresent()) {
            return buildLoginResponseWithToken(defaultShop.get(), user, authentication);
        }

        //case 3: multiple shop + none as default
        return new LoginResponse(
                shopAccessList.stream()
                        .map(this::toShopMenu)
                        .toList(),
                null
        );
    }

    public LoginResponse generateJwtToken(Authentication authentication, Integer shopCode) {
        UserAccounts principal = (UserAccounts) authentication.getPrincipal();
        String uuid = principal.getUuid();
        Users user = usersRepo.findByUuid(uuid)
                .orElseThrow(() -> {
                            log.error("Unexpected error, User entry found for uuid : {}", uuid);
                            return new RuntimeException(String.format("Can't find any user with uuid : %s", uuid));
                        }
                );
        Shops shop = shopsRepo.findByShopCode(shopCode)
                // TODO : Custom exception
                .orElseThrow(() -> new LekhaiException("No shop found for the specified shopCode"));
        UserShopAccess userShopAccess = userShopAccessRepo.findByUserIdAndShopId(user.getId(), shop.getId())
                .orElseThrow(() -> new RuntimeException("Unexpected, userId: " + user.getId() +
                        "and shopId: " + shop.getId() + "entry should exits in userShopAccess")
                );
        return new LoginResponse(null,
                generateJwtToken(userShopAccess.getRole(), uuid, shopCode, authentication)
        );
    }

    private String generateJwtToken(Roles role, String uuid, Integer shopCode, Authentication authentication) {
        Instant now = Instant.now();
        JwtClaimsSet claim = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(JWT_EXPIRY, ChronoUnit.HOURS))
                .claim(SCOPE, role)
                .claim(SUBJECT, authentication.getName())
                .claim(UUID, uuid)
                .claim(SHOP_CODE, shopCode)
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claim)).getTokenValue();
    }

    private LoginResponse buildLoginResponseWithToken(
            UserShopAccess userShopAccess,
            Users user,
            Authentication authentication
    ) {
        Integer shopCode = shopsRepo.findById(userShopAccess.getShopId())
                .orElseThrow(() -> {
                    log.error("Unexpected error, No userShopAccess found with Id : {}", userShopAccess.getShopId());
                    return new RuntimeException("Shop not found");
                })
                .getShopCode();

        return new LoginResponse(
                null,
                generateJwtToken(
                        userShopAccess.getRole(),
                        user.getUuid(),
                        shopCode,
                        authentication
                )
        );
    }

    private ShopMenu toShopMenu(UserShopAccess userShopAccess) {
        Shops shop = shopsRepo.findById(userShopAccess.getShopId())
                .orElseThrow(() -> {
                    log.error("Unexpected error, No userShopAccess found with Id, while creating shopMenu: {}",
                            userShopAccess.getShopId());
                    return new RuntimeException("Shop not found");
                });
        return new ShopMenu(shop.getFirmName(), shop.getShopCode(), userShopAccess.getRole());
    }
}
