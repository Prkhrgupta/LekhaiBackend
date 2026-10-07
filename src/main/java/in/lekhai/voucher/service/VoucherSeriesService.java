package in.lekhai.voucher.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.lekhai.contract.model.CustomVoucherSeriesListResponse;
import in.lekhai.contract.model.CreateSeriesRequest;
import in.lekhai.contract.model.PaginationMeta;
import in.lekhai.contract.model.UpdateSeriesRequest;
import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.error.controller.voucher.exception.SeriesAlreadyExistsException;
import in.lekhai.error.controller.voucher.exception.SeriesFyMissingException;
import in.lekhai.error.controller.voucher.exception.SeriesImmutableException;
import in.lekhai.error.controller.voucher.exception.SeriesNotFoundException;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import in.lekhai.voucher.entity.VoucherType;
import in.lekhai.voucher.repository.CustomVoucherSeriesRepository;
import in.lekhai.voucher.repository.VoucherRepository;
import in.lekhai.voucher.util.FinancialYear;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VoucherSeriesService {

    private static final int MAX_BILL_LENGTH = 16;

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final CustomVoucherSeriesRepository seriesRepository;
    private final VoucherRepository voucherRepository;
    private final ObjectMapper objectMapper;

    public VoucherSeriesService(
            CustomVoucherSeriesRepository seriesRepository,
            VoucherRepository voucherRepository,
            ObjectMapper objectMapper) {
        this.seriesRepository = seriesRepository;
        this.voucherRepository = voucherRepository;
        this.objectMapper = objectMapper;
    }

    @ShopContextTransactional
    public in.lekhai.contract.model.CustomVoucherSeries createSeries(CreateSeriesRequest request) {
        VoucherType type = requireSalesType(request == null ? null : request.getVoucherType());
        String code = requireCode(request == null ? null : request.getSeriesCode());
        String fy = requireFinancialYear(request == null ? null : request.getFinancialYear());
        requireBillLength(request.getPrefix(), request.getSuffix(), 1L);
        if (seriesRepository.existsByVoucherTypeAndSeriesCodeAndFinancialYear(type, code, fy)) {
            throw new SeriesAlreadyExistsException(code, type.name(), fy);
        }
        in.lekhai.voucher.entity.CustomVoucherSeries saved =
                seriesRepository.save(mapToEntity(request, type, code, fy));
        log.info("Created voucher series id={} code={} fy={}", saved.getId(), code, fy);
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public CustomVoucherSeriesListResponse listSeries(
            String voucherType, String financialYear, int page, int size, List<String> sort) {
        VoucherType type = parseTypeFilter(voucherType);
        Pageable pageable = toPageable(page, size, sort);
        List<in.lekhai.voucher.entity.CustomVoucherSeries> rows = seriesRepository.findFiltered(
                type, financialYear, pageable.getPageSize(), pageable.getOffset());
        long total = seriesRepository.countFiltered(type, financialYear);
        if (financialYear != null && !financialYear.isBlank() && total == 0) {
            throw new SeriesFyMissingException(voucherType == null ? "" : voucherType, financialYear);
        }
        List<in.lekhai.contract.model.CustomVoucherSeries> data =
                rows.stream().map(this::mapToResponse).toList();
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new CustomVoucherSeriesListResponse()
                .data(data)
                .pagination(new PaginationMeta()
                        .page(page).size(size)
                        .totalElements(total).totalPages(totalPages));
    }

    @ShopContextTransactional
    public in.lekhai.contract.model.CustomVoucherSeries updateSeries(Long id, UpdateSeriesRequest request) {
        in.lekhai.voucher.entity.CustomVoucherSeries series = seriesRepository.findById(id)
                .orElseThrow(() -> new SeriesNotFoundException(id));
        String nextPrefix = request.getPrefix() == null ? series.getPrefix() : request.getPrefix();
        String nextSuffix = request.getSuffix() == null ? series.getSuffix() : request.getSuffix();
        requireBillLength(nextPrefix, nextSuffix, series.getNextNumber());
        rejectPrefixChangeAfterUse(series, nextPrefix, nextSuffix);
        if (request.getPrefix() != null) {
            series.setPrefix(request.getPrefix());
        }
        if (request.getSuffix() != null) {
            series.setSuffix(request.getSuffix());
        }
        if (request.getAllowManualEntry() != null) {
            series.setAllowManualEntry(request.getAllowManualEntry());
        }
        if (request.getPrintMetadata() != null) {
            series.setPrintMetadata(writeMetadata(request.getPrintMetadata()));
        }
        in.lekhai.voucher.entity.CustomVoucherSeries saved = seriesRepository.save(series);
        log.info("Updated voucher series id={}", saved.getId());
        return mapToResponse(saved);
    }

    private void rejectPrefixChangeAfterUse(
            in.lekhai.voucher.entity.CustomVoucherSeries series, String nextPrefix, String nextSuffix) {
        boolean changed = !equalsNullable(series.getPrefix(), nextPrefix)
                || !equalsNullable(series.getSuffix(), nextSuffix);
        if (!changed) {
            return;
        }
        if (voucherRepository.countBySeriesId(series.getId()) > 0) {
            throw new SeriesImmutableException(series.getId());
        }
    }

    private VoucherType requireSalesType(String voucherType) {
        if (voucherType == null || !voucherType.trim().equalsIgnoreCase(VoucherType.SALES.name())) {
            throw new LekhaiClientException(
                    "Only SALES voucher series are supported", HttpStatus.BAD_REQUEST);
        }
        return VoucherType.SALES;
    }

    private VoucherType parseTypeFilter(String voucherType) {
        if (voucherType == null || voucherType.isBlank()) {
            return null;
        }
        try {
            return VoucherType.valueOf(voucherType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new LekhaiClientException(
                    "Invalid voucher_type filter: " + voucherType, HttpStatus.BAD_REQUEST);
        }
    }

    private String requireCode(String seriesCode) {
        if (seriesCode == null || seriesCode.trim().isEmpty()) {
            throw new LekhaiClientException("series_code must not be blank", HttpStatus.BAD_REQUEST);
        }
        return seriesCode.trim().toUpperCase();
    }

    private String requireFinancialYear(String financialYear) {
        if (!FinancialYear.isValid(financialYear)) {
            throw new LekhaiClientException(
                    "financial_year must be YYYY-YY like 2025-26", HttpStatus.BAD_REQUEST);
        }
        return financialYear;
    }

    private void requireBillLength(String prefix, String suffix, Long number) {
        int length = (prefix == null ? 0 : prefix.length())
                + String.valueOf(number).length()
                + (suffix == null ? 0 : suffix.length());
        if (length > MAX_BILL_LENGTH) {
            throw new LekhaiClientException(
                    "prefix+number+suffix must fit 16 characters", HttpStatus.BAD_REQUEST);
        }
    }

    private in.lekhai.voucher.entity.CustomVoucherSeries mapToEntity(
            CreateSeriesRequest request, VoucherType type, String code, String fy) {
        in.lekhai.voucher.entity.CustomVoucherSeries series =
                new in.lekhai.voucher.entity.CustomVoucherSeries();
        series.setVoucherType(type);
        series.setSeriesCode(code);
        series.setFinancialYear(fy);
        series.setPrefix(request.getPrefix());
        series.setSuffix(request.getSuffix());
        series.setAllowManualEntry(
                request.getAllowManualEntry() == null ? Boolean.FALSE : request.getAllowManualEntry());
        series.setPrintMetadata(writeMetadata(request.getPrintMetadata()));
        series.setNextNumber(1L);
        return series;
    }

    private in.lekhai.contract.model.CustomVoucherSeries mapToResponse(
            in.lekhai.voucher.entity.CustomVoucherSeries series) {
        return new in.lekhai.contract.model.CustomVoucherSeries()
                .id(series.getId())
                .seriesCode(series.getSeriesCode())
                .voucherType(series.getVoucherType().name())
                .financialYear(series.getFinancialYear())
                .prefix(series.getPrefix())
                .suffix(series.getSuffix())
                .allowManualEntry(series.getAllowManualEntry())
                .printMetadata(readMetadata(series.getPrintMetadata()))
                .currentBillNo(series.getNextNumber() == null ? 0L : series.getNextNumber() - 1);
    }

    private Pageable toPageable(int page, int size, List<String> sort) {
        List<Sort.Order> orders = new ArrayList<>();
        if (sort != null) {
            for (String item : sort) {
                String[] parts = item.split(",");
                String property = toColumn(parts[0].trim());
                if (parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc")) {
                    orders.add(Sort.Order.desc(property));
                } else {
                    orders.add(Sort.Order.asc(property));
                }
            }
        }
        if (orders.isEmpty()) {
            orders.add(Sort.Order.asc("id"));
        }
        return PageRequest.of(page, size, Sort.by(orders));
    }

    private String toColumn(String property) {
        return switch (property) {
            case "seriesCode", "series_code" -> "id";
            case "financialYear", "financial_year" -> "id";
            default -> "id";
        };
    }

    private boolean equalsNullable(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    private String writeMetadata(Object metadata) {
        if (metadata == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            throw new LekhaiClientException("print_metadata must be JSON", HttpStatus.BAD_REQUEST);
        }
    }

    private Object readMetadata(String stored) {
        if (stored == null || stored.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(stored, Object.class);
        } catch (JsonProcessingException ex) {
            return stored;
        }
    }
}
