package in.lekhai.core.service.shop;

import in.lekhai.core.account_master.repository.StateRepository;
import in.lekhai.core.domain.shop.Shops;
import in.lekhai.core.domain.users.UserShopAccess;
import in.lekhai.core.domain.users.Users;
import in.lekhai.core.dto.shop.BaseShopRequest;
import in.lekhai.core.dto.shop.CreateShopExistingAdminRequest;
import in.lekhai.core.dto.shop.CreateShopNewAdminRequest;
import in.lekhai.core.dto.shop.ShopCreationResponse;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.category.CategoriesRepo;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.core.repository.users.UserShopAccessRepo;
import in.lekhai.core.repository.users.UsersRepo;
import in.lekhai.core.service.admin.AdminService;
import in.lekhai.core.util.AdminUtils;
import in.lekhai.error.controller.LekhaiClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShopService {

    private final AdminService adminService;
    private final ShopsRepo shopRepo;
    private final UsersRepo userRepo;
    private final UserShopAccessRepo userShopAccessRepo;
    private final CategoriesRepo categoriesRepo;
    private final StateRepository stateRepository;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public ShopService(AdminService adminService,
            ShopsRepo shopRepo,
            UsersRepo userRepo,
            UserShopAccessRepo userShopAccessRepo,
            CategoriesRepo categoriesRepo,
            StateRepository stateRepository) {
        this.adminService = adminService;
        this.shopRepo = shopRepo;
        this.userRepo = userRepo;
        this.userShopAccessRepo = userShopAccessRepo;
        this.categoriesRepo = categoriesRepo;
        this.stateRepository = stateRepository;
    }

    @Transactional
    public ShopCreationResponse createTenantWithExistingAdmin(CreateShopExistingAdminRequest request) {
        Users admin = userRepo.findByUuid(request.adminUuid())
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + request.adminUuid()));

        return createShop(admin, request);
    }

    @Transactional
    public ShopCreationResponse createTenantWithNewAdmin(CreateShopNewAdminRequest request) {
        log.info("Request to create new shop : {}", request);
        Integer shopCode = AdminUtils.createShopCode();
        String adminUuid = adminService.registerAdmin(request.admin(), shopCode).uuid();

        Users admin = userRepo.findByUuid(adminUuid)
                .orElseThrow(() -> new IllegalStateException("Admin creation failed"));

        return createShop(admin, request, shopCode);
    }

    private ShopCreationResponse createShop(Users admin, BaseShopRequest request) {
        Integer tenantId = AdminUtils.createShopCode();
        return createShop(admin, request, tenantId);
    }

    private ShopCreationResponse createShop(Users admin,
                                            BaseShopRequest request,
                                            Integer shopCode) {
        Shops shop = new Shops(
                admin.getCategoryId(),
                Boolean.TRUE,
                request.gstIn(),
                request.firmName(),
                request.address(),
                shopCode);
        shop.setStateCode(resolveStateCode(request));

        Shops savedShop = shopRepo.save(shop);

        List<Long> permissions = categoriesRepo.findById(admin.getCategoryId())
                .map(category -> category.getPermissions())
                .orElse(new ArrayList<>());

        UserShopAccess adminShopMapping = new UserShopAccess(
                admin.getId(),
                savedShop.getId(),
                Roles.SHOP_OWNER,
                request.isDefault(),
                permissions);
        userShopAccessRepo.save(adminShopMapping);
        return new ShopCreationResponse(
                savedShop.getShopCode(),
                savedShop.getFirmName(),
                savedShop.getGstNumber(),
                savedShop.getRegisteredAddress());
    }

    /**
     * Explicit state wins; otherwise derive it from the GSTIN's 2-digit prefix so existing flows keep working.
     */
    private String resolveStateCode(BaseShopRequest request) {
        if (request.stateCode() != null && !request.stateCode().isBlank()) {
            return stateRepository.findById(request.stateCode())
                    .orElseThrow(() -> new LekhaiClientException(
                            "Unknown state code: " + request.stateCode(), HttpStatus.BAD_REQUEST))
                    .getStateCode();
        }
        String gstIn = request.gstIn();
        if (gstIn == null || gstIn.length() < 2) {
            return null;
        }
        return stateRepository.findByGstCode(gstIn.substring(0, 2))
                .map(state -> state.getStateCode())
                .orElse(null);
    }
}
