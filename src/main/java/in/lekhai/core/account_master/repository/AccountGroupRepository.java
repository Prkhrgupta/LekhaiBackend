package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.AccountGroup;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountGroupRepository extends CrudRepository<AccountGroup, Long> {
}
