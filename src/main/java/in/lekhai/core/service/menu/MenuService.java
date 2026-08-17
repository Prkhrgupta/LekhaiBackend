package in.lekhai.core.service.menu;

import in.lekhai.contract.model.MenuResponse;
import in.lekhai.contract.model.TopBarResponse;
import in.lekhai.core.domain.category.Categories;
import in.lekhai.core.domain.category.RolePermissions;
import in.lekhai.core.domain.feature.Features;
import in.lekhai.core.domain.shop.Shops;
import in.lekhai.core.domain.users.UserShopAccess;
import in.lekhai.core.domain.users.Users;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.category.CategoriesRepo;
import in.lekhai.core.repository.category.RolePermissionsRepo;
import in.lekhai.core.repository.feature.FeaturesRepo;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.core.repository.users.UserShopAccessRepo;
import in.lekhai.core.repository.users.UsersRepo;
import in.lekhai.core.service.feature.FeatureMapService;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.core.util.PermissionBitCalculator;
import in.lekhai.error.controller.category.exception.CategoryDoesNotExistException;
import in.lekhai.error.controller.role.exception.RoleForCategoryDoesNotExistException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class MenuService {

        private final UsersRepo usersRepo;
        private final CategoriesRepo categoriesRepo;
        private final RolePermissionsRepo rolePermissionsRepo;
        private final MenuBuilder menuBuilder;
        private final PermissionBitCalculator permissionBitCalculator;
        private final FeatureMapService featureMapService;
        private final FeaturesRepo featuresRepo;
        private final UserShopAccessRepo userShopAccessRepo;
        private final ShopsRepo shopsRepo;

        public MenuService(UsersRepo usersRepo,
                           CategoriesRepo categoriesRepo,
                           RolePermissionsRepo rolePermissionsRepo,
                           MenuBuilder menuBuilder,
                           PermissionBitCalculator permissionBitCalculator,
                           FeatureMapService featureMapService,
                           FeaturesRepo featuresRepo,
                           UserShopAccessRepo userShopAccessRepo,
                           ShopsRepo shopsRepo) {
                this.usersRepo = usersRepo;
                this.categoriesRepo = categoriesRepo;
                this.rolePermissionsRepo = rolePermissionsRepo;
                this.menuBuilder = menuBuilder;
                this.permissionBitCalculator = permissionBitCalculator;
                this.featureMapService = featureMapService;
                this.featuresRepo = featuresRepo;
                this.userShopAccessRepo = userShopAccessRepo;
                this.shopsRepo = shopsRepo;
        }

        public MenuResponse generateMenu() {
                Roles role = JwtUtil.extractJwtClaim().role();
                if (role == Roles.SUPER_ADMIN) {
                        List<Features> superAdminFeatures = featuresRepo.findSuperAdminLeafFeatures();
                        return menuBuilder.buildMenu(superAdminFeatures);
                }
                String uuid = JwtUtil.extractJwtClaim().uuid();

                Users userEntity = usersRepo.findByUuid(uuid)
                                .orElseThrow(() -> new RuntimeException(
                                                String.format("UnException exception, uuid : %s not found. But extracted from JWT",
                                                                uuid)));

                UserShopAccess userShopAccess = userShopAccessRepo.findByUserId(userEntity.getId()).stream()
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("No shop access found for user"));

                Integer categoryId = userEntity.getCategoryId();

                Categories categories = categoriesRepo
                                .findById(categoryId)
                                .orElseThrow(() -> new CategoryDoesNotExistException(String.valueOf(categoryId)));

                RolePermissions rolePermissions = rolePermissionsRepo
                                .findByCategoryIdAndRoleId(categoryId, role)
                                .orElseThrow(() -> new RoleForCategoryDoesNotExistException(role, categoryId));

                List<Long> finalPermissionBits = permissionBitCalculator.calculateFinalPermissions(
                                categories.getPermissions(),
                                rolePermissions.getPermissions(),
                                userShopAccess.getPermissions(),
                                role);

                Set<Integer> enabledBitPositions = permissionBitCalculator
                                .extractEnabledBitPositions(finalPermissionBits);
                List<Features> enabledRootFeatures = featureMapService.getFeaturesByBitPositions(enabledBitPositions);

                return menuBuilder.buildMenu(enabledRootFeatures);
        }

        public TopBarResponse generateTopBar() {
            Roles role = JwtUtil.extractJwtClaim().role();
            if (role == Roles.SUPER_ADMIN) {
                TopBarResponse response = new TopBarResponse();
                response.setFirmName("SuperAdmin Console");
                response.setGstin("N/A");
                response.setName("Super Admin");
                return response;
            }
            Integer shopCode = JwtUtil.extractJwtClaim().shopCode();
            String uuid = JwtUtil.extractJwtClaim().uuid();
            Optional<Shops> shop = shopsRepo.findByShopCode(shopCode);
            if(shop.isEmpty()) {
                throw new RuntimeException(String.format("Can't find shop with shopCode : %s", shopCode));
            }
            Optional<Users> user = usersRepo.findByUuid(uuid);
            if(user.isEmpty()) {
                throw new RuntimeException(String.format("Can't find user with uuid : %s", uuid));
            }

            TopBarResponse response = new TopBarResponse();
            response.setFirmName(shop.get().getFirmName());
            response.setGstin(shop.get().getGstNumber());
            response.setName(user.get().getName());

            return response;
        }
}
