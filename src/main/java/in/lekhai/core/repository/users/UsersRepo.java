package in.lekhai.core.repository.users;

import in.lekhai.core.domain.users.Users;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface UsersRepo extends ListCrudRepository<Users, Long> {
    Optional<Users> findByUuid(String uuid);
}
