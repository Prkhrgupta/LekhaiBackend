package in.lekhai.core.account_master.utils;

import in.lekhai.contract.model.GstTaxability;
import in.lekhai.contract.model.PurchaseType;
import in.lekhai.contract.model.SaleType;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.error.controller.LekhaiClientException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * GST rules shared by the sale and purchase ledger settings: how a GST rate
 * splits into CGST/SGST/IGST for a place-of-supply type, and which tax ledgers a
 * setting must (and must not) carry.
 */
public final class GstLedgerSettingRules {

    private static final BigDecimal MAX_RATE = BigDecimal.valueOf(100);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    /** How tax is charged for a place-of-supply type. */
    public enum SupplyKind {
        /** Intra-state: CGST + SGST. */
        INTRA_STATE,
        /** Inter-state, import, SEZ with payment: IGST. */
        INTER_STATE,
        /** Export / SEZ supply under LUT: zero-rated, no tax charged. */
        ZERO_RATED
    }

    public record TaxPercentages(BigDecimal cgst, BigDecimal sgst, BigDecimal igst) {
    }

    private GstLedgerSettingRules() {
    }

    public static SupplyKind kindOf(SaleType saleType) {
        return switch (saleType) {
            case IN_STATE -> SupplyKind.INTRA_STATE;
            case OUT_STATE, EXPORT_WITH_IGST, SEZ_WITH_IGST -> SupplyKind.INTER_STATE;
            case EXPORT_UNDER_LUT, SEZ_UNDER_LUT -> SupplyKind.ZERO_RATED;
        };
    }

    public static SupplyKind kindOf(PurchaseType purchaseType) {
        return switch (purchaseType) {
            case IN_STATE -> SupplyKind.INTRA_STATE;
            case OUT_STATE, IMPORT, SEZ -> SupplyKind.INTER_STATE;
        };
    }

    /** True when supplies of this kind and taxability actually carry GST. */
    public static boolean chargesTax(SupplyKind kind, GstTaxability taxability) {
        return taxability == GstTaxability.TAXABLE && kind != SupplyKind.ZERO_RATED;
    }

    /**
     * The GST rate to store: the requested rate for taxable supplies, and zero
     * for exempt / nil-rated / non-GST or zero-rated ones.
     */
    public static BigDecimal effectiveRate(SupplyKind kind, GstTaxability taxability, BigDecimal gstRate) {
        return chargesTax(kind, taxability) ? gstRate : BigDecimal.ZERO;
    }

