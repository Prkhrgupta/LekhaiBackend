package in.lekhai.voucher.repository;

import in.lekhai.voucher.entity.VoucherEntry;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherEntryRepository extends ListCrudRepository<VoucherEntry, Long> {
    List<VoucherEntry> findByVoucherId(Long voucherId);
}
