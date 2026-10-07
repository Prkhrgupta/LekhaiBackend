package in.lekhai.voucher.service;

import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.error.controller.voucher.exception.DuplicateBillNumberException;
import in.lekhai.error.controller.voucher.exception.SeriesFyMissingException;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import in.lekhai.voucher.entity.CustomVoucherSeries;
import in.lekhai.voucher.entity.VoucherType;
import in.lekhai.voucher.repository.CustomVoucherSeriesRepository;
import in.lekhai.voucher.repository.VoucherRepository;
import in.lekhai.voucher.util.FinancialYear;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SeriesReserveService {

    private final CustomVoucherSeriesRepository seriesRepository;
    private final VoucherRepository voucherRepository;

    public SeriesReserveService(
            CustomVoucherSeriesRepository seriesRepository,
            VoucherRepository voucherRepository) {
        this.seriesRepository = seriesRepository;
        this.voucherRepository = voucherRepository;
    }

    public record ReserveResult(Long billNo, Long firstMissingBillNo) {
    }

    @ShopContextTransactional
    public ReserveResult reserveAuto(VoucherType type, String seriesCode, LocalDate voucherDate) {
        return reserve(type, seriesCode, voucherDate, null);
    }

    @ShopContextTransactional
    public ReserveResult reserve(
            VoucherType type, String seriesCode, LocalDate voucherDate, Long manualBillNo) {
        String fy = FinancialYear.from(voucherDate);
        CustomVoucherSeries series = lockSeries(type, seriesCode, fy);
        if (manualBillNo == null) {
            return allocateAuto(series, fy);
        }
        return allocateManual(series, fy, manualBillNo);
    }

    private ReserveResult allocateAuto(CustomVoucherSeries series, String fy) {
        int attempts = 0;
        while (true) {
            Long billNo = series.getNextNumber();
            series.setNextNumber(billNo + 1);
            try {
                seriesRepository.save(series);
                return new ReserveResult(billNo, firstMissing(series.getId(), fy));
            } catch (DuplicateKeyException ex) {
                attempts++;
                if (attempts >= 3) {
                    throw ex;
                }
                series = reload(series.getId());
            }
        }
    }

    private ReserveResult allocateManual(CustomVoucherSeries series, String fy, Long manualBillNo) {
        requirePositive(manualBillNo);
        if (!Boolean.TRUE.equals(series.getAllowManualEntry())) {
            throw manualNotAllowed();
        }
        if (voucherRepository.existsBySeriesAndFyAndNumber(series.getId(), fy, manualBillNo)) {
            throw new DuplicateBillNumberException(manualBillNo);
        }
        if (manualBillNo >= series.getNextNumber()) {
            series.setNextNumber(manualBillNo + 1);
            seriesRepository.save(series);
        }
        return new ReserveResult(manualBillNo, firstMissing(series.getId(), fy));
    }

    private CustomVoucherSeries lockSeries(VoucherType type, String seriesCode, String fy) {
        return seriesRepository.findForUpdate(type, seriesCode, fy)
                .orElseThrow(() -> new SeriesFyMissingException(type.name(), fy));
    }

    private CustomVoucherSeries reload(Long id) {
        return seriesRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new LekhaiClientException(
                        "Voucher series missing on retry", HttpStatus.CONFLICT));
    }

    private Long firstMissing(Long seriesId, String fy) {
        List<Long> numbers = voucherRepository.findBillNumbersBySeriesAndFy(seriesId, fy);
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
            throw new LekhaiClientException("Bill no must be positive", HttpStatus.BAD_REQUEST);
        }
    }

    private LekhaiClientException manualNotAllowed() {
        return new LekhaiClientException(
                "Manual bill entry is not allowed for this series", HttpStatus.BAD_REQUEST);
    }
}
