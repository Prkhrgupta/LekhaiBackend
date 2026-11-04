package in.lekhai.salesinvoice.repository;

import in.lekhai.salesinvoice.entity.SalesInvoiceTax;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesInvoiceTaxRepository extends ListCrudRepository<SalesInvoiceTax, Long> {
}
