package in.lekhai.core.repository.superadmin;

import in.lekhai.core.domain.superadmin.SuperAdminMaster;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface SuperAdminMasterRepo extends ListCrudRepository<SuperAdminMaster, Long> {
    Optional<SuperAdminMaster> findByUuid(String uuid);
}
