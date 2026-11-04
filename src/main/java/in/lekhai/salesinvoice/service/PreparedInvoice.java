package in.lekhai.salesinvoice.service;

import in.lekhai.salesinvoice.calc.InvoiceModels.CalcResult;
import in.lekhai.salesinvoice.calc.InvoiceModels.LineInput;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Fully resolved and calculated invoice, ready to preview or persist. Holds only snapshots, no entities.
 */
public record PreparedInvoice(
        String seriesCode,
        Long manualBillNo,
        LocalDate billDate,
        PartyInfo party,
        String placeOfSupply,
        String placeOfSupplySource,
        boolean interState,
        String supplyType,
        String remarks,
        List<LineSnapshot> snapshots,
        List<LineInput> inputs,
        CalcResult calc,
        List<Long> saleLedgerIds,
        List<Long> taxLedgerIds,
        Long roundOffLedgerId) {

    public record PartyInfo(
            Long ledgerId,
            String name,
            String gstin,
            String address,
            String stateGstCode) {
    }

    public record LineSnapshot(
            Long stockItemId,
            String itemName,
            String hsnCode,
            String categoryName,
            Long uomId,
            String uomCode,
            BigDecimal gstRate,
            BigDecimal cessRate,
            BigDecimal costRate) {
    }
}
