package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.Broker;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrokerRepository extends CrudRepository<Broker, Long> {
    Optional<Broker> findByCsvId(Integer csvId);
}
