package in.lekhai.authentication.service;

import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.core.domain.shop.Shops;
import in.lekhai.core.domain.users.UserShopAccess;
import in.lekhai.core.domain.users.Users;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.core.repository.users.UserShopAccessRepo;
import in.lekhai.core.repository.users.UsersRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static in.lekhai.common.JwtConstants.*;

@Service
public class JwtTokenService {

        private final Logger log = LoggerFactory.getLogger(this.getClass());

        private final JwtEncoder jwtEncoder;
        private final UsersRepo usersRepo;
        private final UserShopAccessRepo userShopAccessRepo;
        private final ShopsRepo shopsRepo;

        public JwtTokenService(
                        JwtEncoder jwtEncoder,
                        UsersRepo usersRepo,
                        UserShopAccessRepo userShopAccessRepo,
                        ShopsRepo shopsRepo) {
                this.jwtEncoder = jwtEncoder;
                this.usersRepo = usersRepo;
                this.userShopAccessRepo = userShopAccessRepo;
                this.shopsRepo = shopsRepo;
        }

        public String generateJwtToken(Authentication authentication) {
                Instant now = Instant.now();

                UserAccounts principal = (UserAccounts) authentication.getPrincipal();
                String uuid = principal.getUuid();

                Users user = usersRepo.findByUuid(uuid)
                                .orElseThrow(() -> new RuntimeException(
                                                String.format("Can't find user with uuid : %s", uuid)));

                UserShopAccess access = userShopAccessRepo.findByUserId(user.getId())
                                .stream().findFirst()
                                .orElseThrow(() -> new RuntimeException(
                                                String.format("User %s has no shop access", uuid)));

                Roles role = access.getRole();

                Shops shop = shopsRepo.findById(access.getShopId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Shop not found for ID: " + access.getShopId()));

                JwtClaimsSet claim = JwtClaimsSet.builder()
                                .issuer(ISSUER)
                                .issuedAt(now)
                                .expiresAt(now.plus(6, ChronoUnit.HOURS))
                                .claim(SCOPE, role)
                                .claim(SUBJECT, authentication.getName())
                                .claim(UUID, uuid)
                                .claim(TENANT_ID, shop.getShopCode())
                                .build();

                return this.jwtEncoder.encode(JwtEncoderParameters.from(claim)).getTokenValue();
        }
}
