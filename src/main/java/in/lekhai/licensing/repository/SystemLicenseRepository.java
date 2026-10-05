package in.lekhai.licensing.repository;

import in.lekhai.licensing.domain.SystemLicense;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemLicenseRepository extends CrudRepository<SystemLicense, Long> {
    Optional<SystemLicense> findByIsActiveTrue();
    Optional<SystemLicense> findByLicenseId(String licenseId);
}
