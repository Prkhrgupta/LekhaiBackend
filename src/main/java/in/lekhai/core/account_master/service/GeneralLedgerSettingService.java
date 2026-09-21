package in.lekhai.core.account_master.service;

import in.lekhai.contract.model.GeneralLedgerSettingRequest;
import in.lekhai.contract.model.GeneralLedgerSettingResponse;
import in.lekhai.core.account_master.domain.GeneralLedgerSetting;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.GeneralLedgerSettingRepository;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.error.controller.generalledgersetting.exception.GeneralLedgerSettingNotFoundException;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GeneralLedgerSettingService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final GeneralLedgerSettingRepository generalLedgerSettingRepository;
    private final LedgerRepository ledgerRepository;

    public GeneralLedgerSettingService(GeneralLedgerSettingRepository generalLedgerSettingRepository,
                                       LedgerRepository ledgerRepository) {
        this.generalLedgerSettingRepository = generalLedgerSettingRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @ShopContextTransactional
    public GeneralLedgerSettingResponse getGeneralLedgerSetting() {
        GeneralLedgerSetting setting = generalLedgerSettingRepository.findActive()
                .orElseThrow(GeneralLedgerSettingNotFoundException::new);
        return mapToResponse(setting, resolveLedgerNames(List.of(setting)));
    }

    @ShopContextTransactional
    public GeneralLedgerSettingResponse updateGeneralLedgerSetting(GeneralLedgerSettingRequest request) {
        GeneralLedgerSetting setting = generalLedgerSettingRepository.findActive()
                .orElseGet(GeneralLedgerSetting::new);
        mapToEntity(request, setting);
        GeneralLedgerSetting saved = generalLedgerSettingRepository.save(setting);
        log.info("Saved general ledger setting :: id {}", saved.getId());
        return mapToResponse(saved, resolveLedgerNames(List.of(saved)));
    }

    private GeneralLedgerSetting mapToEntity(GeneralLedgerSettingRequest request, GeneralLedgerSetting setting) {
        setting.setFreightPackingLedgerId(request.getFreightPackingLedgerId());
        setting.setRoundOffLedgerId(request.getRoundOffLedgerId());
        setting.setTdsPercentage(toBigDecimal(request.getTdsPercentage()));
        setting.setTdsLedgerId(request.getTdsLedgerId());
        setting.setTcsPercentage(toBigDecimal(request.getTcsPercentage()));
        setting.setTcsLedgerId(request.getTcsLedgerId());
        setting.setOutputCessLedgerId(request.getOutputCessLedgerId());
        setting.setInputCessLedgerId(request.getInputCessLedgerId());

        return setting;
    }

    private GeneralLedgerSettingResponse mapToResponse(GeneralLedgerSetting setting, Map<Long, String> ledgerNames) {
        return new GeneralLedgerSettingResponse()
                .id(setting.getId())
                .freightPackingLedgerId(setting.getFreightPackingLedgerId())
                .freightPackingLedgerName(ledgerNames.get(setting.getFreightPackingLedgerId()))
                .roundOffLedgerId(setting.getRoundOffLedgerId())
                .roundOffLedgerName(ledgerNames.get(setting.getRoundOffLedgerId()))
                .tdsPercentage(toDouble(setting.getTdsPercentage()))
                .tdsLedgerId(setting.getTdsLedgerId())
                .tdsLedgerName(ledgerNames.get(setting.getTdsLedgerId()))
                .tcsPercentage(toDouble(setting.getTcsPercentage()))
                .tcsLedgerId(setting.getTcsLedgerId())
                .tcsLedgerName(ledgerNames.get(setting.getTcsLedgerId()))
                .outputCessLedgerId(setting.getOutputCessLedgerId())
                .outputCessLedgerName(ledgerNames.get(setting.getOutputCessLedgerId()))
                .inputCessLedgerId(setting.getInputCessLedgerId())
                .inputCessLedgerName(ledgerNames.get(setting.getInputCessLedgerId()));
    }

    private Map<Long, String> resolveLedgerNames(Collection<GeneralLedgerSetting> settings) {
        Set<Long> ledgerIds = settings.stream()
                .flatMap(setting -> Stream.of(
                        setting.getFreightPackingLedgerId(),
                        setting.getRoundOffLedgerId(),
                        setting.getTdsLedgerId(),
                        setting.getTcsLedgerId(),
                        setting.getOutputCessLedgerId(),
                        setting.getInputCessLedgerId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ledgerIds.isEmpty()) {
            return Map.of();
        }

        return ledgerRepository.findAllById(ledgerIds).stream()
                .collect(Collectors.toMap(Ledger::getId, Ledger::getName, (first, second) -> first));
    }

    private static BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private static Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
