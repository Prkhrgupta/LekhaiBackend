package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.State;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StateRepository extends CrudRepository<State, String> {
}
