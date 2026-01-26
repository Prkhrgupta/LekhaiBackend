package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.Area;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaRepository extends CrudRepository<Area, Long> {
}
