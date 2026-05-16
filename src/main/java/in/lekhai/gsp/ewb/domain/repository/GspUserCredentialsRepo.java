package in.lekhai.gsp.ewb.domain.repository;

import in.lekhai.gsp.ewb.domain.entity.GspUserCredentials;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GspUserCredentialsRepo extends ListCrudRepository<GspUserCredentials, Long> {
    Optional<GspUserCredentials> findByShopCode(Integer shopCode);
}
