package in.lekhai.authentication.service;

import in.lekhai.authentication.entity.UserCredentials;
import in.lekhai.core.entity.SuperAdminMaster;
import in.lekhai.core.entity.TenantDetails;
import in.lekhai.core.entity.UserDetails;
import in.lekhai.core.model.enums.Roles;
import in.lekhai.core.repository.SuperAdminMasterRepo;
import in.lekhai.core.repository.TenantDetailsRepo;
import in.lekhai.core.repository.UserDetailsRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JwtTokenService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final JwtEncoder jwtEncoder;
    private final TenantDetailsRepo tenantDetailsRepo;
    private final UserDetailsRepo userDetailsRepo;
    private final SuperAdminMasterRepo superAdminMasterRepo;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            TenantDetailsRepo tenantDetailsRepo,
            UserDetailsRepo userDetailsRepo,
            SuperAdminMasterRepo superAdminMasterRepo
    ) {
        this.jwtEncoder = jwtEncoder;
        this.tenantDetailsRepo = tenantDetailsRepo;
        this.userDetailsRepo = userDetailsRepo;
        this.superAdminMasterRepo = superAdminMasterRepo;
    }

    public String generateJwtToken(Authentication authentication) {
        Instant now = Instant.now();

        UserCredentials principal = (UserCredentials) authentication.getPrincipal();
        String uuid = principal.getUuid();
        Roles role = findRole(uuid);

        JwtClaimsSet claim = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(6, ChronoUnit.HOURS))
                .claim("scope", role)
                .claim("subject", authentication.getName())
                .claim("uuid", uuid)
                .claim("tenant", findTenantId(uuid, role))
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claim)).getTokenValue();
    }

    private Roles findRole(String uuid) {
        return superAdminMasterRepo.findByUuid(uuid).map(u -> Roles.SUPER_ADMIN)
                .or(() -> tenantDetailsRepo.findByUuid(uuid).map(u -> Roles.ADMIN))
                .or(() -> userDetailsRepo.findByUuid(uuid).map(UserDetails::getRole))
                .orElseThrow(() -> {
                            log.error("uuid : {} doesn't exists in any of the user tables, Failed to generate JWT",uuid);
                            return new UsernameNotFoundException(
                                    String.format("User doesn't exists can't create JWT for uuid : %s", uuid
                                    ));
                        }
                );
    }

    private Integer findTenantId(String uuid, Roles role) {
        if(Roles.SUPER_ADMIN.equals(role)) return 0; // Return 0 for SUPERADMIN, This will be handled in JwtClaims.java

        if(Roles.ADMIN.equals(role)) {
            return tenantDetailsRepo.findByUuid(uuid)
                    .map(TenantDetails::getTenant)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            String.format("Missing entry for Role ADMIN in tenant_details, uuid : %s", uuid)
                    ));
        } else {
            return userDetailsRepo.findByUuid(uuid)
                    .map(UserDetails::getTenant)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            String.format("Missing entry for Role %s in tenant_details, uuid : %s", role, uuid)
                    ));
        }
    }
}