    public static TaxPercentages derivePercentages(SupplyKind kind, GstTaxability taxability, BigDecimal gstRate) {
        if (!chargesTax(kind, taxability) || gstRate == null) {
            return new TaxPercentages(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        return switch (kind) {
            case INTRA_STATE -> {
                BigDecimal half = gstRate.divide(TWO, 3, RoundingMode.HALF_UP);
                yield new TaxPercentages(half, half, BigDecimal.ZERO);
            }
            case INTER_STATE -> new TaxPercentages(BigDecimal.ZERO, BigDecimal.ZERO, gstRate);
            case ZERO_RATED -> new TaxPercentages(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        };
    }

    /**
     * Validates the rate and the CGST/SGST/IGST/cess ledgers against the supply
     * kind. Taxable supplies need the ledgers their tax posts to; supplies that
     * carry no tax must not reference any tax ledger.
     */
    public static void validateTaxLedgers(SupplyKind kind,
                                          GstTaxability taxability,
                                          BigDecimal gstRate,
                                          Long cgstLedgerId,
                                          Long sgstLedgerId,
                                          Long igstLedgerId,
                                          BigDecimal cessPercentage,
                                          Long cessLedgerId) {
        boolean hasCess = cessPercentage != null && cessPercentage.signum() != 0;
        if (cessPercentage != null && (cessPercentage.signum() < 0 || cessPercentage.compareTo(MAX_RATE) > 0)) {
            throw badRequest("Cess percentage must be between 0 and 100");
        }

        if (!chargesTax(kind, taxability)) {
            if (cgstLedgerId != null || sgstLedgerId != null || igstLedgerId != null
                    || cessLedgerId != null || hasCess) {
                throw badRequest(kind == SupplyKind.ZERO_RATED
                        ? "Zero-rated (LUT) supplies carry no GST; remove the tax ledgers and cess"
                        : "Only taxable supplies carry GST; remove the tax ledgers and cess");
            }
            return;
        }

        if (gstRate == null || gstRate.signum() <= 0 || gstRate.compareTo(MAX_RATE) > 0) {
            throw badRequest("GST rate for taxable supplies must be greater than 0 and at most 100");
        }

        if (kind == SupplyKind.INTRA_STATE) {
            if (cgstLedgerId == null || sgstLedgerId == null) {
                throw badRequest("In-state supplies need both a CGST and an SGST ledger");
            }
            if (igstLedgerId != null) {
                throw badRequest("In-state supplies are charged CGST + SGST; remove the IGST ledger");
            }
        } else {
            if (igstLedgerId == null) {
                throw badRequest("Inter-state, import and SEZ supplies need an IGST ledger");
            }
            if (cgstLedgerId != null || sgstLedgerId != null) {
                throw badRequest("Inter-state, import and SEZ supplies are charged IGST; "
                        + "remove the CGST and SGST ledgers");
            }
        }

        if (hasCess && cessLedgerId == null) {
            throw badRequest("A cess percentage needs a cess ledger");
        }
    }

    /**
     * Reverse charge: the recipient self-assesses the tax, crediting it to RCM
     * payable ledgers that match how the tax splits (CGST + SGST or IGST).
     */
    public static void validateReverseCharge(boolean reverseCharge,
                                             PurchaseType purchaseType,
                                             GstTaxability taxability,
                                             Long rcmCgstPayableLedgerId,
                                             Long rcmSgstPayableLedgerId,
                                             Long rcmIgstPayableLedgerId) {
        if (!reverseCharge) {
            if (rcmCgstPayableLedgerId != null || rcmSgstPayableLedgerId != null || rcmIgstPayableLedgerId != null) {
                throw badRequest("RCM payable ledgers apply only to reverse charge purchases");
            }
            return;
        }

        if (purchaseType != PurchaseType.IN_STATE && purchaseType != PurchaseType.OUT_STATE) {
            throw badRequest("Reverse charge applies only to in-state or out-state purchases");
        }
        if (taxability != GstTaxability.TAXABLE) {
            throw badRequest("Reverse charge applies only to taxable purchases");
        }

        if (purchaseType == PurchaseType.IN_STATE) {
            if (rcmCgstPayableLedgerId == null || rcmSgstPayableLedgerId == null) {
                throw badRequest("In-state reverse charge needs RCM CGST and RCM SGST payable ledgers");
            }
            if (rcmIgstPayableLedgerId != null) {
                throw badRequest("In-state reverse charge is CGST + SGST; remove the RCM IGST payable ledger");
            }
        } else {
            if (rcmIgstPayableLedgerId == null) {
                throw badRequest("Out-state reverse charge needs an RCM IGST payable ledger");
            }
            if (rcmCgstPayableLedgerId != null || rcmSgstPayableLedgerId != null) {
                throw badRequest("Out-state reverse charge is IGST; "
                        + "remove the RCM CGST and SGST payable ledgers");
            }
        }
    }

    /** Every referenced ledger must exist (RLS already hides other shops' ledgers). */
    public static void requireLedgersExist(LedgerRepository ledgerRepository, Collection<Long> ledgerIds) {
        Set<Long> requested = ledgerIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (requested.isEmpty()) {
            return;
        }
        Set<Long> found = ledgerRepository.findAllById(requested).stream()
                .map(Ledger::getId)
                .collect(Collectors.toSet());
        requested.removeAll(found);
        if (!requested.isEmpty()) {
            throw badRequest("Ledger(s) not found: " + requested);
        }
    }

    private static LekhaiClientException badRequest(String message) {
        return new LekhaiClientException(message, HttpStatus.BAD_REQUEST);
    }
}
