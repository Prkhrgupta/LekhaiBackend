package in.lekhai.core.service.feature;

import in.lekhai.core.domain.feature.FeatureMaster;
import in.lekhai.core.dto.feature.FeatureCreationResponse;
import in.lekhai.core.dto.feature.FeatureResponse;
import in.lekhai.core.dto.feature.ScreenFeatureCreationRequest;
import in.lekhai.core.repository.feature.FeatureMasterRepo;
import in.lekhai.error.controller.feature.exception.ParentIdDoesNotExistException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FeatureService {

    private final FeatureMasterRepo featureMasterRepo;
    private final FeatureMapService featureMapService;
    private final FeatureHierarchyBuilder featureHierarchyBuilder;

    public FeatureService(FeatureMasterRepo featureMasterRepo,
                          FeatureMapService featureMapService,
                          FeatureHierarchyBuilder featureHierarchyBuilder
    ) {
        this.featureMasterRepo = featureMasterRepo;
        this.featureMapService = featureMapService;
        this.featureHierarchyBuilder = featureHierarchyBuilder;
    }


    @Transactional
    public FeatureCreationResponse createScreenFeature(ScreenFeatureCreationRequest request) {
        // TODO --> check for parent circular reference,
        Long parentFeatureId = null;
        String route = null;
        if(request.parentId() != null) {
            FeatureMaster parentFeatureMaster = featureMasterRepo.findById(request.parentId())
                    .orElseThrow(() -> new ParentIdDoesNotExistException(request.parentId()));
            parentFeatureId = parentFeatureMaster.getId();
            if(request.isScreen()) {
                route = generateRouteForScreenFeature(parentFeatureId, request.featureKey());
            }
        }

        FeatureMaster featureMasterToBeCreated = FeatureMaster.builder()
                .featureKey(request.featureKey())
                .parentFeatureId(parentFeatureId)
                .bitPosition(request.isScreen() ? featureMasterRepo.findNextAvailableBitPosition() : null)
                .icon(request.icon())
                .title(request.title())
                .route(route)
                .build();

        FeatureMaster savedFeatureMaster = featureMasterRepo.save(featureMasterToBeCreated);
        return new FeatureCreationResponse(
                savedFeatureMaster.getId(),
                savedFeatureMaster.getFeatureKey(),
                savedFeatureMaster.getBitPosition(),
                savedFeatureMaster.getTitle(),
                savedFeatureMaster.getIcon(),
                savedFeatureMaster.getRoute()
        );
    }

    private String generateRouteForScreenFeature(Long id, String featureKey) {
        List<String> parents = featureMasterRepo.findAllParentsLink(id);
        parents.add(featureKey);
        return "/" + String.join("/", parents);
    }
    public List<FeatureResponse> getAllFeatures() {
        Map<Long, FeatureMaster> featureMap = featureMapService.getFeatureMap();

        return featureMapService.getRootFeatures()
                .stream()
                .map(feature -> buildFeatureResponse(feature, featureMap))
                .collect(Collectors.toList());
    }

    private FeatureResponse buildFeatureResponse(FeatureMaster feature, Map<Long, FeatureMaster> featureMap) {
        List<FeatureMaster> hierarchy = featureHierarchyBuilder.buildParentHierarchy(feature, featureMap);

        return new FeatureResponse(
                feature.getId(),
                feature.getFeatureKey(),
                feature.getTitle(),
                feature.getBitPosition(),
                feature.getIsActive(),
                featureHierarchyBuilder.getParentTitles(hierarchy),
                featureHierarchyBuilder.buildRoute(hierarchy)
        );
    }
}
