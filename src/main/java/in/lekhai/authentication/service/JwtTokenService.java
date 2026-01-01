package in.lekhai.authentication.service;

import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.core.domain.admin.AdminDetails;
import in.lekhai.core.domain.admin.UserInformation;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.admin.AdminDetailsRepo;
import in.lekhai.core.repository.admin.UserInformationRepo;
import in.lekhai.core.repository.superadmin.SuperAdminMasterRepo;
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
    private final AdminDetailsRepo adminDetailsRepo;
    private final UserInformationRepo userInformationRepo;
    private final SuperAdminMasterRepo superAdminMasterRepo;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            AdminDetailsRepo adminDetailsRepo,
            UserInformationRepo userInformationRepo,
            SuperAdminMasterRepo superAdminMasterRepo
    ) {
        this.jwtEncoder = jwtEncoder;
        this.adminDetailsRepo = adminDetailsRepo;
        this.userInformationRepo = userInformationRepo;
        this.superAdminMasterRepo = superAdminMasterRepo;
    }

    public String generateJwtToken(Authentication authentication) {
        Instant now = Instant.now();

        UserAccounts principal = (UserAccounts) authentication.getPrincipal();
        String uuid = principal.getUuid();
        Roles role = findRole(uuid);

        JwtClaimsSet claim = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(6, ChronoUnit.HOURS))
                .claim(SCOPE, role)
                .claim(SUBJECT, authentication.getName())
                .claim(UUID, uuid)
                .claim(TENANT_ID, findTenantId(uuid, role))
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claim)).getTokenValue();
    }

    private Roles findRole(String uuid) {
        return superAdminMasterRepo.findByUuid(uuid).map(u -> Roles.SUPER_ADMIN)
                .or(() -> adminDetailsRepo.findByUuid(uuid).map(u -> Roles.ADMIN))
                .or(() -> userInformationRepo.findByUuid(uuid).map(UserInformation::getRole))
                .orElseThrow(() -> {
                            log.error("adminUuid : {} doesn't exists in any of the user tables, Failed to generate JWT",uuid);
                            return new UsernameNotFoundException(
                                    String.format("User doesn't exists can't create JWT for adminUuid : %s", uuid
                                    ));
                        }
                );
    }

    private Integer findTenantId(String uuid, Roles role) {
        if(Roles.SUPER_ADMIN.equals(role)) return -1;

        if(Roles.ADMIN.equals(role)) {
            return adminDetailsRepo.findByUuid(uuid)
                    .map(AdminDetails::getTenant)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            String.format("Missing entry for Role ADMIN in tenant_details, adminUuid : %s", uuid)
                    ));
        } else {
            return userInformationRepo.findByUuid(uuid)
                    .map(UserInformation::getTenant)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            String.format("Missing entry for Role %s in tenant_details, adminUuid : %s", role, uuid)
                    ));
        }
    }
}
