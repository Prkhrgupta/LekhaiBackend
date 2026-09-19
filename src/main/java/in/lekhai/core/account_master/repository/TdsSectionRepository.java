package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.TdsSection;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TdsSectionRepository extends ListCrudRepository<TdsSection, Long> {

    @Query("SELECT * FROM tds_section WHERE is_active = TRUE ORDER BY code")
    List<TdsSection> findAllActive();
}
