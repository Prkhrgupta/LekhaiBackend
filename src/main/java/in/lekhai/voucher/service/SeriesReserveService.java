package in.lekhai.voucher.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import in.lekhai.error.controller.voucher.exception.DuplicateBillNumberException;
import in.lekhai.error.controller.voucher.exception.SeriesFyMissingException;
import in.lekhai.error.controller.voucher.exception.SeriesValidationException;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import in.lekhai.voucher.entity.CustomVoucherSeries;
import in.lekhai.voucher.entity.VoucherType;
import in.lekhai.voucher.repository.CustomVoucherSeriesRepository;
import in.lekhai.voucher.repository.VoucherRepository;
import in.lekhai.voucher.util.FinancialYear;

@Service
public class SeriesReserveService {

    private static final int MAX_BILL_LENGTH = 16;

    private final CustomVoucherSeriesRepository seriesRepository;
    private final VoucherRepository voucherRepository;

    public SeriesReserveService(
            CustomVoucherSeriesRepository seriesRepository,
            VoucherRepository voucherRepository) {
        this.seriesRepository = seriesRepository;
        this.voucherRepository = voucherRepository;
    }

    public record ReserveResult(Long billNo, Long firstMissingBillNo, Long seriesId,
                                  String prefix, String suffix) {
    }

    @ShopContextTransactional
    public ReserveResult reserveAuto(VoucherType type, String seriesCode, LocalDate voucherDate) {
        return reserve(type, seriesCode, voucherDate, null);
    }

    @ShopContextTransactional
    public ReserveResult reserve(
            VoucherType type, String seriesCode, LocalDate voucherDate, Long manualBillNo) {
        String code = requireCode(seriesCode);
        String fy = FinancialYear.from(voucherDate);
        CustomVoucherSeries series = lockSeries(type, code, fy);
        if (manualBillNo == null) {
            return allocateAuto(series, fy);
        }
        return allocateManual(series, fy, manualBillNo);
    }

    private ReserveResult allocateAuto(CustomVoucherSeries series, String fy) {
        // Why no retry here: FOR UPDATE serializes series bumps; the true duplicate
        // backstop is the voucher unique constraint at insert time, which retries there once wired.
        Long billNo = series.getNextNumber();
        requireBillLength(series, billNo);
        series.setNextNumber(billNo + 1);
        seriesRepository.save(series);
        return new ReserveResult(billNo, firstMissingIncluding(series.getId(), fy, billNo),
                series.getId(), series.getPrefix(), series.getSuffix());
    }

    private ReserveResult allocateManual(CustomVoucherSeries series, String fy, Long manualBillNo) {
        requirePositive(manualBillNo);
        requireBillLength(series, manualBillNo);
        if (!Boolean.TRUE.equals(series.getAllowManualEntry())) {
            throw new SeriesValidationException("Manual bill entry is not allowed for this series");
        }
        if (voucherRepository.existsBySeriesAndFyAndNumber(series.getId(), fy, manualBillNo)) {
            throw new DuplicateBillNumberException(manualBillNo);
        }
        if (manualBillNo >= series.getNextNumber()) {
            series.setNextNumber(manualBillNo + 1);
            seriesRepository.save(series);
        }
        return new ReserveResult(manualBillNo, firstMissingIncluding(series.getId(), fy, manualBillNo),
                series.getId(), series.getPrefix(), series.getSuffix());
    }

    private CustomVoucherSeries lockSeries(VoucherType type, String seriesCode, String fy) {
        return seriesRepository.findForUpdate(type, seriesCode, fy)
                .orElseThrow(() -> new SeriesFyMissingException(type.name(), fy));
    }

    private String requireCode(String seriesCode) {
        if (seriesCode == null || seriesCode.trim().isEmpty()) {
            throw new SeriesValidationException("series_code must not be blank");
        }
        return seriesCode.trim().toUpperCase();
    }

    private Long firstMissingIncluding(Long seriesId, String fy, Long justAllocated) {
        List<Long> numbers =
                new ArrayList<>(voucherRepository.findBillNumbersBySeriesAndFy(seriesId, fy));
        numbers.add(justAllocated);
        numbers.sort(Long::compareTo);
        long expected = 1L;
        for (Long actual : numbers) {
            if (actual == null) {
                continue;
            }
            if (actual > expected) {
                return expected;
            }
            if (actual.equals(expected)) {
                expected++;
            }
        }
        return null;
    }

    private void requirePositive(Long manualBillNo) {
        if (manualBillNo == null || manualBillNo <= 0) {
            throw new SeriesValidationException("Bill no must be positive");
        }
    }

    private void requireBillLength(CustomVoucherSeries series, Long number) {
        int length = (series.getPrefix() == null ? 0 : series.getPrefix().length())
                + String.valueOf(number).length()
                + (series.getSuffix() == null ? 0 : series.getSuffix().length());
        if (length > MAX_BILL_LENGTH) {
            throw new SeriesValidationException("prefix+number+suffix must fit 16 characters");
        }
    }
}
