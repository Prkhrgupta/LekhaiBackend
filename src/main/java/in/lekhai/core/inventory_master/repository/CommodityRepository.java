package in.lekhai.core.inventory_master.repository;

import in.lekhai.core.inventory_master.domain.Commodity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommodityRepository extends CrudRepository<Commodity, Long> {
}
