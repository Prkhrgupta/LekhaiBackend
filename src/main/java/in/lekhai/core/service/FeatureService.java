package in.lekhai.core.service;

import in.lekhai.core.entity.CategoryMaster;
import in.lekhai.core.entity.FeatureMaster;
import in.lekhai.core.model.request.EnableCategoryWiseFeatures;
import in.lekhai.core.model.request.FeatureResponse;
import in.lekhai.core.model.response.FeatureCreationResponse;
import in.lekhai.core.model.request.ScreenFeatureCreationRequest;
import in.lekhai.core.repository.CategoryMasterRepo;
import in.lekhai.core.repository.FeatureMasterRepo;
import in.lekhai.error.controller.feature.exception.FeatureKeyAlreadyExistException;
import in.lekhai.error.controller.feature.exception.ParentIdDoesNotExistException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class FeatureService {

    private final FeatureMasterRepo featureMasterRepo;
    private final CategoryMasterRepo categoryMasterRepo;

    public FeatureService(FeatureMasterRepo featureMasterRepo,
                          CategoryMasterRepo categoryMasterRepo) {
        this.featureMasterRepo = featureMasterRepo;
        this.categoryMasterRepo = categoryMasterRepo;
    }


    @Transactional
    public FeatureCreationResponse createScreenFeature(ScreenFeatureCreationRequest request) {
        featureMasterRepo
                .findByFeatureKey(request.featureKey())
                .ifPresent((v) -> {
                    throw new FeatureKeyAlreadyExistException(request.featureKey());
                });

        Long parentFeatureId = null;
        String parentFeatureKey = null;
        if(request.parentId() != null) {
            FeatureMaster parentFeatureMaster = featureMasterRepo.findById(request.parentId())
                    .orElseThrow(() -> new ParentIdDoesNotExistException(request.parentId()));
            parentFeatureId = parentFeatureMaster.getId();
            parentFeatureKey = parentFeatureMaster.getFeatureKey();
        }

        FeatureMaster featureMasterToBeCreated = FeatureMaster.builder()
                .featureKey(request.featureKey())
                .parentFeatureId(parentFeatureId)
                .bitPosition(request.isScreen() ? featureMasterRepo.findNextAvailableBitPosition() : null)
                .icon(request.icon())
                .title(request.title())
                .build();

        FeatureMaster savedFeatureMaster = featureMasterRepo.save(featureMasterToBeCreated);
        return new FeatureCreationResponse(
                savedFeatureMaster.getId(),
                savedFeatureMaster.getFeatureKey(),
                savedFeatureMaster.getBitPosition(),
                savedFeatureMaster.getTitle(),
                savedFeatureMaster.getIcon(),
                parentFeatureKey
        );
    }

    public List<FeatureResponse> getAllFeatures() {
        List<FeatureMaster> allFeatures = featureMasterRepo.findAll();
        Map<Long, FeatureMaster> featureMap = allFeatures.stream()
                .collect(Collectors.toMap(FeatureMaster::getId, f -> f));

        return allFeatures.stream()
                .filter(f -> f.getBitPosition() != null) // Only root features
                .map(feature -> buildFeatureResponse(feature, featureMap))
                .collect(Collectors.toList());
    }

    private FeatureResponse buildFeatureResponse(FeatureMaster feature, Map<Long, FeatureMaster> featureMap) {
        List<FeatureMaster> hierarchy = buildHierarchy(feature, featureMap);

        List<String> parentFeaturesOrder = hierarchy.stream()
                .map(FeatureMaster::getTitle)
                .collect(Collectors.toList());

        String route = hierarchy.stream()
                .map(FeatureMaster::getFeatureKey)
                .collect(Collectors.joining("/", "/", ""));

        return new FeatureResponse(
                feature.getId(),
                feature.getFeatureKey(),
                feature.getTitle(),
                feature.getBitPosition(),
                feature.getIsActive(),
                parentFeaturesOrder,
                route
        );
    }

    private List<FeatureMaster> buildHierarchy(FeatureMaster feature, Map<Long, FeatureMaster> featureMap) {
        LinkedList<FeatureMaster> hierarchy = new LinkedList<>();
        FeatureMaster current = feature;

        while (current != null) {
            hierarchy.addFirst(current);
            current = current.getParentFeatureId() != null
                    ? featureMap.get(current.getParentFeatureId())
                    : null;
        }

        return hierarchy;
    }

    public void enableFeatureForCategory(EnableCategoryWiseFeatures request)  {
        Set<Integer> bitPositionSetFromDb = featureMasterRepo.findByBitPositionIn(request.bitsPositionsToBeEnabled())
                .stream()
                .map(FeatureMaster::getBitPosition)
                .collect(Collectors.toSet());

        Set<Long> categoryIdFromDb = categoryMasterRepo.findAllById(request.categoryIdList())
                .stream()
                .map(CategoryMaster::getId)
                .collect(Collectors.toSet());

        

    }
}
