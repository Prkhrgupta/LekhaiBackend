package in.lekhai.core.account_master.service;

import in.lekhai.contract.model.DropdownItem;
import in.lekhai.contract.model.GstTaxability;
import in.lekhai.contract.model.PaginationMeta;
import in.lekhai.contract.model.SaleLedgerSettingRequest;
import in.lekhai.contract.model.SaleLedgerSettingResponse;
import in.lekhai.contract.model.SaleLedgerSettingSearchableField;
import in.lekhai.contract.model.SaleLedgerSettingSummaryPageResponse;
import in.lekhai.contract.model.SaleType;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.domain.SaleLedgerSetting;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.account_master.repository.SaleLedgerSettingRepository;
import in.lekhai.core.account_master.utils.GstLedgerSettingRules;
import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.error.controller.saleledgersetting.exception.SaleLedgerSettingNotFoundException;
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
public class SaleLedgerSettingService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final SaleLedgerSettingRepository saleLedgerSettingRepository;
    private final LedgerRepository ledgerRepository;

    public SaleLedgerSettingService(SaleLedgerSettingRepository saleLedgerSettingRepository,
                                    LedgerRepository ledgerRepository) {
        this.saleLedgerSettingRepository = saleLedgerSettingRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @ShopContextTransactional
    public SaleLedgerSettingResponse createSaleLedgerSetting(SaleLedgerSettingRequest request) {
        SaleLedgerSetting setting = mapToEntity(request, new SaleLedgerSetting());
        SaleLedgerSetting saved = saleLedgerSettingRepository.save(setting);
        log.info("Saved sale ledger setting :: id {}", saved.getId());
        return mapToResponse(saved, resolveLedgerNames(List.of(saved)));
    }

    @ShopContextTransactional
    public SaleLedgerSettingResponse updateSaleLedgerSetting(Long id, SaleLedgerSettingRequest request) {
        SaleLedgerSetting setting = saleLedgerSettingRepository.findById(id)
                .orElseThrow(() -> new SaleLedgerSettingNotFoundException(id));
        mapToEntity(request, setting);
        SaleLedgerSetting saved = saleLedgerSettingRepository.save(setting);
        log.info("Updated sale ledger setting :: id {}", saved.getId());
        return mapToResponse(saved, resolveLedgerNames(List.of(saved)));
    }

    @ShopContextTransactional
    public void deleteSaleLedgerSetting(Long id) {
        SaleLedgerSetting setting = saleLedgerSettingRepository.findById(id)
                .orElseThrow(() -> new SaleLedgerSettingNotFoundException(id));
        setting.setDeleted(Boolean.TRUE);
        saleLedgerSettingRepository.save(setting);
        log.info("Soft-deleted sale ledger setting :: id {}", setting.getId());
    }

    @ShopContextTransactional
    public SaleLedgerSettingResponse getSaleLedgerSettingById(Long id) {
        SaleLedgerSetting setting = saleLedgerSettingRepository.findById(id)
                .orElseThrow(() -> new SaleLedgerSettingNotFoundException(id));
        return mapToResponse(setting, resolveLedgerNames(List.of(setting)));
    }

    @ShopContextTransactional
    public List<DropdownItem> listSaleLedgerSettings() {
        List<SaleLedgerSetting> settings = StreamSupport
                .stream(saleLedgerSettingRepository.findAll().spliterator(), false)
                .filter(setting -> !Boolean.TRUE.equals(setting.getDeleted()))
                .toList();
        Map<Long, String> ledgerNames = resolveLedgerNames(settings);
        return settings.stream()
                .map(setting -> new DropdownItem()
                        .id(setting.getId())
                        .label(setting.getSaleType() == null
                                ? ledgerNames.get(setting.getSaleLedgerId())
                                : ledgerNames.get(setting.getSaleLedgerId()) + " (" + setting.getSaleType() + ")"))
                .toList();
    }

    @ShopContextTransactional
    public SaleLedgerSettingSummaryPageResponse listSaleLedgerSettingSummaries(
            SaleLedgerSettingSearchableField searchableField,
            String searchText,
            Pageable pageable) {
        Page<SaleLedgerSetting> settingsPage;
        if (searchText != null && !searchText.trim().isEmpty()
                && searchableField == SaleLedgerSettingSearchableField.SALE_LEDGER_NAME) {
            List<SaleLedgerSetting> settings = saleLedgerSettingRepository
                    .findActiveBySaleLedgerNameContainingIgnoreCase(
                            searchText.trim(), pageable.getPageSize(), pageable.getOffset());
            long total = saleLedgerSettingRepository
                    .countActiveBySaleLedgerNameContainingIgnoreCase(searchText.trim());
            settingsPage = new PageImpl<>(settings, pageable, total);
        } else {
            List<SaleLedgerSetting> settings = saleLedgerSettingRepository
                    .findAllActive(pageable.getPageSize(), pageable.getOffset());
            long total = saleLedgerSettingRepository.countAllActive();
            settingsPage = new PageImpl<>(settings, pageable, total);
        }

        Map<Long, String> ledgerNames = resolveLedgerNames(settingsPage.getContent());
        List<SaleLedgerSettingResponse> data = settingsPage.getContent().stream()
                .map(setting -> mapToResponse(setting, ledgerNames))
                .toList();

        return new SaleLedgerSettingSummaryPageResponse()
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
     * client: they are derived from the GST rate and the sale type.
     */
    private SaleLedgerSetting mapToEntity(SaleLedgerSettingRequest request, SaleLedgerSetting setting) {
        SaleType saleType = request.getSaleType();
        if (request.getSaleLedgerId() == null || saleType == null) {
            throw new LekhaiClientException("Sale ledger and sale type are required", HttpStatus.BAD_REQUEST);
        }
        GstTaxability taxability = request.getTaxability() == null
                ? GstTaxability.TAXABLE : request.getTaxability();
        GstLedgerSettingRules.SupplyKind kind = GstLedgerSettingRules.kindOf(saleType);
        BigDecimal requestedRate = toBigDecimal(request.getGstRate());
        BigDecimal cessPercentage = toBigDecimal(request.getCessPercentage());

        GstLedgerSettingRules.validateTaxLedgers(kind, taxability, requestedRate,
                request.getCgstLedgerId(), request.getSgstLedgerId(), request.getIgstLedgerId(),
                cessPercentage, request.getCessLedgerId());
        GstLedgerSettingRules.requireLedgersExist(ledgerRepository, Arrays.asList(
                request.getSaleLedgerId(),
                request.getCgstLedgerId(),
                request.getSgstLedgerId(),
                request.getIgstLedgerId(),
                request.getCessLedgerId(),
                request.getFreightPackingLedgerId(),
                request.getRoundOffLedgerId()));

        long excludeId = setting.getId() == null ? 0L : setting.getId();
        if (saleLedgerSettingRepository.existsActiveBySaleLedgerIdAndSaleType(
                request.getSaleLedgerId(), saleType.getValue(), excludeId)) {
            throw new LekhaiClientException(
                    "A " + saleType.getValue() + " setting already exists for this sale ledger",
                    HttpStatus.CONFLICT);
        }

        BigDecimal gstRate = GstLedgerSettingRules.effectiveRate(kind, taxability, requestedRate);
        GstLedgerSettingRules.TaxPercentages percentages =
                GstLedgerSettingRules.derivePercentages(kind, taxability, gstRate);

        setting.setSaleLedgerId(request.getSaleLedgerId());
        setting.setSaleType(saleType.getValue());
        setting.setTaxability(taxability.getValue());
        setting.setGstRate(gstRate);

        setting.setCgstPercentage(percentages.cgst());
        setting.setCgstLedgerId(request.getCgstLedgerId());
        setting.setSgstPercentage(percentages.sgst());
        setting.setSgstLedgerId(request.getSgstLedgerId());
        setting.setIgstPercentage(percentages.igst());
        setting.setIgstLedgerId(request.getIgstLedgerId());
        setting.setCessPercentage(cessPercentage == null ? BigDecimal.ZERO : cessPercentage);
        setting.setCessLedgerId(request.getCessLedgerId());

        setting.setFreightPackingLedgerId(request.getFreightPackingLedgerId());
        setting.setRoundOffLedgerId(request.getRoundOffLedgerId());

        return setting;
    }

    private SaleLedgerSettingResponse mapToResponse(SaleLedgerSetting setting, Map<Long, String> ledgerNames) {
        return new SaleLedgerSettingResponse()
                .id(setting.getId())
                .saleLedgerId(setting.getSaleLedgerId())
                .saleLedgerName(ledgerNames.get(setting.getSaleLedgerId()))
                .saleType(setting.getSaleType() == null ? null : SaleType.fromValue(setting.getSaleType()))
                .taxability(setting.getTaxability() == null
                        ? null : GstTaxability.fromValue(setting.getTaxability()))
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
                .roundOffLedgerName(ledgerNames.get(setting.getRoundOffLedgerId()));
    }

    /**
     * One lookup for every ledger referenced anywhere in {@code settings}, so a
     * page of rows costs a single query instead of eight per row.
     */
    private Map<Long, String> resolveLedgerNames(Collection<SaleLedgerSetting> settings) {
        Set<Long> ledgerIds = settings.stream()
                .flatMap(setting -> Stream.of(
                        setting.getSaleLedgerId(),
                        setting.getCgstLedgerId(),
                        setting.getSgstLedgerId(),
                        setting.getIgstLedgerId(),
                        setting.getCessLedgerId(),
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
