package in.lekhai.authentication.repository;

import in.lekhai.authentication.entity.UserCredentials;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCredentialRepository extends ListCrudRepository<UserCredentials, Long> {

    @Query("SELECT * FROM user_credentials where username = :username")
    Optional<UserCredentials> findByUsername(String username);

}
