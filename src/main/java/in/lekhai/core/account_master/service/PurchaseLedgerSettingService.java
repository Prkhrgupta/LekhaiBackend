package in.lekhai.core.account_master.service;

import in.lekhai.contract.model.DropdownItem;
import in.lekhai.contract.model.PaginationMeta;
import in.lekhai.contract.model.PurchaseLedgerSettingRequest;
import in.lekhai.contract.model.PurchaseLedgerSettingResponse;
import in.lekhai.contract.model.PurchaseLedgerSettingSearchableField;
import in.lekhai.contract.model.PurchaseLedgerSettingSummaryPageResponse;
import in.lekhai.contract.model.PurchaseType;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.domain.PurchaseLedgerSetting;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.account_master.repository.PurchaseLedgerSettingRepository;
import in.lekhai.error.controller.purchaseledgersetting.exception.PurchaseLedgerSettingNotFoundException;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class PurchaseLedgerSettingService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final PurchaseLedgerSettingRepository purchaseLedgerSettingRepository;
    private final LedgerRepository ledgerRepository;

    public PurchaseLedgerSettingService(PurchaseLedgerSettingRepository purchaseLedgerSettingRepository,
                                        LedgerRepository ledgerRepository) {
        this.purchaseLedgerSettingRepository = purchaseLedgerSettingRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @ShopContextTransactional
    public PurchaseLedgerSettingResponse createPurchaseLedgerSetting(PurchaseLedgerSettingRequest request) {
        PurchaseLedgerSetting setting = mapToEntity(request, new PurchaseLedgerSetting());
        PurchaseLedgerSetting saved = purchaseLedgerSettingRepository.save(setting);
        log.info("Saved purchase ledger setting :: id {}", saved.getId());
        return mapToResponse(saved, resolveLedgerNames(List.of(saved)));
    }

    @ShopContextTransactional
    public PurchaseLedgerSettingResponse updatePurchaseLedgerSetting(Long id, PurchaseLedgerSettingRequest request) {
        PurchaseLedgerSetting setting = purchaseLedgerSettingRepository.findById(id)
                .orElseThrow(() -> new PurchaseLedgerSettingNotFoundException(id));
        mapToEntity(request, setting);
        PurchaseLedgerSetting saved = purchaseLedgerSettingRepository.save(setting);
        log.info("Updated purchase ledger setting :: id {}", saved.getId());
        return mapToResponse(saved, resolveLedgerNames(List.of(saved)));
    }

    @ShopContextTransactional
    public void deletePurchaseLedgerSetting(Long id) {
        PurchaseLedgerSetting setting = purchaseLedgerSettingRepository.findById(id)
                .orElseThrow(() -> new PurchaseLedgerSettingNotFoundException(id));
        setting.setDeleted(Boolean.TRUE);
        purchaseLedgerSettingRepository.save(setting);
        log.info("Soft-deleted purchase ledger setting :: id {}", setting.getId());
    }

    @ShopContextTransactional
    public PurchaseLedgerSettingResponse getPurchaseLedgerSettingById(Long id) {
        PurchaseLedgerSetting setting = purchaseLedgerSettingRepository.findById(id)
                .orElseThrow(() -> new PurchaseLedgerSettingNotFoundException(id));
        return mapToResponse(setting, resolveLedgerNames(List.of(setting)));
    }

    @ShopContextTransactional
    public List<DropdownItem> listPurchaseLedgerSettings() {
        List<PurchaseLedgerSetting> settings = StreamSupport
                .stream(purchaseLedgerSettingRepository.findAll().spliterator(), false)
                .filter(setting -> !Boolean.TRUE.equals(setting.getDeleted()))
                .toList();
        Map<Long, String> ledgerNames = resolveLedgerNames(settings);
        return settings.stream()
                .map(setting -> new DropdownItem()
                        .id(setting.getId())
                        .label(ledgerNames.get(setting.getPurchaseLedgerId())))
                .toList();
    }

    @ShopContextTransactional
    public PurchaseLedgerSettingSummaryPageResponse listPurchaseLedgerSettingSummaries(
            PurchaseLedgerSettingSearchableField searchableField,
            String searchText,
            Pageable pageable) {
        Page<PurchaseLedgerSetting> settingsPage;
        if (searchText != null && !searchText.trim().isEmpty()
                && searchableField == PurchaseLedgerSettingSearchableField.PURCHASE_LEDGER_NAME) {
            List<PurchaseLedgerSetting> settings = purchaseLedgerSettingRepository
                    .findActiveByPurchaseLedgerNameContainingIgnoreCase(
                            searchText.trim(), pageable.getPageSize(), pageable.getOffset());
            long total = purchaseLedgerSettingRepository
                    .countActiveByPurchaseLedgerNameContainingIgnoreCase(searchText.trim());
            settingsPage = new PageImpl<>(settings, pageable, total);
        } else {
            List<PurchaseLedgerSetting> settings = purchaseLedgerSettingRepository
                    .findAllActive(pageable.getPageSize(), pageable.getOffset());
            long total = purchaseLedgerSettingRepository.countAllActive();
            settingsPage = new PageImpl<>(settings, pageable, total);
        }

        Map<Long, String> ledgerNames = resolveLedgerNames(settingsPage.getContent());
        List<PurchaseLedgerSettingResponse> data = settingsPage.getContent().stream()
                .map(setting -> mapToResponse(setting, ledgerNames))
                .toList();

        return new PurchaseLedgerSettingSummaryPageResponse()
                .data(data)
                .pagination(new PaginationMeta()
                        .page(settingsPage.getNumber())
                        .size(settingsPage.getSize())
                        .totalElements(settingsPage.getTotalElements())
                        .totalPages(settingsPage.getTotalPages()));
    }

    private PurchaseLedgerSetting mapToEntity(PurchaseLedgerSettingRequest request, PurchaseLedgerSetting setting) {
        setting.setPurchaseLedgerId(request.getPurchaseLedgerId());
        setting.setPurchaseType(request.getPurchaseType() == null ? null : request.getPurchaseType().getValue());
        setting.setGstRate(toBigDecimal(request.getGstRate()));

        setting.setCgstPercentage(toBigDecimal(request.getCgstPercentage()));
        setting.setCgstLedgerId(request.getCgstLedgerId());
        setting.setSgstPercentage(toBigDecimal(request.getSgstPercentage()));
        setting.setSgstLedgerId(request.getSgstLedgerId());
        setting.setIgstPercentage(toBigDecimal(request.getIgstPercentage()));
        setting.setIgstLedgerId(request.getIgstLedgerId());
        setting.setCessPercentage(toBigDecimal(request.getCessPercentage()));
        setting.setCessLedgerId(request.getCessLedgerId());

        setting.setFreightPackingLedgerId(request.getFreightPackingLedgerId());
        setting.setRoundOffLedgerId(request.getRoundOffLedgerId());
        setting.setTdsPercentage(toBigDecimal(request.getTdsPercentage()));
        setting.setTdsLedgerId(request.getTdsLedgerId());

        return setting;
    }

    private PurchaseLedgerSettingResponse mapToResponse(PurchaseLedgerSetting setting, Map<Long, String> ledgerNames) {
        return new PurchaseLedgerSettingResponse()
                .id(setting.getId())
                .purchaseLedgerId(setting.getPurchaseLedgerId())
                .purchaseLedgerName(ledgerNames.get(setting.getPurchaseLedgerId()))
                .purchaseType(setting.getPurchaseType() == null
                        ? null : PurchaseType.fromValue(setting.getPurchaseType()))
                .gstRate(toDouble(setting.getGstRate()))
                .cgstPercentage(toDouble(setting.getCgstPercentage()))
                .cgstLedgerId(setting.getCgstLedgerId())
                .cgstLedgerName(ledgerNames.get(setting.getCgstLedgerId()))
                .sgstPercentage(toDouble(setting.getSgstPercentage()))
                .sgstLedgerId(setting.getSgstLedgerId())
                .sgstLedgerName(ledgerNames.get(setting.getSgstLedgerId()))
                .igstPercentage(toDouble(setting.getIgstPercentage()))
                .igstLedgerId(setting.getIgstLedgerId())
                .igstLedgerName(ledgerNames.get(setting.getIgstLedgerId()))
                .cessPercentage(toDouble(setting.getCessPercentage()))
                .cessLedgerId(setting.getCessLedgerId())
                .cessLedgerName(ledgerNames.get(setting.getCessLedgerId()))
                .freightPackingLedgerId(setting.getFreightPackingLedgerId())
                .freightPackingLedgerName(ledgerNames.get(setting.getFreightPackingLedgerId()))
                .roundOffLedgerId(setting.getRoundOffLedgerId())
                .roundOffLedgerName(ledgerNames.get(setting.getRoundOffLedgerId()))
                .tdsPercentage(toDouble(setting.getTdsPercentage()))
                .tdsLedgerId(setting.getTdsLedgerId())
                .tdsLedgerName(ledgerNames.get(setting.getTdsLedgerId()));
    }

    /**
     * One lookup for every ledger referenced anywhere in {@code settings}, so a
     * page of rows costs a single query instead of eight per row.
     */
    private Map<Long, String> resolveLedgerNames(Collection<PurchaseLedgerSetting> settings) {
        Set<Long> ledgerIds = settings.stream()
                .flatMap(setting -> Stream.of(
                        setting.getPurchaseLedgerId(),
                        setting.getCgstLedgerId(),
                        setting.getSgstLedgerId(),
                        setting.getIgstLedgerId(),
                        setting.getCessLedgerId(),
                        setting.getFreightPackingLedgerId(),
                        setting.getRoundOffLedgerId(),
                        setting.getTdsLedgerId()))
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
