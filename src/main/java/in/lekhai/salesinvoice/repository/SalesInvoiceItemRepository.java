package in.lekhai.salesinvoice.repository;

import in.lekhai.salesinvoice.entity.SalesInvoiceItem;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesInvoiceItemRepository extends ListCrudRepository<SalesInvoiceItem, Long> {
}
