package in.lekhai.core.service.feature;

import in.lekhai.core.domain.feature.Features;
import in.lekhai.core.dto.feature.FeatureCreationResponse;
import in.lekhai.core.dto.feature.FeatureResponse;
import in.lekhai.core.dto.feature.ScreenFeatureCreationRequest;
import in.lekhai.core.repository.feature.FeaturesRepo;
import in.lekhai.error.controller.feature.exception.ParentIdDoesNotExistException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeatureService {

    private final FeaturesRepo featuresRepo;
    private final FeatureMapService featureMapService;
    private final FeatureHierarchyBuilder featureHierarchyBuilder;

    public FeatureService(FeaturesRepo featuresRepo,
            FeatureMapService featureMapService,
            FeatureHierarchyBuilder featureHierarchyBuilder) {
        this.featuresRepo = featuresRepo;
        this.featureMapService = featureMapService;
        this.featureHierarchyBuilder = featureHierarchyBuilder;
    }

    @Transactional
    public FeatureCreationResponse createScreenFeature(ScreenFeatureCreationRequest request) {
        // TODO --> check for parent circular reference,
        Long parentFeatureId = null;
        String route = null;
        if (request.parentId() != null) {
            Features parentFeatures = featuresRepo.findById(request.parentId())
                    .orElseThrow(() -> new ParentIdDoesNotExistException(request.parentId()));
            parentFeatureId = parentFeatures.getId();
            if (request.isScreen()) {
                route = generateRouteForScreenFeature(parentFeatureId, request.featureKey());
            }
        }

        Features featuresToBeCreated = Features.builder()
                .featureKey(request.featureKey())
                .parentFeatureId(parentFeatureId)
                .bitPosition(request.isScreen() ? featuresRepo.findNextAvailableBitPosition() : null)
                .icon(request.icon())
                .title(request.title())
                .route(route)
                .build();

        Features savedFeatures = featuresRepo.save(featuresToBeCreated);
        return new FeatureCreationResponse(
                savedFeatures.getId(),
                savedFeatures.getFeatureKey(),
                savedFeatures.getBitPosition(),
                savedFeatures.getTitle(),
                savedFeatures.getIcon(),
                savedFeatures.getRoute());
    }

    private String generateRouteForScreenFeature(Long id, String featureKey) {
        List<String> parents = featuresRepo.findAllParentsLink(id);
        parents.add(featureKey);
        return "/" + String.join("/", parents);
    }

    public List<FeatureResponse> getAllFeatures() {
        Map<Long, Features> featureMap = featureMapService.getFeatureMap();

        return featureMapService.getRootFeatures()
                .stream()
                .map(feature -> buildFeatureResponse(feature, featureMap))
                .collect(Collectors.toList());
    }

    private FeatureResponse buildFeatureResponse(Features feature, Map<Long, Features> featureMap) {
        List<Features> hierarchy = featureHierarchyBuilder.buildParentHierarchy(feature, featureMap);

        return new FeatureResponse(
                feature.getId(),
                feature.getFeatureKey(),
                feature.getTitle(),
                feature.getBitPosition(),
                feature.getIsActive(),
                featureHierarchyBuilder.getParentTitles(hierarchy),
                featureHierarchyBuilder.buildRoute(hierarchy));
    }
}
