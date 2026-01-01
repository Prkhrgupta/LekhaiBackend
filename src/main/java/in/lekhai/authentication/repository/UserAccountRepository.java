package in.lekhai.authentication.repository;

import in.lekhai.authentication.entity.UserAccounts;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends ListCrudRepository<UserAccounts, Long> {

    Optional<UserAccounts> findByUsername(String username);

}
