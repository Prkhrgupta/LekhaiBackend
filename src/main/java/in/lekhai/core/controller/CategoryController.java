package in.lekhai.core.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.common.Result;
import in.lekhai.core.model.request.CategoryCreationRequest;
import in.lekhai.core.model.request.EnableCategoryWiseFeatures;
import in.lekhai.core.model.response.CategoryCreationResponse;
import in.lekhai.core.model.response.CategoryResponse;
import in.lekhai.core.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@PreAuthorize(SecurityExpressions.IS_SUPER_ADMIN)
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/create")
    public ResponseEntity<Result<?>> createCategory(@RequestBody @Valid CategoryCreationRequest request) {
        CategoryCreationResponse response = categoryService.createCategory(request);
        return ResponseEntity.ok(Result.success(response));
    }

    @GetMapping("/list-all")
    public ResponseEntity<Result<List<CategoryResponse>>> getListOfCategories() {
        List<CategoryResponse> categoryResponses = categoryService.listOfCategories();
        return ResponseEntity.ok(Result.success(categoryResponses));
    }

    @PostMapping("/enable/features")
    public ResponseEntity<Result<?>> enableFeaturesForCategory(@RequestBody @Valid EnableCategoryWiseFeatures request) {
        categoryService.enableFeaturesForCategory(request);
        return ResponseEntity.ok(null);
    }
}
