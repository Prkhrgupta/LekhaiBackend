package in.lekhai.core.repository.superadmin;

import in.lekhai.core.domain.superadmin.SuperAdminDetails;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface SuperAdminDetailsRepo extends ListCrudRepository<SuperAdminDetails, Integer> {
    Optional<SuperAdminDetails> findByUuid(String uuid);
}
