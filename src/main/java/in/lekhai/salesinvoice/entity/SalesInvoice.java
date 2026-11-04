package in.lekhai.salesinvoice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Immutable sales invoice header. Party, address and series data are frozen snapshots taken at billing time.
 */
@Table("sales_invoice")
public record SalesInvoice(
        @Id Long id,
        Long voucherId,
        Long seriesId,
        String seriesCodeSnapshot,
        Long billNo,
        String billDisplay,
        String financialYear,
        LocalDate billDate,
        Long partyLedgerId,
        String partyNameSnapshot,
        String partyGstinSnapshot,
        String partyStateCodeSnapshot,
        String billingAddressSnapshot,
        String shippingAddressSnapshot,
        Long shipToId,
        String placeOfSupply,
        String placeOfSupplySource,
        Boolean isInterState,
        String supplyType,
        Boolean isRcm,
        String status,
        BigDecimal totalGoodsValue,
        BigDecimal totalDiscount,
        BigDecimal totalAddCharges,
        BigDecimal totalTaxable,
        BigDecimal cgstAmt,
        BigDecimal sgstAmt,
        BigDecimal igstAmt,
        BigDecimal cessAmt,
        BigDecimal roundOff,
        BigDecimal netAmount,
        String remarks,
        Instant cancelledAt,
        String cancelReason,
        Instant createdAt
) {
    public SalesInvoice withId(Long newId) {
        return new SalesInvoice(newId, voucherId, seriesId, seriesCodeSnapshot, billNo, billDisplay,
                financialYear, billDate, partyLedgerId, partyNameSnapshot, partyGstinSnapshot,
                partyStateCodeSnapshot, billingAddressSnapshot, shippingAddressSnapshot, shipToId,
                placeOfSupply, placeOfSupplySource, isInterState, supplyType, isRcm, status,
                totalGoodsValue, totalDiscount, totalAddCharges, totalTaxable, cgstAmt, sgstAmt,
                igstAmt, cessAmt, roundOff, netAmount, remarks, cancelledAt, cancelReason, createdAt);
    }
}
