package in.lekhai.core.service;

import in.lekhai.core.entity.CategoryMaster;
import in.lekhai.core.model.request.CategoryCreationRequest;
import in.lekhai.core.model.request.EnableCategoryWiseFeatures;
import in.lekhai.core.model.response.CategoryCreationResponse;
import in.lekhai.core.model.response.CategoryResponse;
import in.lekhai.core.repository.CategoryMasterRepo;
import in.lekhai.core.util.PermissionBitCalculator;
import in.lekhai.error.controller.category.exception.CategoryAlreadyExistException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryMasterRepo categoryMasterRepo;
    private final PermissionBitCalculator bitCalculator;

    public CategoryService(CategoryMasterRepo categoryMasterRepo,
                           PermissionBitCalculator bitCalculator
    ) {
        this.categoryMasterRepo = categoryMasterRepo;
        this.bitCalculator = bitCalculator;
    }

    public CategoryCreationResponse createCategory(CategoryCreationRequest request) {
        categoryMasterRepo
                .findByCategory(request.categoryName())
                .ifPresent(category -> {
                    throw new CategoryAlreadyExistException(request.categoryName());
                });

        CategoryMaster savedCategoryResponse = categoryMasterRepo.save(CategoryMaster.builder()
                .category(request.categoryName()).build());

        return new CategoryCreationResponse(savedCategoryResponse.getCategory());
    }

    public List<CategoryResponse> listOfCategories() {
        return categoryMasterRepo.findAll()
                .stream()
                .map((categoryMaster) ->
                        new CategoryResponse(categoryMaster.getId(),
                                categoryMaster.getCategory(),
                                categoryMaster.getCreatedAt()
                        ))
                .toList();
    }

    public void enableFeaturesForCategory(EnableCategoryWiseFeatures request) {
        //TODO: check if the bits and category id are valid, send a list of category or permission that don't exists
        List<CategoryMaster> categoryMasterList = categoryMasterRepo.findAllById(request.categoryIdList());
        categoryMasterList.forEach(category -> {
            bitCalculator.enableBits(category.getPermission(), request.bitsPositionsToBeEnabled());
        });
        categoryMasterRepo.saveAll(categoryMasterList);
    }
}
