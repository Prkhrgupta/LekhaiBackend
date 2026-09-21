package in.lekhai.core.service.category;

import in.lekhai.core.domain.category.Categories;
import in.lekhai.core.domain.category.RolePermissions;
import in.lekhai.core.dto.category.CategoryCreationRequest;
import in.lekhai.core.dto.category.CategoryCreationResponse;
import in.lekhai.core.dto.category.CategoryResponse;
import in.lekhai.core.dto.category.EnableCategoryWiseFeatures;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.category.CategoriesRepo;
import in.lekhai.core.repository.category.RolePermissionsRepo;
import in.lekhai.core.util.PermissionBitCalculator;
import in.lekhai.error.controller.category.exception.CategoryAlreadyExistException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private static final List<Roles> PROPAGATED_ROLES = List.of(Roles.SUPER_ADMIN, Roles.SHOP_OWNER);

    private final CategoriesRepo categoriesRepo;
    private final RolePermissionsRepo rolePermissionsRepo;
    private final PermissionBitCalculator bitCalculator;

    public CategoryService(CategoriesRepo categoriesRepo,
                           RolePermissionsRepo rolePermissionsRepo,
                           PermissionBitCalculator bitCalculator
    ) {
        this.categoriesRepo = categoriesRepo;
        this.rolePermissionsRepo = rolePermissionsRepo;
        this.bitCalculator = bitCalculator;
    }
    public CategoryCreationResponse createCategory(CategoryCreationRequest request) {
        categoriesRepo
                .findByName(request.categoryName())
                .ifPresent(category -> {
                    throw new CategoryAlreadyExistException(request.categoryName());
                });

        Categories savedCategoryResponse = categoriesRepo.save(Categories.builder()
                .name(request.categoryName()).build());

        return new CategoryCreationResponse(savedCategoryResponse.getName());
    }

    public List<CategoryResponse> listOfCategories() {
        return categoriesRepo.findAll()
                .stream()
                .map((categories) ->
                        new CategoryResponse(categories.getId(),
                                categories.getName(),
                                categories.getPermissions(),
                                categories.getCreatedAt()
                        ))
                .toList();
    }

    @Transactional
    public void enableFeaturesForCategory(EnableCategoryWiseFeatures request) {
        //TODO: check if the bits and category id are valid, send a list of category or permission that don't exists
        List<Categories> categoriesList = categoriesRepo.findAllById(request.categoryIdList());
        updateCategoryPermissions(categoriesList, request.bitsPositionsToBeEnabled());
        categoriesRepo.saveAll(categoriesList);
        syncRolePermissions(categoriesList, request.bitsPositionsToBeEnabled());
    }

    private void updateCategoryPermissions(List<Categories> categoriesList, Set<Integer> bitsToEnable) {
        categoriesList.forEach(category -> {
            ensurePermissionsInitialized(category.getPermissions(), category::setPermissions);
            category.getPermissions().clear();
            bitCalculator.enableBits(category.getPermissions(), bitsToEnable);
        });
    }

    private void syncRolePermissions(List<Categories> categoriesList, Set<Integer> bitsToEnable) {
        if (categoriesList.isEmpty()) {
            return;
        }
        List<Integer> categoryIds = categoriesList.stream().map(Categories::getId).toList();
        Map<String, RolePermissions> existingByKey = rolePermissionsRepo
                .findByCategoryIdInAndRoleIn(categoryIds, PROPAGATED_ROLES)
                .stream()
                .collect(Collectors.toMap(this::rolePermissionKey, Function.identity()));
        List<RolePermissions> toSave = new ArrayList<>();
        for (Categories category : categoriesList) {
            for (Roles role : PROPAGATED_ROLES) {
                RolePermissions rolePermissions = existingByKey.get(key(category.getId(), role));
                if (rolePermissions == null) {
                    continue;
                }
                ensurePermissionsInitialized(rolePermissions.getPermissions(), rolePermissions::setPermissions);
                rolePermissions.getPermissions().clear();
                bitCalculator.enableBits(rolePermissions.getPermissions(), bitsToEnable);
                toSave.add(rolePermissions);
            }
        }
        if (toSave.isEmpty()) {
            return;
        }
        rolePermissionsRepo.saveAll(toSave);
    }

    private void ensurePermissionsInitialized(List<Long> permissions, Consumer<List<Long>> setter) {
        if (permissions == null) {
            setter.accept(new ArrayList<>());
        }
    }

    private String rolePermissionKey(RolePermissions rolePermissions) {
        return key(rolePermissions.getCategoryId(), rolePermissions.getRole());
    }

    private String key(Integer categoryId, Roles role) {
        return categoryId + ":" + role.name();
    }
}
