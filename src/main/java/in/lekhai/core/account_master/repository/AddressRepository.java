package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.Address;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends CrudRepository<Address, Long> {
    Optional<Address> findByLedgerId(Long ledgerId);
}
