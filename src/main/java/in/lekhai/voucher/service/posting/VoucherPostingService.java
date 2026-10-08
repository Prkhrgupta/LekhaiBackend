package in.lekhai.voucher.service.posting;

import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import in.lekhai.voucher.dto.posting.PostingEntry;
import in.lekhai.voucher.dto.posting.PostingRequest;
import in.lekhai.voucher.dto.posting.PostingResult;
import in.lekhai.voucher.entity.Voucher;
import in.lekhai.voucher.entity.VoucherEntry;
import in.lekhai.voucher.mapper.VoucherPostingMapper;
import in.lekhai.voucher.repository.VoucherEntryRepository;
import in.lekhai.voucher.repository.VoucherRepository;
import in.lekhai.voucher.service.SeriesReserveService;
import in.lekhai.voucher.service.VoucherCounterService;
import in.lekhai.voucher.util.FinancialYear;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
public class VoucherPostingService {
    private final VoucherCounterService voucherCounterService;
    private final SeriesReserveService seriesReserveService;
    private final VoucherPostingMapper voucherPostingMapper;
    private final VoucherRepository voucherRepository;
    private final VoucherEntryRepository voucherEntryRepository;
    private final LedgerRepository ledgerRepository;

    public VoucherPostingService(
            VoucherCounterService voucherCounterService,
            SeriesReserveService seriesReserveService,
            VoucherPostingMapper voucherPostingMapper,
            VoucherRepository voucherRepository,
            VoucherEntryRepository voucherEntryRepository,
            LedgerRepository ledgerRepository
    ) {
        this.voucherCounterService = voucherCounterService;
        this.seriesReserveService = seriesReserveService;
        this.voucherPostingMapper = voucherPostingMapper;
        this.voucherRepository = voucherRepository;
        this.voucherEntryRepository = voucherEntryRepository;
        this.ledgerRepository = ledgerRepository;
    }

    /**
     * generate a voucher number
     *  enter voucher and voucher entry
     */
    @ShopContextTransactional
    public PostingResult post(PostingRequest request) {
        validate(request);
        if (hasSeries(request)) {
            return postWithSeries(request);
        }
        return postWithCounter(request);
    }

    private PostingResult postWithCounter(PostingRequest request) {
        Long voucherNumber = voucherCounterService.reserveNextNumber(request.voucherType());
        Voucher savedVoucher = saveVoucher(request, voucherNumber, null, null);
        String display = request.voucherType().getPrefix() + "-" + voucherNumber;
        return new PostingResult(savedVoucher, display, null);
    }

    private PostingResult postWithSeries(PostingRequest request) {
        SeriesReserveService.ReserveResult reserved = seriesReserveService.reserve(
                request.voucherType(), request.seriesCode(), request.voucherDate(), request.manualBillNo());
        String financialYear = FinancialYear.from(request.voucherDate());
        Voucher savedVoucher = saveVoucher(
                request, reserved.billNo(), reserved.seriesId(), financialYear);
        String display = formatSeriesDisplay(reserved.prefix(), reserved.billNo(), reserved.suffix());
        return new PostingResult(savedVoucher, display, reserved.firstMissingBillNo());
    }

    private Voucher saveVoucher(
            PostingRequest request, Long voucherNumber, Long seriesId, String financialYear) {
        Voucher voucher = voucherPostingMapper.mapToVoucher(request, voucherNumber);
        voucher.setSeriesId(seriesId);
        voucher.setFinancialYear(financialYear);
        Voucher savedVoucher = voucherRepository.save(voucher);

        int lineNumber = 1;
        List<VoucherEntry> entries = new ArrayList<>();
        for (PostingEntry entry : request.entries()) {
            entries.add(voucherPostingMapper.mapToVoucherEntry(
                    entry,
                    savedVoucher.getId(),
                    lineNumber++
            ));
        }
        voucherEntryRepository.saveAll(entries);
        return savedVoucher;
    }

    private boolean hasSeries(PostingRequest request) {
        return request.seriesCode() != null && !request.seriesCode().isBlank();
    }

    private String formatSeriesDisplay(String prefix, Long billNo, String suffix) {
        String head = prefix == null ? "" : prefix;
        String tail = suffix == null ? "" : suffix;
        return head + billNo + tail;
    }

    /**
     * 1. entries is not empty
     * 2. Each entry have one positive side and one side == 0 | Dr > 0 || Cr > 0
     * 3. Total Cr == Total Dr
     * 4. the ledgerId exists
     * 5. VoucherDate is Valid, i.e. it's in the financial year user is working on (will have this in JWT )
     */
    private void validate(PostingRequest request) {
        if (request == null) {
            throw new LekhaiClientException("Voucher posting request must not be null", HttpStatus.BAD_REQUEST);
        }
        if (request.entries() == null || request.entries().isEmpty()) {
            throw new LekhaiClientException("Voucher must contain at least one entry", HttpStatus.BAD_REQUEST);
        }
        requireVoucherDateInFinancialYear(request.voucherDate());
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (PostingEntry entry : request.entries()) {
            requireOneSidedEntry(entry);
            totalDebit = totalDebit.add(entry.debitAmount());
            totalCredit = totalCredit.add(entry.creditAmount());
        }
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new LekhaiClientException(
                    "Voucher total debit must equal total credit",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void requireVoucherDateInFinancialYear(LocalDate voucherDate) {
        if (voucherDate == null) {
            throw new LekhaiClientException("Voucher date must not be null", HttpStatus.BAD_REQUEST);
        }
        Year financialYearStart = JwtUtil.extractJwtClaim().financialYearStart();
        LocalDate aprilFirst = LocalDate.of(financialYearStart.getValue(), 4, 1);
        LocalDate marchThirtyFirst = LocalDate.of(financialYearStart.plusYears(1).getValue(), 3, 31);
        if (voucherDate.isBefore(aprilFirst) || voucherDate.isAfter(marchThirtyFirst)) {
            throw new LekhaiClientException(
                    String.format("Voucher date [%s] is outside the current financial year", voucherDate),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void requireOneSidedEntry(PostingEntry entry) {
        if (entry == null || entry.ledgerId() == null) {
            throw new LekhaiClientException("Each voucher entry must have a ledger", HttpStatus.BAD_REQUEST);
        }
        if (!ledgerRepository.existsById(entry.ledgerId())) {
            throw new LekhaiClientException(
                    String.format("Ledger [%d] not found", entry.ledgerId()),
                    HttpStatus.NOT_FOUND
            );
        }
        if (!isOneSided(entry.debitAmount(), entry.creditAmount())) {
            throw new LekhaiClientException(
                    "Each voucher entry must have exactly one positive side and the other zero",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private boolean isOneSided(BigDecimal debitAmount, BigDecimal creditAmount) {
        if (debitAmount == null || creditAmount == null) {
            return false;
        }
        boolean debitPositive = debitAmount.signum() > 0 && creditAmount.signum() == 0;
        boolean creditPositive = creditAmount.signum() > 0 && debitAmount.signum() == 0;
        return debitPositive || creditPositive;
    }
}
