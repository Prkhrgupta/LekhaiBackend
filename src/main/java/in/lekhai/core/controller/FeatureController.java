package in.lekhai.core.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.common.Result;
import in.lekhai.core.model.request.FeatureResponse;
import in.lekhai.core.model.request.ScreenFeatureCreationRequest;
import in.lekhai.core.model.response.FeatureCreationResponse;
import in.lekhai.core.service.FeatureService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feature")
@PreAuthorize(SecurityExpressions.IS_SUPER_ADMIN)
public class FeatureController {

    private final FeatureService featureService;

    public FeatureController(FeatureService featureService) {
        this.featureService = featureService;
    }

    @PostMapping("/create")
    public ResponseEntity<Result<?>> createFeature(@RequestBody @Valid ScreenFeatureCreationRequest request) {
        FeatureCreationResponse featureCreationResponse = featureService.createScreenFeature(request);
        Result<FeatureCreationResponse> success = Result.success(featureCreationResponse);
        return ResponseEntity.ok(success);
    }

    @GetMapping("/all")
    public ResponseEntity<Result<?>> getAllFeatures() {
        List<FeatureResponse> allFeatures = featureService.getAllFeatures();
        return ResponseEntity.ok(Result.success(allFeatures));
    }
}
