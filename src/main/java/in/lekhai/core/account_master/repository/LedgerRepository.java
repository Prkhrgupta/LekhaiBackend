package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.Ledger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LedgerRepository extends CrudRepository<Ledger, Long> {
    Optional<Ledger> findByGstInNumber(String gstInNumber);
    Page<Ledger> findAll(Pageable pageable);

    @Query("SELECT * FROM ledger WHERE name ILIKE '%' || :query || '%'")
    List<Ledger> findByNameContainingIgnoreCase(@Param("query") String query, Pageable pageable);

    @Query("SELECT COUNT(*) FROM ledger WHERE name ILIKE '%' || :query || '%'")
    long countByNameContainingIgnoreCase(@Param("query") String query);
}
