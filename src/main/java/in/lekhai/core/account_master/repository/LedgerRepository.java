package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.Ledger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LedgerRepository extends CrudRepository<Ledger, Long> {
    Optional<Ledger> findByGstInNumber(String gstInNumber);
    Page<Ledger> findAll(Pageable pageable);
}
