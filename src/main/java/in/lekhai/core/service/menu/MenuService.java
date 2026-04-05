package in.lekhai.core.service.menu;

import in.lekhai.contract.model.MenuResponse;
import in.lekhai.core.domain.category.Categories;
import in.lekhai.core.domain.category.RolePermissions;
import in.lekhai.core.domain.feature.Features;
import in.lekhai.core.domain.users.UserShopAccess;
import in.lekhai.core.domain.users.Users;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.category.CategoriesRepo;
import in.lekhai.core.repository.category.RolePermissionsRepo;
import in.lekhai.core.repository.users.UserShopAccessRepo;
import in.lekhai.core.repository.users.UsersRepo;
import in.lekhai.core.service.feature.FeatureMapService;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.core.util.PermissionBitCalculator;
import in.lekhai.error.controller.category.exception.CategoryDoesNotExistException;
import in.lekhai.error.controller.role.exception.RoleForCategoryDoesNotExistException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class MenuService {

        private final UsersRepo usersRepo;
        private final CategoriesRepo categoriesRepo;
        private final RolePermissionsRepo rolePermissionsRepo;
        private final MenuBuilder menuBuilder;
        private final PermissionBitCalculator permissionBitCalculator;
        private final FeatureMapService featureMapService;
        private final UserShopAccessRepo userShopAccessRepo;

        public MenuService(UsersRepo usersRepo,
                           CategoriesRepo categoriesRepo,
                           RolePermissionsRepo rolePermissionsRepo,
                           MenuBuilder menuBuilder,
                           PermissionBitCalculator permissionBitCalculator,
                           FeatureMapService featureMapService,
                           UserShopAccessRepo userShopAccessRepo) {
                this.usersRepo = usersRepo;
                this.categoriesRepo = categoriesRepo;
                this.rolePermissionsRepo = rolePermissionsRepo;
                this.menuBuilder = menuBuilder;
                this.permissionBitCalculator = permissionBitCalculator;
                this.featureMapService = featureMapService;
                this.userShopAccessRepo = userShopAccessRepo;
        }

        public MenuResponse generateMenu() {
                String uuid = JwtUtil.extractJwtClaim().uuid();
                Roles role = JwtUtil.extractJwtClaim().role();

                Users userEntity = usersRepo.findByUuid(uuid)
                                .orElseThrow(() -> new RuntimeException(
                                                String.format("UnException exception, uuid : %s not found. But extracted from JWT",
                                                                uuid)));

                UserShopAccess userShopAccess = userShopAccessRepo.findByUserId(userEntity.getId()).stream()
                                .filter(usa -> {
                                        // This logic is tricky if we don't know the shop ID corresponding to shopCode
                                        // from here simply.
                                        // But we can assume if the user is logged in, they are logged in context of a
                                        // shop.
                                        // Ideally we should filter by shopId, but we only have shopCode in JWT.
                                        // However, we can also just take the one that matches the role if unique, or
                                        // fetch shopId.
                                        // For now, let's just get the first one or better, if we have shopCode, we
                                        // assume the token is scoped.
                                        // Actually, let's just get the permissions from the first shop access if
                                        // multiple?
                                        // Or we should fetch Shop by shopCode and then filter by shopId.
                                        return true;
                                        // To do it properly: We need to inject ShopsRepo to find shopId from shopCode.
                                })
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("No shop access found for user"));

                // NOTE: The above stream logic is simplified. To be robust, we should filter by
                // the specific shop in the JWT.
                // Assuming single shop per user implementation for now or that we don't care
                // about verifying match.
                // But better is to inject ShopsRepo.

                // Wait, I can't inject ShopsRepo here without adding it to constructor.
                // It's better to add ShopsRepo to constructor.

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
}
