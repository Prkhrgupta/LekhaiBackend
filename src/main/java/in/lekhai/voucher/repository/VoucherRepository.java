package in.lekhai.voucher.repository;

import in.lekhai.voucher.entity.Voucher;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherRepository extends ListCrudRepository<Voucher, Long> {
}
