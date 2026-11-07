package in.lekhai.salesinvoice.repository;

import in.lekhai.salesinvoice.entity.SalesInvoiceItem;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesInvoiceItemRepository extends ListCrudRepository<SalesInvoiceItem, Long> {

    List<SalesInvoiceItem> findBySalesInvoiceIdOrderBySrno(Long salesInvoiceId);
}
