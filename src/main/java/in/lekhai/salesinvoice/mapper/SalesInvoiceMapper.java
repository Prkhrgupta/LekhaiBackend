package in.lekhai.salesinvoice.mapper;

import in.lekhai.contract.model.PlaceOfSupplySource;
import in.lekhai.contract.model.SalesInvoiceItemResult;
import in.lekhai.contract.model.SalesInvoiceParty;
import in.lekhai.contract.model.SalesInvoicePreviewResponse;
import in.lekhai.contract.model.SalesInvoiceResponse;
import in.lekhai.contract.model.SalesInvoiceStatus;
import in.lekhai.contract.model.SalesInvoiceSupplyType;
import in.lekhai.contract.model.SalesInvoiceTaxLine;
import in.lekhai.contract.model.SalesInvoiceTaxType;
import in.lekhai.contract.model.SalesInvoiceTotals;
import in.lekhai.contract.model.SalesInvoiceWarning;
import in.lekhai.salesinvoice.calc.InvoiceModels.LineResult;
import in.lekhai.salesinvoice.calc.InvoiceModels.TaxRow;
import in.lekhai.salesinvoice.calc.InvoiceModels.Totals;
import in.lekhai.salesinvoice.entity.SalesInvoice;
import in.lekhai.salesinvoice.entity.SalesInvoiceItem;
import in.lekhai.salesinvoice.entity.SalesInvoiceTax;
import in.lekhai.salesinvoice.service.PreparedInvoice;
import in.lekhai.salesinvoice.service.PreparedInvoice.LineSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Maps a {@link PreparedInvoice} (the frozen snapshot) to the generated contract responses.
 */
@Component
public class SalesInvoiceMapper {

    private static final String PCS = "PCS";

    public SalesInvoiceResponse toResponse(PreparedInvoice invoice, SalesInvoice saved,
                                           List<SalesInvoiceWarning> warnings) {
        return new SalesInvoiceResponse()
                .id(saved.id()).voucherId(saved.voucherId()).status(SalesInvoiceStatus.ACTIVE)
                .seriesId(saved.seriesId()).seriesCode(saved.seriesCodeSnapshot()).billNo(saved.billNo())
                .billDisplay(saved.billDisplay()).financialYear(saved.financialYear())
                .billDate(saved.billDate()).supplyType(SalesInvoiceSupplyType.fromValue(invoice.supplyType()))
                .placeOfSupply(invoice.placeOfSupply())
                .placeOfSupplySource(PlaceOfSupplySource.fromValue(invoice.placeOfSupplySource()))
                .isInterState(invoice.interState()).shipToId(null).remarks(invoice.remarks())
                .party(toParty(invoice)).items(toItems(invoice)).taxes(toTaxes(invoice))
                .totals(toTotals(invoice)).warnings(warnings)
                .createdAt(saved.createdAt().atOffset(ZoneOffset.UTC));
    }

    /**
     * Rebuilds the response from the stored snapshot rows. Per-line GST is not persisted, so it is derived from
     * the frozen taxable value and rate snapshot with the same rounding the calculator used at creation.
     */
    public SalesInvoiceResponse toResponse(SalesInvoice saved, List<SalesInvoiceItem> items,
                                           List<SalesInvoiceTax> taxes) {
        boolean interState = Boolean.TRUE.equals(saved.isInterState());
        return new SalesInvoiceResponse()
                .id(saved.id()).voucherId(saved.voucherId()).status(SalesInvoiceStatus.fromValue(saved.status()))
                .seriesId(saved.seriesId()).seriesCode(saved.seriesCodeSnapshot()).billNo(saved.billNo())
                .billDisplay(saved.billDisplay()).financialYear(saved.financialYear())
                .billDate(saved.billDate()).supplyType(SalesInvoiceSupplyType.fromValue(saved.supplyType()))
                .placeOfSupply(saved.placeOfSupply())
                .placeOfSupplySource(PlaceOfSupplySource.fromValue(saved.placeOfSupplySource()))
                .isInterState(interState).shipToId(saved.shipToId()).remarks(saved.remarks())
                .party(new SalesInvoiceParty().ledgerId(saved.partyLedgerId()).name(saved.partyNameSnapshot())
                        .gstin(saved.partyGstinSnapshot()).address(saved.billingAddressSnapshot())
                        .stateCode(saved.partyStateCodeSnapshot()))
                .items(items.stream().map(i -> toItem(i, interState)).toList())
                .taxes(taxes.stream().map(this::toTax).toList())
                .totals(toTotals(saved, items))
                .warnings(List.of())
                .createdAt(saved.createdAt().atOffset(ZoneOffset.UTC));
    }

