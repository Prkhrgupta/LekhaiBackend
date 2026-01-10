package in.lekhai.core.service.category;

import in.lekhai.core.domain.category.Categories;
import in.lekhai.core.dto.category.CategoryCreationRequest;
import in.lekhai.core.dto.category.CategoryCreationResponse;
import in.lekhai.core.dto.category.CategoryResponse;
import in.lekhai.core.dto.category.EnableCategoryWiseFeatures;
import in.lekhai.core.repository.category.CategoriesRepo;
import in.lekhai.core.util.PermissionBitCalculator;
import in.lekhai.error.controller.category.exception.CategoryAlreadyExistException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoriesRepo categoriesRepo;
    private final PermissionBitCalculator bitCalculator;

    public CategoryService(CategoriesRepo categoriesRepo,
                           PermissionBitCalculator bitCalculator
    ) {
        this.categoriesRepo = categoriesRepo;
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
                                categories.getCreatedAt()
                        ))
                .toList();
    }

    public void enableFeaturesForCategory(EnableCategoryWiseFeatures request) {
        //TODO: check if the bits and category id are valid, send a list of category or permission that don't exists
        List<Categories> categoriesList = categoriesRepo.findAllById(request.categoryIdList());
        categoriesList.forEach(category -> {
            bitCalculator.enableBits(category.getPermissions(), request.bitsPositionsToBeEnabled());
        });
        categoriesRepo.saveAll(categoriesList);
    }
}
