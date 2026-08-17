package in.lekhai.core.repository.feature;

import in.lekhai.core.domain.feature.Features;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FeaturesRepo extends ListCrudRepository<Features, Long> {
    List<Features> findByBitPositionIn(Set<Integer> bitPositions);
    Optional<Features> findByFeatureKey(String featureKey);

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

    @Query(value = """
            WITH RECURSIVE super_admin_tree AS (
                SELECT id, feature_key, parent_id, title, icon, route, bit_position, display_order, is_active, is_deleted, created_at, updated_at
                FROM features
                WHERE feature_key = 'super_admin' AND parent_id IS NULL
                
                UNION ALL
                
                SELECT f.id, f.feature_key, f.parent_id, f.title, f.icon, f.route, f.bit_position, f.display_order, f.is_active, f.is_deleted, f.created_at, f.updated_at
                FROM features f
                INNER JOIN super_admin_tree sat ON f.parent_id = sat.id
            )
            SELECT id, feature_key, parent_id, title, icon, route, bit_position, display_order, is_active, is_deleted, created_at, updated_at
            FROM super_admin_tree
            WHERE bit_position IS NOT NULL AND is_active = true AND is_deleted = false;
            """)
    List<Features> findSuperAdminLeafFeatures();
}
