package in.lekhai.voucher.repository;

import in.lekhai.voucher.entity.VoucherEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface VoucherEntryRepository extends ListCrudRepository<VoucherEntry, Long> {
    List<VoucherEntry> findByVoucherId(Long voucherId);

    List<VoucherEntry> findByVoucherIdIn(List<Long> voucherIds);

    @Query("""
        SELECT ve.* FROM voucher_entry ve
        JOIN voucher v ON ve.voucher_id = v.id
        WHERE ve.ledger_id = :ledgerId
        ORDER BY v.voucher_date ASC, ve.line_number ASC
    """)
    List<VoucherEntry> findByLedgerIdOrderByVoucherDate(@Param("ledgerId") Long ledgerId, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(ve.debit_amount - ve.credit_amount), 0)
        FROM voucher_entry ve
        JOIN voucher v ON ve.voucher_id = v.id
        WHERE ve.ledger_id = :ledgerId
        AND (v.voucher_date < :voucherDate
             OR (v.voucher_date = :voucherDate AND ve.line_number < :lineNumber))
    """)
    BigDecimal sumBeforeEntry(@Param("ledgerId") Long ledgerId,
                              @Param("voucherDate") LocalDate voucherDate,
                              @Param("lineNumber") Integer lineNumber);

    @Query("SELECT COUNT(*) FROM voucher_entry WHERE ledger_id = :ledgerId")
    long countByLedgerId(@Param("ledgerId") Long ledgerId);
}
