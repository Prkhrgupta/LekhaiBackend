package in.lekhai.core.account_master.utils;

import in.lekhai.contract.model.GstTaxability;
import in.lekhai.contract.model.PurchaseType;
import in.lekhai.contract.model.SaleType;
import in.lekhai.core.account_master.utils.GstLedgerSettingRules.SupplyKind;
import in.lekhai.core.account_master.utils.GstLedgerSettingRules.TaxPercentages;
import in.lekhai.error.controller.LekhaiClientException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GstLedgerSettingRulesTest {

    private static final BigDecimal EIGHTEEN = BigDecimal.valueOf(18);

    @Test
    void kindOf_mapsEveryPlaceOfSupplyType() {
        assertEquals(SupplyKind.INTRA_STATE, GstLedgerSettingRules.kindOf(SaleType.IN_STATE));
        assertEquals(SupplyKind.INTER_STATE, GstLedgerSettingRules.kindOf(SaleType.OUT_STATE));
        assertEquals(SupplyKind.INTER_STATE, GstLedgerSettingRules.kindOf(SaleType.EXPORT_WITH_IGST));
        assertEquals(SupplyKind.INTER_STATE, GstLedgerSettingRules.kindOf(SaleType.SEZ_WITH_IGST));
        assertEquals(SupplyKind.ZERO_RATED, GstLedgerSettingRules.kindOf(SaleType.EXPORT_UNDER_LUT));
        assertEquals(SupplyKind.ZERO_RATED, GstLedgerSettingRules.kindOf(SaleType.SEZ_UNDER_LUT));

        assertEquals(SupplyKind.INTRA_STATE, GstLedgerSettingRules.kindOf(PurchaseType.IN_STATE));
        assertEquals(SupplyKind.INTER_STATE, GstLedgerSettingRules.kindOf(PurchaseType.OUT_STATE));
        assertEquals(SupplyKind.INTER_STATE, GstLedgerSettingRules.kindOf(PurchaseType.IMPORT));
        assertEquals(SupplyKind.INTER_STATE, GstLedgerSettingRules.kindOf(PurchaseType.SEZ));
    }

    @Test
    void derivePercentages_intraStateSplitsRateIntoCgstAndSgst() {
        TaxPercentages p = GstLedgerSettingRules.derivePercentages(
                SupplyKind.INTRA_STATE, GstTaxability.TAXABLE, BigDecimal.valueOf(5));

        assertEquals(0, p.cgst().compareTo(new BigDecimal("2.5")));
        assertEquals(0, p.sgst().compareTo(new BigDecimal("2.5")));
        assertEquals(0, p.igst().signum());
    }

    @Test
    void derivePercentages_interStateChargesFullRateAsIgst() {
        TaxPercentages p = GstLedgerSettingRules.derivePercentages(
                SupplyKind.INTER_STATE, GstTaxability.TAXABLE, EIGHTEEN);

        assertEquals(0, p.cgst().signum());
        assertEquals(0, p.sgst().signum());
        assertEquals(0, p.igst().compareTo(EIGHTEEN));
    }

    @Test
    void derivePercentages_zeroRatedAndNonTaxableCarryNoTax() {
        TaxPercentages lut = GstLedgerSettingRules.derivePercentages(
                SupplyKind.ZERO_RATED, GstTaxability.TAXABLE, EIGHTEEN);
        TaxPercentages exempt = GstLedgerSettingRules.derivePercentages(
                SupplyKind.INTRA_STATE, GstTaxability.EXEMPT, EIGHTEEN);

        for (TaxPercentages p : new TaxPercentages[]{lut, exempt}) {
            assertEquals(0, p.cgst().signum());
            assertEquals(0, p.sgst().signum());
            assertEquals(0, p.igst().signum());
        }
        assertEquals(0, GstLedgerSettingRules.effectiveRate(
                SupplyKind.INTRA_STATE, GstTaxability.NIL_RATED, EIGHTEEN).signum());
    }

    @Test
    void validateTaxLedgers_acceptsConsistentSettings() {
        assertDoesNotThrow(() -> GstLedgerSettingRules.validateTaxLedgers(
                SupplyKind.INTRA_STATE, GstTaxability.TAXABLE, EIGHTEEN, 1L, 2L, null, null, null));
        assertDoesNotThrow(() -> GstLedgerSettingRules.validateTaxLedgers(
                SupplyKind.INTER_STATE, GstTaxability.TAXABLE, EIGHTEEN, null, null, 3L,
                BigDecimal.ONE, 4L));
        assertDoesNotThrow(() -> GstLedgerSettingRules.validateTaxLedgers(
                SupplyKind.ZERO_RATED, GstTaxability.TAXABLE, null, null, null, null, null, null));
    }

    @Test
    void validateTaxLedgers_rejectsIgstLedgerOnInStateSupply() {
        assertBadRequest(() -> GstLedgerSettingRules.validateTaxLedgers(
                SupplyKind.INTRA_STATE, GstTaxability.TAXABLE, EIGHTEEN, 1L, 2L, 3L, null, null));
    }

    @Test
    void validateTaxLedgers_rejectsMissingSgstLedgerOnInStateSupply() {
        assertBadRequest(() -> GstLedgerSettingRules.validateTaxLedgers(
                SupplyKind.INTRA_STATE, GstTaxability.TAXABLE, EIGHTEEN, 1L, null, null, null, null));
    }

    @Test
    void validateTaxLedgers_rejectsCgstLedgerOnInterStateSupply() {
        assertBadRequest(() -> GstLedgerSettingRules.validateTaxLedgers(
                SupplyKind.INTER_STATE, GstTaxability.TAXABLE, EIGHTEEN, 1L, null, 3L, null, null));
    }

    @Test
    void validateTaxLedgers_rejectsMissingOrOutOfRangeRateForTaxableSupply() {
        assertBadRequest(() -> GstLedgerSettingRules.validateTaxLedgers(
                SupplyKind.INTER_STATE, GstTaxability.TAXABLE, BigDecimal.ZERO, null, null, 3L, null, null));
        assertBadRequest(() -> GstLedgerSettingRules.validateTaxLedgers(
                SupplyKind.INTER_STATE, GstTaxability.TAXABLE, BigDecimal.valueOf(101), null, null, 3L, null, null));
    }

    @Test
    void validateTaxLedgers_rejectsTaxLedgersOnSuppliesWithoutTax() {
        assertBadRequest(() -> GstLedgerSettingRules.validateTaxLedgers(
                SupplyKind.ZERO_RATED, GstTaxability.TAXABLE, EIGHTEEN, null, null, 3L, null, null));
        assertBadRequest(() -> GstLedgerSettingRules.validateTaxLedgers(
                SupplyKind.INTRA_STATE, GstTaxability.EXEMPT, null, null, null, null, BigDecimal.ONE, 4L));
    }

    @Test
    void validateTaxLedgers_rejectsCessWithoutLedger() {
        assertBadRequest(() -> GstLedgerSettingRules.validateTaxLedgers(
                SupplyKind.INTER_STATE, GstTaxability.TAXABLE, EIGHTEEN, null, null, 3L, BigDecimal.ONE, null));
    }

    @Test
    void validateReverseCharge_acceptsMatchingPayableLedgers() {
        assertDoesNotThrow(() -> GstLedgerSettingRules.validateReverseCharge(
                true, PurchaseType.IN_STATE, GstTaxability.TAXABLE, 1L, 2L, null));
        assertDoesNotThrow(() -> GstLedgerSettingRules.validateReverseCharge(
                true, PurchaseType.OUT_STATE, GstTaxability.TAXABLE, null, null, 3L));
        assertDoesNotThrow(() -> GstLedgerSettingRules.validateReverseCharge(
                false, PurchaseType.IMPORT, GstTaxability.TAXABLE, null, null, null));
    }

    @Test
    void validateReverseCharge_rejectsInconsistentSettings() {
        // wrong split for in-state
        assertBadRequest(() -> GstLedgerSettingRules.validateReverseCharge(
                true, PurchaseType.IN_STATE, GstTaxability.TAXABLE, null, null, 3L));
        // wrong split for out-state
        assertBadRequest(() -> GstLedgerSettingRules.validateReverseCharge(
                true, PurchaseType.OUT_STATE, GstTaxability.TAXABLE, 1L, 2L, null));
        // imports are not reverse charge purchases here
        assertBadRequest(() -> GstLedgerSettingRules.validateReverseCharge(
                true, PurchaseType.IMPORT, GstTaxability.TAXABLE, null, null, 3L));
        // only taxable purchases
        assertBadRequest(() -> GstLedgerSettingRules.validateReverseCharge(
                true, PurchaseType.IN_STATE, GstTaxability.EXEMPT, 1L, 2L, null));
        // payable ledgers without reverse charge
        assertBadRequest(() -> GstLedgerSettingRules.validateReverseCharge(
                false, PurchaseType.IN_STATE, GstTaxability.TAXABLE, 1L, 2L, null));
    }

    private static void assertBadRequest(Runnable call) {
        LekhaiClientException e = assertThrows(LekhaiClientException.class, call::run);
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
    }
}
