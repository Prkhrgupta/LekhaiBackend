package in.lekhai.voucher.repository;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import in.lekhai.voucher.entity.Voucher;

@Repository
public interface VoucherRepository extends ListCrudRepository<Voucher, Long> {

    @Query("""
        SELECT COUNT(*)
        FROM voucher
        WHERE series_id = :seriesId
        """)
    long countBySeriesId(@Param("seriesId") Long seriesId);

    @Query("""
        SELECT voucher_number
        FROM voucher
        WHERE series_id = :seriesId
          AND financial_year = :financialYear
        ORDER BY voucher_number
        """)
    List<Long> findBillNumbersBySeriesAndFy(
            @Param("seriesId") Long seriesId,
            @Param("financialYear") String financialYear);

    @Query("""
        SELECT COUNT(*) > 0
        FROM voucher
        WHERE series_id = :seriesId
          AND financial_year = :financialYear
          AND voucher_number = :voucherNumber
        """)
    boolean existsBySeriesAndFyAndNumber(
            @Param("seriesId") Long seriesId,
            @Param("financialYear") String financialYear,
            @Param("voucherNumber") Long voucherNumber);

    @Query("""
        SELECT *
        FROM voucher
        WHERE voucher_type = :voucherType
          AND voucher_date >= :fromDate
          AND voucher_date <= :toDate
        ORDER BY voucher_date ASC, id ASC
        """)
    List<Voucher> findByVoucherTypeAndDateBetween(
            @Param("voucherType") String voucherType,
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate);
}
