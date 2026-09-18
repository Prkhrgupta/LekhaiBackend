package in.lekhai.core.inventory_master.repository;

import in.lekhai.core.inventory_master.domain.StockItem;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockItemRepository extends CrudRepository<StockItem, Long> {

    @Query("SELECT * FROM stock_item_master WHERE (is_deleted IS NULL OR is_deleted = FALSE) "
            + "ORDER BY item_name, id LIMIT :limit OFFSET :offset")
    List<StockItem> findAllActive(@Param("limit") int limit, @Param("offset") long offset);

    @Query("SELECT COUNT(*) FROM stock_item_master WHERE (is_deleted IS NULL OR is_deleted = FALSE)")
    long countAllActive();

    @Query("SELECT * FROM stock_item_master WHERE (is_deleted IS NULL OR is_deleted = FALSE) AND item_name ILIKE '%' || :query || '%' "
            + "ORDER BY item_name, id LIMIT :limit OFFSET :offset")
    List<StockItem> findActiveByNameContainingIgnoreCase(@Param("query") String query,
                                                         @Param("limit") int limit,
                                                         @Param("offset") long offset);

    @Query("SELECT COUNT(*) FROM stock_item_master WHERE (is_deleted IS NULL OR is_deleted = FALSE) AND item_name ILIKE '%' || :query || '%'")
    long countActiveByNameContainingIgnoreCase(@Param("query") String query);
}
