package in.lekhai.salesinvoice.service;

import in.lekhai.core.account_master.domain.GeneralLedgerSetting;
import in.lekhai.core.account_master.domain.SaleLedgerSetting;
import in.lekhai.core.account_master.repository.GeneralLedgerSettingRepository;
import in.lekhai.core.account_master.repository.SaleLedgerSettingRepository;
import in.lekhai.salesinvoice.calc.InvoiceCalculator;
import in.lekhai.salesinvoice.calc.InvoiceModels.TaxRow;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Maps (sale type, GST rate) to the sale and tax ledgers, and loads round-off / cess ledgers from General Ledger Setting.
 */
@Component
class SalesInvoiceLedgerResolver {

    static final String IN_STATE = "IN_STATE";
    static final String OUT_STATE = "OUT_STATE";

    private final SaleLedgerSettingRepository saleSettingRepository;
    private final GeneralLedgerSettingRepository generalSettingRepository;

    SalesInvoiceLedgerResolver(SaleLedgerSettingRepository saleSettingRepository,
                               GeneralLedgerSettingRepository generalSettingRepository) {
        this.saleSettingRepository = saleSettingRepository;
        this.generalSettingRepository = generalSettingRepository;
    }

    Map<BigDecimal, SaleLedgerSetting> settingsByRate(boolean interState) {
        String saleType = interState ? OUT_STATE : IN_STATE;
        return saleSettingRepository.findActiveBySaleType(saleType).stream()
                .collect(Collectors.toMap(s -> key(s.getGstRate()), Function.identity(), (a, b) -> a));
    }

    void requireSaleLedger(Long saleLedgerId) {
        if (saleLedgerId != null && !saleSettingRepository.existsActiveBySaleLedgerId(saleLedgerId)) {
            throw SalesInvoiceErrors.unprocessable(
                    String.format("Ledger [%d] is not a configured sale ledger", saleLedgerId));
        }
    }

    Long saleLedgerFor(Map<BigDecimal, SaleLedgerSetting> settings, BigDecimal gstRate, boolean interState) {
        SaleLedgerSetting setting = settings.get(key(gstRate));
        if (setting == null) {
            throw SalesInvoiceErrors.unprocessable(String.format(
                    "Configure a %s sale ledger for GST %s%% in Sale Ledger Setting",
                    interState ? "out-of-state" : "in-state", gstRate.stripTrailingZeros().toPlainString()));
        }
        return setting.getSaleLedgerId();
    }

    List<Long> taxLedgers(List<TaxRow> taxes, Map<BigDecimal, SaleLedgerSetting> settings) {
        GeneralLedgerSetting general = generalSettingRepository.findActive().orElse(null);
        return taxes.stream().map(row -> taxLedger(row, settings, general)).toList();
    }

    Long roundOffLedger() {
        return generalSettingRepository.findActive().map(GeneralLedgerSetting::getRoundOffLedgerId).orElse(null);
    }

    private Long taxLedger(TaxRow row, Map<BigDecimal, SaleLedgerSetting> settings, GeneralLedgerSetting general) {
        Long ledgerId = InvoiceCalculator.CESS.equals(row.taxType())
                ? (general == null ? null : general.getOutputCessLedgerId())
                : fromSetting(row, settings.get(key(row.slabRate())));
        if (ledgerId == null) {
            throw SalesInvoiceErrors.unprocessable(String.format("Configure the %s ledger for rate %s%%",
                    row.taxType(), row.slabRate().stripTrailingZeros().toPlainString()));
        }
        return ledgerId;
    }

    private Long fromSetting(TaxRow row, SaleLedgerSetting setting) {
        if (setting == null) {
            return null;
        }
        return switch (row.taxType()) {
            case InvoiceCalculator.CGST -> setting.getCgstLedgerId();
            case InvoiceCalculator.SGST -> setting.getSgstLedgerId();
            default -> setting.getIgstLedgerId();
        };
    }

    private BigDecimal key(BigDecimal rate) {
        return rate == null ? BigDecimal.ZERO : rate.stripTrailingZeros();
    }
}
