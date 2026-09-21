package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.GeneralLedgerSetting;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GeneralLedgerSettingRepository extends CrudRepository<GeneralLedgerSetting, Long> {

    @Query("SELECT * FROM general_ledger_setting_master "
            + "WHERE (is_deleted IS NULL OR is_deleted = FALSE) LIMIT 1")
    Optional<GeneralLedgerSetting> findActive();
}
