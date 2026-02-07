package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.GstInDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GstDetailsRepository extends CrudRepository<GstInDetails, Long> {
    Optional<GstInDetails> findByLedgerId(Long ledgerId);
}
