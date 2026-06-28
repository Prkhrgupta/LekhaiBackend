package in.lekhai.voucher.repository;

import in.lekhai.voucher.entity.Voucher;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends ListCrudRepository<Voucher, Long> {
    Optional<Voucher> findByVoucherTypeAndVoucherNumber(String voucherType, Long voucherNumber);
}
