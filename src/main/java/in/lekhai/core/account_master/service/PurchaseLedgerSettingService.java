package in.lekhai.core.account_master.service;

import in.lekhai.contract.model.DropdownItem;
import in.lekhai.contract.model.GstTaxability;
import in.lekhai.contract.model.ItcEligibility;
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
import in.lekhai.core.account_master.utils.GstLedgerSettingRules;
import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.error.controller.purchaseledgersetting.exception.PurchaseLedgerSettingNotFoundException;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
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
                        .label(setting.getPurchaseType() == null
                                ? ledgerNames.get(setting.getPurchaseLedgerId())
                                : ledgerNames.get(setting.getPurchaseLedgerId())
                                        + " (" + setting.getPurchaseType() + ")"))
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

    /**
     * Validates the request against the GST rules and copies it onto
     * {@code setting}. The CGST/SGST/IGST percentages are never taken from the
     * client: they are derived from the GST rate and the purchase type.
     */
    private PurchaseLedgerSetting mapToEntity(PurchaseLedgerSettingRequest request, PurchaseLedgerSetting setting) {
        PurchaseType purchaseType = request.getPurchaseType();
        if (request.getPurchaseLedgerId() == null || purchaseType == null) {
            throw new LekhaiClientException("Purchase ledger and purchase type are required", HttpStatus.BAD_REQUEST);
        }
        GstTaxability taxability = request.getTaxability() == null
                ? GstTaxability.TAXABLE : request.getTaxability();
        ItcEligibility itcEligibility = request.getItcEligibility() == null
                ? ItcEligibility.ELIGIBLE : request.getItcEligibility();
        boolean reverseCharge = Boolean.TRUE.equals(request.getReverseCharge());
        GstLedgerSettingRules.SupplyKind kind = GstLedgerSettingRules.kindOf(purchaseType);
        BigDecimal requestedRate = toBigDecimal(request.getGstRate());
        BigDecimal cessPercentage = toBigDecimal(request.getCessPercentage());

        GstLedgerSettingRules.validateTaxLedgers(kind, taxability, requestedRate,
                request.getCgstLedgerId(), request.getSgstLedgerId(), request.getIgstLedgerId(),
                cessPercentage, request.getCessLedgerId());
        GstLedgerSettingRules.validateReverseCharge(reverseCharge, purchaseType, taxability,
                request.getRcmCgstPayableLedgerId(),
                request.getRcmSgstPayableLedgerId(),
                request.getRcmIgstPayableLedgerId());
        GstLedgerSettingRules.requireLedgersExist(ledgerRepository, Arrays.asList(
                request.getPurchaseLedgerId(),
                request.getCgstLedgerId(),
                request.getSgstLedgerId(),
                request.getIgstLedgerId(),
                request.getCessLedgerId(),
                request.getRcmCgstPayableLedgerId(),
                request.getRcmSgstPayableLedgerId(),
                request.getRcmIgstPayableLedgerId(),
                request.getFreightPackingLedgerId(),
                request.getRoundOffLedgerId()));

        long excludeId = setting.getId() == null ? 0L : setting.getId();
        if (purchaseLedgerSettingRepository.existsActiveByPurchaseLedgerIdAndPurchaseType(
                request.getPurchaseLedgerId(), purchaseType.getValue(), excludeId)) {
            throw new LekhaiClientException(
                    "A " + purchaseType.getValue() + " setting already exists for this purchase ledger",
                    HttpStatus.CONFLICT);
        }

        BigDecimal gstRate = GstLedgerSettingRules.effectiveRate(kind, taxability, requestedRate);
        GstLedgerSettingRules.TaxPercentages percentages =
                GstLedgerSettingRules.derivePercentages(kind, taxability, gstRate);

        setting.setPurchaseLedgerId(request.getPurchaseLedgerId());
        setting.setPurchaseType(purchaseType.getValue());
        setting.setTaxability(taxability.getValue());
        setting.setItcEligibility(itcEligibility.getValue());
        setting.setReverseCharge(reverseCharge);
        setting.setGstRate(gstRate);

        setting.setCgstPercentage(percentages.cgst());
        setting.setCgstLedgerId(request.getCgstLedgerId());
        setting.setSgstPercentage(percentages.sgst());
        setting.setSgstLedgerId(request.getSgstLedgerId());
        setting.setIgstPercentage(percentages.igst());
        setting.setIgstLedgerId(request.getIgstLedgerId());
        setting.setCessPercentage(cessPercentage == null ? BigDecimal.ZERO : cessPercentage);
        setting.setCessLedgerId(request.getCessLedgerId());

        setting.setRcmCgstPayableLedgerId(request.getRcmCgstPayableLedgerId());
        setting.setRcmSgstPayableLedgerId(request.getRcmSgstPayableLedgerId());
        setting.setRcmIgstPayableLedgerId(request.getRcmIgstPayableLedgerId());

        setting.setFreightPackingLedgerId(request.getFreightPackingLedgerId());
        setting.setRoundOffLedgerId(request.getRoundOffLedgerId());

        return setting;
    }

    private PurchaseLedgerSettingResponse mapToResponse(PurchaseLedgerSetting setting, Map<Long, String> ledgerNames) {
        return new PurchaseLedgerSettingResponse()
                .id(setting.getId())
                .purchaseLedgerId(setting.getPurchaseLedgerId())
                .purchaseLedgerName(ledgerNames.get(setting.getPurchaseLedgerId()))
                .purchaseType(setting.getPurchaseType() == null
                        ? null : PurchaseType.fromValue(setting.getPurchaseType()))
                .taxability(setting.getTaxability() == null
                        ? null : GstTaxability.fromValue(setting.getTaxability()))
                .itcEligibility(setting.getItcEligibility() == null
                        ? null : ItcEligibility.fromValue(setting.getItcEligibility()))
                .reverseCharge(setting.getReverseCharge())
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
                .rcmCgstPayableLedgerId(setting.getRcmCgstPayableLedgerId())
                .rcmCgstPayableLedgerName(ledgerNames.get(setting.getRcmCgstPayableLedgerId()))
                .rcmSgstPayableLedgerId(setting.getRcmSgstPayableLedgerId())
                .rcmSgstPayableLedgerName(ledgerNames.get(setting.getRcmSgstPayableLedgerId()))
                .rcmIgstPayableLedgerId(setting.getRcmIgstPayableLedgerId())
                .rcmIgstPayableLedgerName(ledgerNames.get(setting.getRcmIgstPayableLedgerId()))
                .freightPackingLedgerId(setting.getFreightPackingLedgerId())
                .freightPackingLedgerName(ledgerNames.get(setting.getFreightPackingLedgerId()))
                .roundOffLedgerId(setting.getRoundOffLedgerId())
                .roundOffLedgerName(ledgerNames.get(setting.getRoundOffLedgerId()));
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
                        setting.getRcmCgstPayableLedgerId(),
                        setting.getRcmSgstPayableLedgerId(),
                        setting.getRcmIgstPayableLedgerId(),
                        setting.getFreightPackingLedgerId(),
                        setting.getRoundOffLedgerId()))
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
