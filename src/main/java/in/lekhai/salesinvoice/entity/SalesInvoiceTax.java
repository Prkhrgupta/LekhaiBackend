package in.lekhai.salesinvoice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("sales_invoice_tax")
public record SalesInvoiceTax(
        @Id Long id,
        Long salesInvoiceId,
        String taxType,
        BigDecimal taxRate,
        BigDecimal taxableAmount,
        BigDecimal taxAmount,
        Long taxLedgerId
) {
    public SalesInvoiceTax withId(Long newId) {
        return new SalesInvoiceTax(newId, salesInvoiceId, taxType, taxRate, taxableAmount, taxAmount,
                taxLedgerId);
    }
}
