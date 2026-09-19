package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.PurchaseLedgerSetting;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseLedgerSettingRepository extends CrudRepository<PurchaseLedgerSetting, Long> {

    @Query("SELECT p.* FROM purchase_ledger_setting_master p "
            + "JOIN ledger l ON l.id = p.purchase_ledger_id "
            + "WHERE (p.is_deleted IS NULL OR p.is_deleted = FALSE) "
            + "ORDER BY l.name, p.id LIMIT :limit OFFSET :offset")
    List<PurchaseLedgerSetting> findAllActive(@Param("limit") int limit, @Param("offset") long offset);

    @Query("SELECT COUNT(*) FROM purchase_ledger_setting_master WHERE (is_deleted IS NULL OR is_deleted = FALSE)")
    long countAllActive();

    @Query("SELECT p.* FROM purchase_ledger_setting_master p "
            + "JOIN ledger l ON l.id = p.purchase_ledger_id "
            + "WHERE (p.is_deleted IS NULL OR p.is_deleted = FALSE) "
            + "AND l.name ILIKE '%' || :query || '%' "
            + "ORDER BY l.name, p.id LIMIT :limit OFFSET :offset")
    List<PurchaseLedgerSetting> findActiveByPurchaseLedgerNameContainingIgnoreCase(@Param("query") String query,
                                                                                  @Param("limit") int limit,
                                                                                  @Param("offset") long offset);

    @Query("SELECT COUNT(*) FROM purchase_ledger_setting_master p "
            + "JOIN ledger l ON l.id = p.purchase_ledger_id "
            + "WHERE (p.is_deleted IS NULL OR p.is_deleted = FALSE) "
            + "AND l.name ILIKE '%' || :query || '%'")
    long countActiveByPurchaseLedgerNameContainingIgnoreCase(@Param("query") String query);

    /** Pass {@code excludeId = 0} when creating (ids start at 1). */
    @Query("SELECT EXISTS (SELECT 1 FROM purchase_ledger_setting_master "
            + "WHERE (is_deleted IS NULL OR is_deleted = FALSE) "
            + "AND purchase_ledger_id = :ledgerId AND purchase_type = :purchaseType AND id <> :excludeId)")
    boolean existsActiveByPurchaseLedgerIdAndPurchaseType(@Param("ledgerId") Long ledgerId,
                                                          @Param("purchaseType") String purchaseType,
                                                          @Param("excludeId") long excludeId);
}
