package in.lekhai.core.service.feature;

import in.lekhai.core.domain.feature.Features;
import in.lekhai.core.repository.feature.FeaturesRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FeatureMapService {

    private final FeaturesRepo featuresRepo;

    public FeatureMapService(FeaturesRepo featuresRepo) {
        this.featuresRepo = featuresRepo;
    }

    /**
     * Returns all features as a map for quick lookup
     * Can be cached if features don't change often
     */
    public Map<Long, Features> getFeatureMap() {
        return featuresRepo.findAll()
                .stream()
                .collect(Collectors.toMap(Features::getId, f -> f));
    }

    /**
     * Gets features by bit positions
     */
    public List<Features> getFeaturesByBitPositions(Set<Integer> bitPositions) {
        return featuresRepo.findByBitPositionIn(bitPositions);
    }

    /**
     * Gets only root features (with bit positions)
     */
    public List<Features> getRootFeatures() {
        return featuresRepo.findAll()
                .stream()
                .filter(f -> f.getBitPosition() != null)
                .collect(Collectors.toList());
    }
}