package in.lekhai.core.repository.feature;

import in.lekhai.core.domain.feature.FeatureMaster;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FeatureMasterRepo extends ListCrudRepository<FeatureMaster, Long> {
    Optional<FeatureMaster> findByFeatureKey(String featureKey);
    List<FeatureMaster> findByBitPositionIn(Set<Integer> bitPositions);

    @Query(value = """
            SELECT *
            FROM features
            WHERE bit_position is NOT NULL
            """)
    List<FeatureMaster> findAllRootFeatures();

    @Query(value = """
        SELECT COALESCE(MAX(bit_position) + 1, 0)
        FROM features
        WHERE bit_position IS NOT NULL
        """)
    Integer findNextAvailableBitPosition();

    @Query(value = """
            WITH RECURSIVE parent_hierarchy AS (
                SELECT
                    id,
                    feature_key,
                    parent_id,
                    1 AS level
                FROM features
                WHERE id = :id
            
                UNION ALL
            
                SELECT
                    f.id,
                    f.feature_key,
                    f.parent_id,
                    ph.level + 1
                FROM features f
                INNER JOIN parent_hierarchy ph ON f.id = ph.parent_id
            )
            SELECT feature_key
            FROM parent_hierarchy
            ORDER BY level DESC;
            """)
    List<String> findAllParentsLink(Long id);
}
