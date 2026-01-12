package in.lekhai.authentication.service;

import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.core.domain.users.Users;
import in.lekhai.core.repository.users.UsersRepo;
import in.lekhai.error.controller.shop.exception.TenantDoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static in.lekhai.common.JwtConstants.*;

@Service
public class JwtTokenService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final JwtEncoder jwtEncoder;
    private final UsersRepo usersRepo;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            UsersRepo usersRepo
    ) {
        this.jwtEncoder = jwtEncoder;
        this.usersRepo = usersRepo;
    }

    public String generateJwtToken(Authentication authentication) {
        Instant now = Instant.now();

        UserAccounts principal = (UserAccounts) authentication.getPrincipal();
        String uuid = principal.getUuid();
        Users user = findTenant(uuid);

        JwtClaimsSet claim = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(JWT_EXPIRY, ChronoUnit.HOURS))
                .claim(SCOPE, user.getRole())
                .claim(SUBJECT, authentication.getName())
                .claim(UUID, uuid)
                .claim(SHOP_CODE, user.getShopCode())
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claim)).getTokenValue();
    }

    @Transactional
    private Users findTenant(String uuid) {
        return usersRepo.findByUuid(uuid).orElseThrow(() -> new TenantDoesNotExistException(uuid));
    }
}
