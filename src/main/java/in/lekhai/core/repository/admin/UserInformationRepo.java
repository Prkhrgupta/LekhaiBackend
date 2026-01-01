package in.lekhai.core.repository.admin;

import in.lekhai.core.domain.admin.UserInformation;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface UserInformationRepo extends ListCrudRepository<UserInformation, Long> {
    Optional<UserInformation> findByUuid(String uuid);
}