    private SalesInvoiceItemResult toItem(SalesInvoiceItem item, boolean interState) {
        BigDecimal gst = item.taxableValue().multiply(item.gstRateSnapshot())
                .divide(BigDecimal.valueOf(100), 20, RoundingMode.HALF_UP);
        BigDecimal half = gst.divide(BigDecimal.valueOf(2), 20, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
        BigDecimal igst = interState ? gst.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal cgst = interState ? BigDecimal.ZERO : half;
        BigDecimal total = item.taxableValue().add(cgst).add(cgst).add(igst).add(item.cessAmount());
        return new SalesInvoiceItemResult()
                .lineNo(item.srno()).stockItemId(item.stockItemId()).itemName(item.itemNameSnapshot())
                .hsnCode(item.hsnCodeSnapshot()).categoryName(item.categoryNameSnapshot()).uomId(item.uomId())
                .uomCode(item.uomSnapshot()).quantity(d(item.quantity())).rate(d(item.rate()))
                .grossAmount(d(item.grossAmount())).discountPct(d(item.discountPct()))
                .discountAmount(d(item.discountAmount())).taxableAmount(d(item.taxableValue()))
                .gstRate(d(item.gstRateSnapshot())).cessRate(d(item.cessRateSnapshot()))
                .cgstAmount(d(cgst)).sgstAmount(d(cgst)).igstAmount(d(igst)).cessAmount(d(item.cessAmount()))
                .lineTotal(d(total)).saleLedgerId(item.saleLedgerId());
    }

    private SalesInvoiceTaxLine toTax(SalesInvoiceTax tax) {
        return new SalesInvoiceTaxLine().taxType(SalesInvoiceTaxType.fromValue(tax.taxType()))
                .rate(d(tax.taxRate())).taxableAmount(d(tax.taxableAmount())).amount(d(tax.taxAmount()))
                .taxLedgerId(tax.taxLedgerId());
    }

    private SalesInvoiceTotals toTotals(SalesInvoice s, List<SalesInvoiceItem> items) {
        BigDecimal pcs = items.stream().filter(i -> PCS.equalsIgnoreCase(i.uomSnapshot()))
                .map(SalesInvoiceItem::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new SalesInvoiceTotals().totalPcs(d(pcs)).goodsAmount(d(s.totalGoodsValue()))
                .discountAmount(d(s.totalDiscount())).taxableAmount(d(s.totalTaxable()))
                .cgstAmount(d(s.cgstAmt())).sgstAmount(d(s.sgstAmt())).igstAmount(d(s.igstAmt()))
                .cessAmount(d(s.cessAmt())).totalAddCharges(d(s.totalAddCharges())).roundOff(d(s.roundOff()))
                .netAmount(d(s.netAmount()));
    }

    public SalesInvoicePreviewResponse toPreview(PreparedInvoice invoice, Long billNo, String billDisplay,
                                                 String financialYear, List<SalesInvoiceWarning> warnings) {
        return new SalesInvoicePreviewResponse()
                .seriesCode(invoice.seriesCode()).billNo(billNo).billDisplay(billDisplay)
                .financialYear(financialYear).supplyType(SalesInvoiceSupplyType.fromValue(invoice.supplyType()))
                .placeOfSupply(invoice.placeOfSupply())
                .placeOfSupplySource(PlaceOfSupplySource.fromValue(invoice.placeOfSupplySource()))
                .isInterState(invoice.interState()).party(toParty(invoice)).items(toItems(invoice))
                .taxes(toTaxes(invoice)).totals(toTotals(invoice)).warnings(warnings);
    }

    private SalesInvoiceParty toParty(PreparedInvoice invoice) {
        var party = invoice.party();
        return new SalesInvoiceParty().ledgerId(party.ledgerId()).name(party.name()).gstin(party.gstin())
                .address(party.address()).stateCode(party.stateGstCode());
    }

    private List<SalesInvoiceItemResult> toItems(PreparedInvoice invoice) {
        return IntStream.range(0, invoice.snapshots().size()).mapToObj(i -> toItem(invoice, i)).toList();
    }

    private SalesInvoiceItemResult toItem(PreparedInvoice invoice, int i) {
        LineSnapshot snap = invoice.snapshots().get(i);
        var input = invoice.inputs().get(i);
        LineResult line = invoice.calc().lines().get(i);
        return new SalesInvoiceItemResult()
                .lineNo(i + 1).stockItemId(snap.stockItemId()).itemName(snap.itemName())
                .hsnCode(snap.hsnCode()).categoryName(snap.categoryName()).uomId(snap.uomId())
                .uomCode(snap.uomCode()).quantity(d(input.quantity())).rate(d(input.rate()))
                .grossAmount(d(line.grossAmount())).discountPct(d(input.discountPct()))
                .discountAmount(d(line.discountAmount())).taxableAmount(d(line.taxableAmount()))
                .gstRate(d(snap.gstRate())).cessRate(d(snap.cessRate())).cgstAmount(d(line.cgstAmount()))
                .sgstAmount(d(line.sgstAmount())).igstAmount(d(line.igstAmount()))
                .cessAmount(d(line.cessAmount())).lineTotal(d(line.lineTotal()))
                .saleLedgerId(invoice.saleLedgerIds().get(i));
    }

    private List<SalesInvoiceTaxLine> toTaxes(PreparedInvoice invoice) {
        return IntStream.range(0, invoice.calc().taxes().size()).mapToObj(i -> {
            TaxRow row = invoice.calc().taxes().get(i);
            return new SalesInvoiceTaxLine().taxType(SalesInvoiceTaxType.fromValue(row.taxType()))
                    .rate(d(row.rate())).taxableAmount(d(row.taxableAmount())).amount(d(row.amount()))
                    .taxLedgerId(invoice.taxLedgerIds().get(i));
        }).toList();
    }

    private SalesInvoiceTotals toTotals(PreparedInvoice invoice) {
        Totals t = invoice.calc().totals();
        return new SalesInvoiceTotals().totalPcs(d(totalPcs(invoice))).goodsAmount(d(t.goodsAmount()))
                .discountAmount(d(t.discountAmount())).taxableAmount(d(t.taxableAmount()))
                .cgstAmount(d(t.cgstAmount())).sgstAmount(d(t.sgstAmount())).igstAmount(d(t.igstAmount()))
                .cessAmount(d(t.cessAmount())).totalAddCharges(0d).roundOff(d(t.roundOff()))
                .netAmount(d(t.netAmount()));
    }

    // Why: cloth-shop v1 shows quantity in the Pcs column only when the item's primary UOM is Pcs.
    private BigDecimal totalPcs(PreparedInvoice invoice) {
        return IntStream.range(0, invoice.snapshots().size())
                .filter(i -> PCS.equalsIgnoreCase(invoice.snapshots().get(i).uomCode()))
                .mapToObj(i -> invoice.inputs().get(i).quantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Double d(BigDecimal value) {
        return value.doubleValue();
    }
}
