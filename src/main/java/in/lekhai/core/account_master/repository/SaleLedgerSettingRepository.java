package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.SaleLedgerSetting;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleLedgerSettingRepository extends CrudRepository<SaleLedgerSetting, Long> {

    @Query("SELECT s.* FROM sale_ledger_setting_master s "
            + "JOIN ledger l ON l.id = s.sale_ledger_id "
            + "WHERE (s.is_deleted IS NULL OR s.is_deleted = FALSE) "
            + "ORDER BY l.name, s.id LIMIT :limit OFFSET :offset")
    List<SaleLedgerSetting> findAllActive(@Param("limit") int limit, @Param("offset") long offset);

    @Query("SELECT COUNT(*) FROM sale_ledger_setting_master WHERE (is_deleted IS NULL OR is_deleted = FALSE)")
    long countAllActive();

    @Query("SELECT s.* FROM sale_ledger_setting_master s "
            + "JOIN ledger l ON l.id = s.sale_ledger_id "
            + "WHERE (s.is_deleted IS NULL OR s.is_deleted = FALSE) "
            + "AND l.name ILIKE '%' || :query || '%' "
            + "ORDER BY l.name, s.id LIMIT :limit OFFSET :offset")
    List<SaleLedgerSetting> findActiveBySaleLedgerNameContainingIgnoreCase(@Param("query") String query,
                                                                          @Param("limit") int limit,
                                                                          @Param("offset") long offset);

    @Query("SELECT COUNT(*) FROM sale_ledger_setting_master s "
            + "JOIN ledger l ON l.id = s.sale_ledger_id "
            + "WHERE (s.is_deleted IS NULL OR s.is_deleted = FALSE) "
            + "AND l.name ILIKE '%' || :query || '%'")
    long countActiveBySaleLedgerNameContainingIgnoreCase(@Param("query") String query);
}
