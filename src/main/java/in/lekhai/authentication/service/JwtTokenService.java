package in.lekhai.authentication.service;

import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.core.enums.Roles;
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
        Roles role = usersRepo.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException(String.format("Can't find user with uuid : %s", uuid)))
                .getRole();

        JwtClaimsSet claim = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(6, ChronoUnit.HOURS))
                .claim(SCOPE, role)
                .claim(SUBJECT, authentication.getName())
                .claim(UUID, uuid)
                .claim(TENANT_ID, findTenantId(uuid))
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claim)).getTokenValue();
    }

    private Integer findTenantId(String uuid) {
        return usersRepo.findByUuid(uuid)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Missing entry for Role ADMIN in tenant_details, adminUuid : %s", uuid)
                ))
                .getShopCode();
    }
}
