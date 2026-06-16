package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.AccountGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountGroupRepository extends ListCrudRepository<AccountGroup, Long> {

    Optional<AccountGroup> findByNameIgnoreCase(String name);

    Page<AccountGroup> findAll(Pageable pageable);

    @Query("SELECT * FROM account_group WHERE name ILIKE '%' || :query || '%'")
    List<AccountGroup> findByNameContainingIgnoreCase(@Param("query") String query, Pageable pageable);

    @Query("SELECT COUNT(*) FROM account_group WHERE name ILIKE '%' || :query || '%'")
    long countByNameContainingIgnoreCase(@Param("query") String query);
}
