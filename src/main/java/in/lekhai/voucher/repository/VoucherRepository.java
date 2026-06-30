package in.lekhai.voucher.repository;

import in.lekhai.voucher.entity.Voucher;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface VoucherRepository extends ListCrudRepository<Voucher, Long> {
    Optional<Voucher> findByVoucherTypeAndVoucherNumber(String voucherType, Long voucherNumber);

    @Query("""
        SELECT COALESCE(SUM(debit_amount - credit_amount), 0)
        FROM voucher_entry
        WHERE ledger_id = :ledgerId
    """)
    BigDecimal ledgerCurrentBalance(@Param("ledgerId") Long ledgerId);
}
