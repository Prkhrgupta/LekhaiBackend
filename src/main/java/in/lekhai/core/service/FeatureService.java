package in.lekhai.core.service;

import in.lekhai.core.entity.FeatureMaster;
import in.lekhai.core.model.FeatureCreationResponse;
import in.lekhai.core.model.ScreenFeatureCreationRequest;
import in.lekhai.core.repository.FeatureMasterRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class FeatureService {

    private final FeatureMasterRepo featureMasterRepo;

    public FeatureService(FeatureMasterRepo featureMasterRepo) {
        this.featureMasterRepo = featureMasterRepo;
    }


    @Transactional
    public FeatureCreationResponse createScreenFeature(ScreenFeatureCreationRequest request) {
        featureMasterRepo.findByFeatureKey(request.featureKey())
                .ifPresent((v) -> {
                    log.error("featureKey {} already exists, use a unique key", request.featureKey());
                    throw new IllegalArgumentException("Feature key already exists");
                });

        Long parentFeatureId = null;
        String parentFeatureKey = null;
        if(request.parentId() != null) {
            FeatureMaster parentFeatureMaster = featureMasterRepo.findById(request.parentId())
                    .orElseThrow(() -> {
                        log.error("parentId {} doesn't exists", request.parentId());
                        return new IllegalArgumentException("Passed parentId doesn't exits");
                    });
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

}
