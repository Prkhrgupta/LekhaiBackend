package in.lekhai.core.repository;

import in.lekhai.core.entity.TenantDetails;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantDetailsRepo extends ListCrudRepository<TenantDetails, Long> {
    Optional<TenantDetails> findByUuid(String Uuid);
}
