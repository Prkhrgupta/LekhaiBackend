package in.lekhai.core.service.tenant;

import in.lekhai.core.domain.admin.AdminDetails;
import in.lekhai.core.domain.tenant.TenantDetails;
import in.lekhai.core.dto.tenant.BaseTenantRequest;
import in.lekhai.core.dto.tenant.CreateTenantExistingAdminRequest;
import in.lekhai.core.dto.tenant.CreateTenantNewAdminRequest;
import in.lekhai.core.dto.tenant.TenantCreationResponse;
import in.lekhai.core.repository.admin.AdminDetailsRepo;
import in.lekhai.core.repository.tenant.TenantDetailsRepo;
import in.lekhai.core.service.admin.AdminService;
import in.lekhai.core.util.AdminUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {

    private final AdminService adminService;
    private final TenantDetailsRepo tenantDetailsRepo;
    private final AdminDetailsRepo adminDetailsRepo;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public TenantService(AdminService adminService,
                         TenantDetailsRepo tenantDetailsRepo,
                         AdminDetailsRepo adminDetailsRepo
    ) {
        this.adminService = adminService;
        this.tenantDetailsRepo = tenantDetailsRepo;
        this.adminDetailsRepo = adminDetailsRepo;
    }

    @Transactional
    public TenantCreationResponse createTenantWithExistingAdmin(CreateTenantExistingAdminRequest request) {
        AdminDetails admin = adminDetailsRepo.findByUuid(request.adminUuid())
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + request.adminUuid()));

        return createTenantInternal(admin, request);
    }

    @Transactional
    public TenantCreationResponse createTenantWithNewAdmin(CreateTenantNewAdminRequest request) {
        Integer tenantId = AdminUtils.createTenant();
        String adminUuid = adminService.registerAdmin(request.admin(), tenantId).uuid();

        AdminDetails admin = adminDetailsRepo.findByUuid(adminUuid)
                .orElseThrow(() -> new IllegalStateException("Admin creation failed"));

        return createTenantInternal(admin, request, tenantId);
    }

    private TenantCreationResponse createTenantInternal(AdminDetails admin, BaseTenantRequest request) {
        Integer tenantId = AdminUtils.createTenant();
        return createTenantInternal(admin, request, tenantId);
    }

    private TenantCreationResponse createTenantInternal(AdminDetails admin,
                                                        BaseTenantRequest request,
                                                        Integer tenantId) {
        TenantDetails tenant = new TenantDetails(
                admin.getUuid(),
                admin.getCategoryId(),
                request.isDefault(),
                request.gstIn(),
                request.firmName(),
                request.address(),
                tenantId
        );
        TenantDetails saved = tenantDetailsRepo.save(tenant);
        return new TenantCreationResponse(
                saved.getTenant(), saved.getFirmName(), saved.getGstIn(), saved.getRegisteredAddress()
        );
    }
}
