package in.lekhai.salesinvoice.service;

import in.lekhai.contract.model.SalesInvoiceItemRequest;
import in.lekhai.contract.model.SalesInvoiceRequest;
import in.lekhai.core.account_master.domain.SaleLedgerSetting;
import in.lekhai.salesinvoice.calc.InvoiceCalculator;
import in.lekhai.salesinvoice.calc.InvoiceModels.CalcResult;
import in.lekhai.salesinvoice.calc.InvoiceModels.LineInput;
import in.lekhai.salesinvoice.service.PreparedInvoice.LineSnapshot;
import in.lekhai.salesinvoice.service.PreparedInvoice.PartyInfo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Shared by preview and create: validates the request, snapshots masters, calculates, and resolves ledgers.
 * Reads only; never posts or persists.
 */
@Service
public class SalesInvoicePreparationService {

    private static final BigDecimal B2CL_THRESHOLD = BigDecimal.valueOf(100000);

    private final SalesInvoicePartyResolver partyResolver;
    private final SalesInvoiceItemResolver itemResolver;
    private final SalesInvoiceLedgerResolver ledgerResolver;
    private final InvoiceCalculator calculator;

    public SalesInvoicePreparationService(SalesInvoicePartyResolver partyResolver,
                                          SalesInvoiceItemResolver itemResolver,
                                          SalesInvoiceLedgerResolver ledgerResolver,
                                          InvoiceCalculator calculator) {
        this.partyResolver = partyResolver;
        this.itemResolver = itemResolver;
        this.ledgerResolver = ledgerResolver;
        this.calculator = calculator;
    }

    public PreparedInvoice prepare(SalesInvoiceRequest request) {
        requireSupported(request);
        PartyInfo party = partyResolver.resolveParty(request.getPartyLedgerId());
        boolean requested = hasText(unwrap(request.getPlaceOfSupply()));
        String pos = requested ? unwrap(request.getPlaceOfSupply()) : requireLedgerState(party);
        partyResolver.requireKnownGstState(pos);
        boolean interState = !partyResolver.resolveSellerGstCode().equals(pos);
        List<LineSnapshot> snapshots = itemResolver.snapshots(request.getItems());
        List<LineInput> inputs = toInputs(request.getItems(), snapshots);
        CalcResult calc = calculator.calculate(inputs, interState);
        requirePositiveNet(calc);
        return assemble(request, party, pos, requested, interState, snapshots, inputs, calc);
    }

    private PreparedInvoice assemble(SalesInvoiceRequest request, PartyInfo party, String pos, boolean requested,
                                     boolean interState, List<LineSnapshot> snapshots, List<LineInput> inputs,
                                     CalcResult calc) {
        Map<BigDecimal, SaleLedgerSetting> settings = ledgerResolver.settingsByRate(interState);
        ledgerResolver.requireSaleLedger(unwrap(request.getSaleLedgerId()));
        List<Long> saleLedgers = snapshots.stream()
                .map(s -> ledgerResolver.saleLedgerFor(settings, s.gstRate(), interState)).toList();
        List<Long> taxLedgers = ledgerResolver.taxLedgers(calc.taxes(), settings);
        Long roundOffLedger = requireRoundOffLedger(calc);
        return new PreparedInvoice(request.getSeriesCode().trim().toUpperCase(), unwrap(request.getBillNo()),
                request.getBillDate(), party, pos, requested ? "REQUEST" : "LEDGER", interState,
                classify(party, interState, calc), unwrap(request.getRemarks()), snapshots, inputs, calc,
                saleLedgers, taxLedgers, roundOffLedger);
    }

    private void requireSupported(SalesInvoiceRequest request) {
        if (unwrap(request.getShipToId()) != null) {
            throw SalesInvoiceErrors.unprocessable("Ship-to addresses are not supported yet");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw SalesInvoiceErrors.badRequest("Invoice must contain at least one item");
        }
        if (request.getSeriesCode() == null || request.getSeriesCode().isBlank()) {
            throw SalesInvoiceErrors.badRequest("series_code is required");
        }
    }

    private String requireLedgerState(PartyInfo party) {
        if (party.stateGstCode() == null) {
            throw SalesInvoiceErrors.unprocessable("Party state not set; provide place_of_supply");
        }
        return party.stateGstCode();
    }

    private List<LineInput> toInputs(List<SalesInvoiceItemRequest> requests, List<LineSnapshot> snapshots) {
        return java.util.stream.IntStream.range(0, requests.size())
                .mapToObj(i -> itemResolver.toInput(requests.get(i), snapshots.get(i))).toList();
    }

    private void requirePositiveNet(CalcResult calc) {
        if (calc.totals().netAmount().signum() <= 0) {
            throw SalesInvoiceErrors.badRequest("Invoice net amount must be greater than zero");
        }
    }

    private Long requireRoundOffLedger(CalcResult calc) {
        if (calc.totals().roundOff().signum() == 0) {
            return null;
        }
        Long ledgerId = ledgerResolver.roundOffLedger();
        if (ledgerId == null) {
            throw SalesInvoiceErrors.unprocessable("Configure the round-off ledger in General Ledger Setting");
        }
        return ledgerId;
    }

    private String classify(PartyInfo party, boolean interState, CalcResult calc) {
        if (hasText(party.gstin())) {
            return "B2B";
        }
        boolean large = interState && calc.totals().taxableAmount().compareTo(B2CL_THRESHOLD) > 0;
        return large ? "B2CL" : "B2CS";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private <T> T unwrap(org.openapitools.jackson.nullable.JsonNullable<T> value) {
        return value == null || !value.isPresent() ? null : value.get();
    }
}
