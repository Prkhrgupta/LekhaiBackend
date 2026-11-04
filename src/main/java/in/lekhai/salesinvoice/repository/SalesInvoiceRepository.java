package in.lekhai.salesinvoice.repository;

import in.lekhai.salesinvoice.entity.SalesInvoice;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesInvoiceRepository extends ListCrudRepository<SalesInvoice, Long> {
}
