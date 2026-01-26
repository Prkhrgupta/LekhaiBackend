package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.Transport;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransportRepository extends CrudRepository<Transport, Long> {
}
