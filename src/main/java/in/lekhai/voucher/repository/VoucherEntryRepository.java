package in.lekhai.voucher.repository;

import in.lekhai.core.account_master.domain.LedgerSummaryProjection;
import in.lekhai.voucher.entity.VoucherEntry;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface VoucherEntryRepository extends CrudRepository<VoucherEntry, Long> {
    List<VoucherEntry> findByVoucherId(Long voucherId);

    List<VoucherEntry> findByVoucherIdIn(List<Long> voucherIds);

    @Query("""
        SELECT * FROM (
            SELECT ve.id, ve.voucher_id, ve.ledger_id, ve.line_number,
                   ve.debit_amount, ve.credit_amount,
                   v.voucher_date,
                   SUM(ve.debit_amount - ve.credit_amount) OVER (
                       ORDER BY v.voucher_date, ve.voucher_id, ve.line_number, ve.id
                       ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                   ) AS running_net
            FROM voucher_entry ve
            JOIN voucher v ON ve.voucher_id = v.id
            WHERE ve.ledger_id = :ledgerId
              AND v.voucher_date <= :toDate
        ) t
        WHERE t.voucher_date >= :fromDate
        ORDER BY t.voucher_date, t.voucher_id, t.line_number, t.id
        LIMIT :limit OFFSET :offset
    """)
    List<LedgerEntryWithBalanceProjection> findPageWithRunningBalance(
            @Param("ledgerId") Long ledgerId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Query("""
        SELECT COALESCE(SUM(ve.debit_amount - ve.credit_amount), 0)
        FROM voucher_entry ve
        JOIN voucher v ON ve.voucher_id = v.id
        WHERE ve.ledger_id = :ledgerId
          AND v.voucher_date < :fromDate
    """)
    BigDecimal sumNetBefore(@Param("ledgerId") Long ledgerId,
                            @Param("fromDate") LocalDate fromDate);

    @Query("""
        SELECT COUNT(*)
        FROM voucher_entry ve
        JOIN voucher v ON ve.voucher_id = v.id
        WHERE ve.ledger_id = :ledgerId
          AND v.voucher_date BETWEEN :fromDate AND :toDate
    """)
    long countByLedgerIdAndDateBetween(@Param("ledgerId") Long ledgerId,
                                       @Param("fromDate") LocalDate fromDate,
                                       @Param("toDate") LocalDate toDate);


    @Query(value = """
        SELECT
            COALESCE(SUM(ve.debit_amount), 0) AS total_debit,
            COALESCE(SUM(ve.credit_amount), 0) AS total_credit,
            COALESCE(SUM(ve.debit_amount - ve.credit_amount), 0) AS net_balance
        FROM voucher_entry ve
        JOIN voucher v ON ve.voucher_id = v.id
        WHERE ve.ledger_id = :ledgerId
          AND v.voucher_date >= :fromDate
          AND v.voucher_date <= :toDate
    """)
    LedgerSummaryProjection getLedgerSummary(
            @Param("ledgerId") Long ledgerId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
