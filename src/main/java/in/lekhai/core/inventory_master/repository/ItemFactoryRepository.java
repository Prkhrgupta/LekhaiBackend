package in.lekhai.core.inventory_master.repository;

import in.lekhai.core.inventory_master.domain.ItemFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemFactoryRepository extends CrudRepository<ItemFactory, Long> {

    @Query("SELECT * FROM item_factory_master WHERE (is_deleted IS NULL OR is_deleted = FALSE)")
    List<ItemFactory> findAllActive(Pageable pageable);

    @Query("SELECT COUNT(*) FROM item_factory_master WHERE (is_deleted IS NULL OR is_deleted = FALSE)")
    long countAllActive();

    @Query("SELECT * FROM item_factory_master WHERE (is_deleted IS NULL OR is_deleted = FALSE) AND name ILIKE '%' || :query || '%'")
    List<ItemFactory> findActiveByNameContainingIgnoreCase(@Param("query") String query, Pageable pageable);

    @Query("SELECT COUNT(*) FROM item_factory_master WHERE (is_deleted IS NULL OR is_deleted = FALSE) AND name ILIKE '%' || :query || '%'")
    long countActiveByNameContainingIgnoreCase(@Param("query") String query);
}
