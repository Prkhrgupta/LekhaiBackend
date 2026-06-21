package in.lekhai.core.inventory_master.repository;

import in.lekhai.core.inventory_master.domain.Commodity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommodityRepository extends CrudRepository<Commodity, Long> {

    @Query("SELECT * FROM commodity_master WHERE (is_deleted IS NULL OR is_deleted = FALSE) "
            + "ORDER BY item_name, item_id LIMIT :limit OFFSET :offset")
    List<Commodity> findAllActive(@Param("limit") int limit, @Param("offset") long offset);

    @Query("SELECT COUNT(*) FROM commodity_master WHERE is_deleted IS NULL OR is_deleted = FALSE")
    long countAllActive();
}
