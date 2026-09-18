package in.lekhai.voucher.repository;

import in.lekhai.voucher.entity.VoucherCounter;
import in.lekhai.voucher.entity.VoucherType;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherCounterRepository extends ListCrudRepository<VoucherCounter, Long> {
    /**
      FOR UPDATE --> This will lock the selected row for the txn, to avoid same voucherNo generation
      DON'T use this in long transactions
     */
    @Query("""
        SELECT *
        FROM voucher_counter
        WHERE voucher_type = :voucherType
        FOR UPDATE
        """)
    Optional<VoucherCounter> findForUpdate(VoucherType voucherType);
}
