package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.AccountGroup;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountGroupRepository extends ListCrudRepository<AccountGroup, Long> {

    Optional<AccountGroup> findByNameIgnoreCase(String name);
}
