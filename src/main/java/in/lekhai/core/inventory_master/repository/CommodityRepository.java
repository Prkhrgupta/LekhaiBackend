package in.lekhai.core.inventory_master.repository;

import in.lekhai.core.inventory_master.domain.Commodity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommodityRepository extends CrudRepository<Commodity, Long> {

    @Query("SELECT * FROM commodity_master WHERE is_deleted IS NULL OR is_deleted = FALSE")
    List<Commodity> findAllActive(Pageable pageable);

    @Query("SELECT COUNT(*) FROM commodity_master WHERE is_deleted IS NULL OR is_deleted = FALSE")
    long countAllActive();
}
