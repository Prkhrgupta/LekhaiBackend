package in.lekhai.core.repository;

import in.lekhai.core.entity.SuperAdminMaster;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface SuperAdminMasterRepo extends ListCrudRepository<SuperAdminMaster, Long> {
    Optional<SuperAdminMaster> findByUuid(String uuid);
}
