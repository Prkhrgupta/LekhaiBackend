package in.lekhai.core.service;

import in.lekhai.core.entity.FeatureMaster;
import in.lekhai.core.model.response.FeatureCreationResponse;
import in.lekhai.core.model.request.ScreenFeatureCreationRequest;
import in.lekhai.core.repository.FeatureMasterRepo;
import in.lekhai.error.controller.feature.exception.FeatureKeyAlreadyExistException;
import in.lekhai.error.controller.feature.exception.ParentIdDoesNotExistException;
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

}
