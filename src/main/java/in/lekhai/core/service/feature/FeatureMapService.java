package in.lekhai.core.service.feature;

import in.lekhai.core.domain.feature.FeatureMaster;
import in.lekhai.core.repository.feature.FeatureMasterRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FeatureMapService {

    private final FeatureMasterRepo featureMasterRepo;

    public FeatureMapService(FeatureMasterRepo featureMasterRepo) {
        this.featureMasterRepo = featureMasterRepo;
    }

    /**
     * Returns all features as a map for quick lookup
     * Can be cached if features don't change often
     */
    public Map<Long, FeatureMaster> getFeatureMap() {
        return featureMasterRepo.findAll()
                .stream()
                .collect(Collectors.toMap(FeatureMaster::getId, f -> f));
    }

    /**
     * Gets features by bit positions
     */
    public List<FeatureMaster> getFeaturesByBitPositions(Set<Integer> bitPositions) {
        return featureMasterRepo.findByBitPositionIn(bitPositions);
    }

    /**
     * Gets only root features (with bit positions)
     */
    public List<FeatureMaster> getRootFeatures() {
        return featureMasterRepo.findAll()
                .stream()
                .filter(f -> f.getBitPosition() != null)
                .collect(Collectors.toList());
    }
}