package in.lekhai.voucher.repository;

import in.lekhai.accountbooks.balancesheet.repository.LedgerNetTotalProjection;
import in.lekhai.accountbooks.balancesheet.repository.NatureTotalProjection;
import in.lekhai.core.account_master.domain.LedgerSummaryProjection;
import in.lekhai.voucher.entity.VoucherEntry;
import org.springframework.data.jdbc.repository.query.Modifying;
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

    @Query(value = """
        SELECT ve.ledger_id,
               COALESCE(SUM(ve.debit_amount), 0) AS total_debit,
               COALESCE(SUM(ve.credit_amount), 0) AS total_credit
        FROM voucher_entry ve
        JOIN voucher v ON ve.voucher_id = v.id
        WHERE v.voucher_date <= :toDate
        GROUP BY ve.ledger_id
    """)
    List<LedgerNetTotalProjection> sumNetByLedgerUpToDate(@Param("toDate") LocalDate toDate);

    @Query(value = """
        SELECT ve.ledger_id,
               COALESCE(SUM(ve.debit_amount), 0) AS total_debit,
               COALESCE(SUM(ve.credit_amount), 0) AS total_credit
        FROM voucher_entry ve
        JOIN voucher v ON ve.voucher_id = v.id
        WHERE v.voucher_date >= :fromDate
          AND v.voucher_date <= :toDate
        GROUP BY ve.ledger_id
    """)
    List<LedgerNetTotalProjection> sumNetByLedgerBetweenDates(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
        SELECT ve.ledger_id,
               COALESCE(SUM(ve.debit_amount), 0) AS total_debit,
               COALESCE(SUM(ve.credit_amount), 0) AS total_credit
        FROM voucher_entry ve
        JOIN voucher v ON ve.voucher_id = v.id
        WHERE v.voucher_date < :fromDate
        GROUP BY ve.ledger_id
    """)
    List<LedgerNetTotalProjection> sumNetByLedgerBeforeDate(@Param("fromDate") LocalDate fromDate);

    @Query(value = """
        SELECT ag.nature,
               COALESCE(SUM(ve.debit_amount), 0) AS total_debit,
               COALESCE(SUM(ve.credit_amount), 0) AS total_credit
        FROM voucher_entry ve
        JOIN voucher v ON ve.voucher_id = v.id
        JOIN ledger l ON ve.ledger_id = l.id
        JOIN account_group ag ON l.account_group_id = ag.id
        WHERE ag.nature IN ('INCOME', 'EXPENSE')
          AND v.voucher_date >= :fromDate
          AND v.voucher_date <= :toDate
        GROUP BY ag.nature
    """)
    List<NatureTotalProjection> sumIncomeExpenseBetweenDates(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
        SELECT ve.id, ve.voucher_id, ve.ledger_id, ve.line_number,
               ve.debit_amount, ve.credit_amount,
               v.voucher_date,
               ve.clear_date,
               0.0 AS running_net
        FROM voucher_entry ve
        JOIN voucher v ON ve.voucher_id = v.id
        WHERE ve.ledger_id IN (:ledgerIds)
          AND v.voucher_date >= :fromDate
          AND v.voucher_date <= :toDate
        ORDER BY v.voucher_date, ve.voucher_id, ve.line_number, ve.id
    """)
    List<LedgerEntryWithBalanceProjection> findEntriesByLedgerIdsBetweenDates(
            @Param("ledgerIds") List<Long> ledgerIds,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
        SELECT COALESCE(SUM(ve.debit_amount - ve.credit_amount), 0)
        FROM voucher_entry ve
        JOIN voucher v ON ve.voucher_id = v.id
        WHERE ve.ledger_id IN (:ledgerIds)
          AND v.voucher_date < :fromDate
    """)
    BigDecimal sumNetBeforeByLedgerIds(
            @Param("ledgerIds") List<Long> ledgerIds,
            @Param("fromDate") LocalDate fromDate
    );

    @Modifying
    @Query("""
        UPDATE voucher_entry
        SET clear_date = :clearDate, updated_at = CURRENT_TIMESTAMP
        WHERE id = :id
    """)
    int updateClearDate(@Param("id") Long id, @Param("clearDate") LocalDate clearDate);
}
