package in.lekhai.core.repository;

import in.lekhai.core.entity.FeatureMaster;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeatureMasterRepo extends ListCrudRepository<FeatureMaster, Long> {
    Optional<FeatureMaster> findByFeatureKey(String featureKey);

    @Query(value = """
        SELECT COALESCE(MAX(bit_position) + 1, 0)
        FROM features
        WHERE bit_position IS NOT NULL
        """)
    Long findNextAvailableBitPosition();
}
