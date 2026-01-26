package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.Ledger;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LedgerRepository extends CrudRepository<Ledger, Long> {
}
