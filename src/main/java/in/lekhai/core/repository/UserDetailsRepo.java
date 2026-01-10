package in.lekhai.core.repository;

import in.lekhai.core.entity.UserDetails;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailsRepo extends ListCrudRepository<UserDetails, Long> {
    Optional<UserDetails> findByUuid(String uuid);
}
