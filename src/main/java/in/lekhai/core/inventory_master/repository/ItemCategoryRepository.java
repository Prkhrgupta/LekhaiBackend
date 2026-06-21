package in.lekhai.core.inventory_master.repository;

import in.lekhai.core.inventory_master.domain.ItemCategory;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemCategoryRepository extends CrudRepository<ItemCategory, Long> {

    @Query("SELECT * FROM item_category_master WHERE (is_deleted IS NULL OR is_deleted = FALSE) "
            + "ORDER BY name, id LIMIT :limit OFFSET :offset")
    List<ItemCategory> findAllActive(@Param("limit") int limit, @Param("offset") long offset);

    @Query("SELECT COUNT(*) FROM item_category_master WHERE (is_deleted IS NULL OR is_deleted = FALSE)")
    long countAllActive();

    @Query("SELECT * FROM item_category_master WHERE (is_deleted IS NULL OR is_deleted = FALSE) AND name ILIKE '%' || :query || '%' "
            + "ORDER BY name, id LIMIT :limit OFFSET :offset")
    List<ItemCategory> findActiveByNameContainingIgnoreCase(@Param("query") String query,
                                                            @Param("limit") int limit,
                                                            @Param("offset") long offset);

    @Query("SELECT COUNT(*) FROM item_category_master WHERE (is_deleted IS NULL OR is_deleted = FALSE) AND name ILIKE '%' || :query || '%'")
    long countActiveByNameContainingIgnoreCase(@Param("query") String query);
}
