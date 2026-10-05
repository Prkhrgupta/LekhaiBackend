package in.lekhai.licensing.service;

import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.authentication.repository.UserAccountRepository;
import in.lekhai.core.domain.category.Categories;
import in.lekhai.core.domain.category.RolePermissions;
import in.lekhai.core.domain.shop.Shops;
import in.lekhai.core.domain.superadmin.SuperAdminDetails;
import in.lekhai.core.domain.users.UserShopAccess;
import in.lekhai.core.domain.users.Users;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.category.CategoriesRepo;
import in.lekhai.core.repository.category.RolePermissionsRepo;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.core.repository.superadmin.SuperAdminDetailsRepo;
import in.lekhai.core.repository.users.UserShopAccessRepo;
import in.lekhai.core.repository.users.UsersRepo;
import in.lekhai.core.util.AdminUtils;
import in.lekhai.core.util.PermissionBitCalculator;
import in.lekhai.licensing.domain.LicensePayload;
import in.lekhai.licensing.domain.ShopLicenseProfile;
import in.lekhai.licensing.domain.SystemLicense;
import in.lekhai.licensing.dto.ActivateLicenseRequest;
import in.lekhai.licensing.dto.LicenseStatusResponse;
import in.lekhai.licensing.dto.ShopQuotaStatus;
import in.lekhai.licensing.repository.SystemLicenseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class LicenseService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final SystemLicenseRepository systemLicenseRepository;
    private final LicenseCryptoService licenseCryptoService;
    private final ShopsRepo shopsRepo;
    private final CategoriesRepo categoriesRepo;
    private final RolePermissionsRepo rolePermissionsRepo;
    private final UserAccountRepository userAccountRepository;
    private final UsersRepo usersRepo;
    private final UserShopAccessRepo userShopAccessRepo;
    private final SuperAdminDetailsRepo superAdminDetailsRepo;
    private final PasswordEncoder passwordEncoder;
    private final PermissionBitCalculator permissionBitCalculator;
    private final in.lekhai.core.repository.feature.FeaturesRepo featuresRepo;

    public LicenseService(
            SystemLicenseRepository systemLicenseRepository,
            LicenseCryptoService licenseCryptoService,
            ShopsRepo shopsRepo,
            CategoriesRepo categoriesRepo,
            RolePermissionsRepo rolePermissionsRepo,
            UserAccountRepository userAccountRepository,
            UsersRepo usersRepo,
            UserShopAccessRepo userShopAccessRepo,
            SuperAdminDetailsRepo superAdminDetailsRepo,
            PasswordEncoder passwordEncoder,
            PermissionBitCalculator permissionBitCalculator,
            in.lekhai.core.repository.feature.FeaturesRepo featuresRepo) {
        this.systemLicenseRepository = systemLicenseRepository;
        this.licenseCryptoService = licenseCryptoService;
        this.shopsRepo = shopsRepo;
        this.categoriesRepo = categoriesRepo;
        this.rolePermissionsRepo = rolePermissionsRepo;
        this.userAccountRepository = userAccountRepository;
        this.usersRepo = usersRepo;
        this.userShopAccessRepo = userShopAccessRepo;
        this.superAdminDetailsRepo = superAdminDetailsRepo;
        this.passwordEncoder = passwordEncoder;
        this.permissionBitCalculator = permissionBitCalculator;
        this.featuresRepo = featuresRepo;
    }

    public Optional<SystemLicense> getActiveLicense() {
        return systemLicenseRepository.findByIsActiveTrue();
    }

    public Optional<LicensePayload> getActiveLicensePayload() {
        return getActiveLicense().map(lic -> licenseCryptoService.parsePayload(lic.getRawPayload()));
    }

    public boolean isLicensed() {
        return getActiveLicense().isPresent();
    }

    public boolean isReadOnlyMode() {
        Optional<SystemLicense> activeOpt = getActiveLicense();
        if (activeOpt.isEmpty()) {
            return false; // Not yet licensed or initial setup
        }
        SystemLicense lic = activeOpt.get();
        if (lic.getExpiresAt() != null && LocalDateTime.now().isAfter(lic.getExpiresAt())) {
            return true;
        }
        return false;
    }

    public LicenseStatusResponse getLicenseStatus() {
        Optional<SystemLicense> activeOpt = getActiveLicense();
        if (activeOpt.isEmpty()) {
            return LicenseStatusResponse.unauthenticated();
        }

        SystemLicense activeLicense = activeOpt.get();
        LicensePayload payload = licenseCryptoService.parsePayload(activeLicense.getRawPayload());

        boolean isExpired = payload.isExpired();
        Long daysRemaining = null;
        if (payload.expiresAt() != null) {
            daysRemaining = Math.max(0, Duration.between(LocalDateTime.now(), payload.expiresAt()).toDays());
        }

        List<ShopQuotaStatus> shopStatuses = new ArrayList<>();
        if (payload.shops() != null) {
            for (ShopLicenseProfile shopProfile : payload.shops()) {
                Optional<Shops> shopOpt = shopsRepo.findByShopCode(shopProfile.shopCode());
                Long currentUsers = 0L;
                Long shopId = null;
                if (shopOpt.isPresent()) {
                    shopId = shopOpt.get().getId();
                    currentUsers = userShopAccessRepo.countByShopId(shopId);
                }

                int maxUsers = shopProfile.maxUsers() != null ? shopProfile.maxUsers() : 1;
                boolean canAdd = currentUsers < maxUsers && !isExpired;

                shopStatuses.add(new ShopQuotaStatus(
                        shopId,
                        shopProfile.shopCode(),
                        shopProfile.firmName(),
                        shopProfile.gstNumber(),
                        maxUsers,
                        currentUsers,
                        canAdd
                ));
            }
        }

        return new LicenseStatusResponse(
                true,
                payload.licenseId(),
                payload.issuedTo(),
                payload.adminUsername(),
                payload.issuedAt(),
                payload.expiresAt(),
                daysRemaining,
                isExpired,
                isExpired,
                shopStatuses,
                payload.features() != null ? payload.features() : Map.of()
        );
    }

    @Transactional
    public LicenseStatusResponse activateLicense(ActivateLicenseRequest request) {
        // 1. Verify digital signature
        boolean isValidSignature = licenseCryptoService.verifySignature(request.licenseData(), request.signature());
        if (!isValidSignature) {
            throw new IllegalArgumentException("Invalid cryptographic license signature. Tampering detected.");
        }

        // 2. Parse payload
        LicensePayload payload = licenseCryptoService.parsePayload(request.licenseData());
        if (payload.isExpired()) {
            throw new IllegalArgumentException("Cannot activate an expired license. Expiry: " + payload.expiresAt());
        }

        // 3. Deactivate old licenses
        systemLicenseRepository.findAll().forEach(lic -> {
            lic.setActive(false);
            systemLicenseRepository.save(lic);
        });

        // 4. Save new active license
        SystemLicense newLicense = new SystemLicense(
                payload.licenseId(),
                payload.issuedTo(),
                payload.adminUsername(),
                request.licenseData(),
                request.signature(),
                true,
                payload.issuedAt(),
                payload.expiresAt()
        );
        systemLicenseRepository.save(newLicense);

        // Collect all available feature bit positions
        Set<Integer> allFeatureBits = new HashSet<>();
        featuresRepo.findAll().forEach(f -> {
            if (f.getBitPosition() != null) {
                allFeatureBits.add(f.getBitPosition());
            }
        });
        if (allFeatureBits.isEmpty()) {
            for (int i = 0; i <= 63; i++) {
                allFeatureBits.add(i);
            }
        }

        // 5. Provision licensed shops & setup category permissions
        List<Shops> provisionedShops = new ArrayList<>();
        Categories primaryCategory = null;

        if (payload.shops() != null && !payload.shops().isEmpty()) {
            for (ShopLicenseProfile shopProfile : payload.shops()) {
                String catName = shopProfile.categoryName() != null ? shopProfile.categoryName() : "SAREE";
                Categories category = categoriesRepo.findByName(catName)
                        .orElseGet(() -> categoriesRepo.save(Categories.builder().name(catName).build()));

                if (category.getPermissions() == null) {
                    category.setPermissions(new ArrayList<>());
                }
                permissionBitCalculator.enableBits(category.getPermissions(), allFeatureBits);
                final Integer currentCatId = category.getId();
                Optional<RolePermissions> rolePermOpt = rolePermissionsRepo.findByCategoryIdAndRoleId(currentCatId, Roles.SHOP_OWNER);
                RolePermissions rolePerm = rolePermOpt.orElseGet(() -> new RolePermissions(Roles.SHOP_OWNER, currentCatId));
                if (rolePerm.getPermissions() == null) {
                    rolePerm.setPermissions(new ArrayList<>());
                }
                permissionBitCalculator.enableBits(rolePerm.getPermissions(), allFeatureBits);
                rolePermissionsRepo.save(rolePerm);

                if (primaryCategory == null) {
                    primaryCategory = category;
                }

                Optional<Shops> existingShop = shopsRepo.findByShopCode(shopProfile.shopCode());
                Shops shop;
                if (existingShop.isPresent()) {
                    shop = existingShop.get();
                } else {
                    shop = new Shops(
                            category.getId(),
                            true,
                            shopProfile.gstNumber(),
                            shopProfile.firmName(),
                            shopProfile.registeredAddress(),
                            shopProfile.shopCode()
                    );
                    shop = shopsRepo.save(shop);
                }
                provisionedShops.add(shop);
            }
        }

        if (primaryCategory == null) {
            primaryCategory = categoriesRepo.findByName("SAREE")
                    .orElseGet(() -> categoriesRepo.save(Categories.builder().name("SAREE").build()));
            if (primaryCategory.getPermissions() == null) {
                primaryCategory.setPermissions(new ArrayList<>());
            }
            permissionBitCalculator.enableBits(primaryCategory.getPermissions(), allFeatureBits);
            primaryCategory = categoriesRepo.save(primaryCategory);
        }

        // 6. Provision / Update Admin Account as SHOP_OWNER
        String adminUsername = payload.adminUsername();
        String adminName = (request.adminName() != null && !request.adminName().isBlank())
                ? request.adminName()
                : payload.issuedTo();

        Optional<UserAccounts> existingAcc = userAccountRepository.findByUsername(adminUsername);
        String uuid;
        if (existingAcc.isPresent()) {
            UserAccounts acc = existingAcc.get();
            acc.setPassHash(passwordEncoder.encode(request.adminPassword()));
            userAccountRepository.save(acc);
            uuid = acc.getUuid();
        } else {
            uuid = AdminUtils.createUUID(Roles.SHOP_OWNER);
            UserAccounts newAcc = UserAccounts.builder()
                    .username(adminUsername)
                    .passHash(passwordEncoder.encode(request.adminPassword()))
                    .uuid(uuid)
                    .build();
            userAccountRepository.save(newAcc);
        }

        // If previously saved as super admin, remove from super_admin_details
        superAdminDetailsRepo.findByUuid(uuid).ifPresent(superAdminDetailsRepo::delete);

        final Integer primaryCatId = primaryCategory.getId();
        Users adminUser = usersRepo.findByUuid(uuid).orElseGet(() -> {
            Users u = Users.createUserWithCategory(uuid, adminName, primaryCatId);
            return usersRepo.save(u);
        });
        adminUser.setName(adminName);
        adminUser.setCategoryId(primaryCatId);
        adminUser = usersRepo.save(adminUser);

        // 7. Grant SHOP_OWNER access to all provisioned shops
        for (int i = 0; i < provisionedShops.size(); i++) {
            Shops s = provisionedShops.get(i);
            boolean isDefault = (i == 0);
            Optional<UserShopAccess> existingAccess = userShopAccessRepo.findByUserIdAndShopId(adminUser.getId(), s.getId());
            if (existingAccess.isEmpty()) {
                UserShopAccess access = new UserShopAccess(adminUser.getId(), s.getId(), Roles.SHOP_OWNER, isDefault, new ArrayList<>(primaryCategory.getPermissions()));
                userShopAccessRepo.save(access);
            } else {
                UserShopAccess access = existingAccess.get();
                access.setRole(Roles.SHOP_OWNER);
                access.setPermissions(new ArrayList<>(primaryCategory.getPermissions()));
                userShopAccessRepo.save(access);
            }
        }

        log.info("Successfully activated license {} for {} (Admin: {})", payload.licenseId(), payload.issuedTo(), adminUsername);
        return getLicenseStatus();
    }

    public void checkUserCreationQuota(Long shopId) {
        if (isReadOnlyMode()) {
            throw new IllegalStateException("License has expired. Application is in Read-Only Grace Mode.");
        }

        Optional<LicensePayload> payloadOpt = getActiveLicensePayload();
        if (payloadOpt.isEmpty()) {
            return; // Dev / unconfigured mode
        }

        LicensePayload payload = payloadOpt.get();
        Optional<Shops> shopOpt = shopsRepo.findById(shopId);
        if (shopOpt.isEmpty()) return;

        Shops shop = shopOpt.get();
        if (payload.shops() != null) {
            for (ShopLicenseProfile p : payload.shops()) {
                if (Objects.equals(p.shopCode(), shop.getShopCode())) {
                    long current = userShopAccessRepo.countByShopId(shopId);
                    int max = p.maxUsers() != null ? p.maxUsers() : 1;
                    if (current >= max) {
                        throw new IllegalStateException(String.format("User quota exceeded for shop '%s'. Max allowed: %d, Current: %d", shop.getFirmName(), max, current));
                    }
                }
            }
        }
    }
}
