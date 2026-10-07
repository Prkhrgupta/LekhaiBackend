package in.lekhai.voucher.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import in.lekhai.voucher.entity.CustomVoucherSeries;
import in.lekhai.voucher.entity.VoucherType;

@Repository
public interface CustomVoucherSeriesRepository extends ListCrudRepository<CustomVoucherSeries, Long> {

    @Query("""
        SELECT *
        FROM custom_voucher_series
        WHERE voucher_type = :voucherType
          AND series_code = :seriesCode
          AND financial_year = :financialYear
        FOR UPDATE
        """)
    Optional<CustomVoucherSeries> findForUpdate(
            @Param("voucherType") VoucherType voucherType,
            @Param("seriesCode") String seriesCode,
            @Param("financialYear") String financialYear);

    @Query("""
        SELECT *
        FROM custom_voucher_series
        WHERE id = :id
        FOR UPDATE
        """)
    Optional<CustomVoucherSeries> findByIdForUpdate(@Param("id") Long id);

    boolean existsByVoucherTypeAndSeriesCodeAndFinancialYear(
            VoucherType voucherType, String seriesCode, String financialYear);

    @Query("""
        SELECT *
        FROM custom_voucher_series
        WHERE (:voucherType IS NULL OR voucher_type = :voucherType)
          AND (:financialYear IS NULL OR financial_year = :financialYear)
        ORDER BY id
        LIMIT :limit OFFSET :offset
        """)
    List<CustomVoucherSeries> findFiltered(
            @Param("voucherType") VoucherType voucherType,
            @Param("financialYear") String financialYear,
            @Param("limit") int limit,
            @Param("offset") long offset);

    @Query("""
        SELECT COUNT(*)
        FROM custom_voucher_series
        WHERE (:voucherType IS NULL OR voucher_type = :voucherType)
          AND (:financialYear IS NULL OR financial_year = :financialYear)
        """)
    long countFiltered(
            @Param("voucherType") VoucherType voucherType,
            @Param("financialYear") String financialYear);
}
