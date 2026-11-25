package in.lekhai.accountbooks.interestcalculation.service;

import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import in.lekhai.voucher.entity.Voucher;
import in.lekhai.voucher.entity.VoucherEntry;
import in.lekhai.voucher.repository.LedgerEntryWithBalanceProjection;
import in.lekhai.voucher.repository.VoucherEntryRepository;
import in.lekhai.voucher.repository.VoucherRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InterestCalculationService {

    private final LedgerRepository ledgerRepository;
    private final VoucherEntryRepository voucherEntryRepository;
    private final VoucherRepository voucherRepository;

    public InterestCalculationService(
            LedgerRepository ledgerRepository,
            VoucherEntryRepository voucherEntryRepository,
            VoucherRepository voucherRepository
    ) {
        this.ledgerRepository = ledgerRepository;
        this.voucherEntryRepository = voucherEntryRepository;
        this.voucherRepository = voucherRepository;
    }

    @ShopContextTransactional
    public ProductBasisInterestResponse getProductBasisInterest(
            Long ledgerId,
            LocalDate fromDate,
            LocalDate toDate,
            String calculationType,
            BigDecimal interestRate,
            BigDecimal includeOpeningInterest
    ) {
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new IllegalArgumentException("Ledger not found: " + ledgerId));

        LocalDate[] range = resolveDateRange(fromDate, toDate);
        LocalDate start = range[0];
        LocalDate end = range[1];

        String calcType = "MONTHLY".equalsIgnoreCase(calculationType) ? "MONTHLY" : "YEARLY";
        BigDecimal rate = interestRate != null ? interestRate : BigDecimal.valueOf(12.0);
        BigDecimal openingIntt = includeOpeningInterest != null ? includeOpeningInterest : BigDecimal.ZERO;

        BigDecimal signedOpening = ledger.getOpeningBalance() != null ? ledger.getOpeningBalance() : BigDecimal.ZERO;
        if (ledger.getOpeningBalanceType() == AccountEntryType.CR) {
            signedOpening = signedOpening.negate();
        }

        BigDecimal priorNet = voucherEntryRepository.sumNetBefore(ledgerId, start);
        BigDecimal openingAsOf = signedOpening.add(priorNet != null ? priorNet : BigDecimal.ZERO);

        List<LedgerEntryWithBalanceProjection> rawEntries =
                voucherEntryRepository.findEntriesByLedgerIdsBetweenDates(List.of(ledgerId), start, end);

        // Group balance changes by date
        Map<LocalDate, BigDecimal> netChangeByDate = new TreeMap<>();
        for (LedgerEntryWithBalanceProjection entry : rawEntries) {
            BigDecimal net = (entry.debitAmount() != null ? entry.debitAmount() : BigDecimal.ZERO)
                    .subtract(entry.creditAmount() != null ? entry.creditAmount() : BigDecimal.ZERO);
            netChangeByDate.merge(entry.voucherDate(), net, BigDecimal::add);
        }

        // Build chronological list of (date, balance)
        List<Map.Entry<LocalDate, BigDecimal>> balanceTimeline = new ArrayList<>();
        BigDecimal currentBal = openingAsOf;

        if (!netChangeByDate.containsKey(start)) {
            balanceTimeline.add(Map.entry(start, currentBal));
        }

        for (Map.Entry<LocalDate, BigDecimal> entry : netChangeByDate.entrySet()) {
            currentBal = currentBal.add(entry.getValue());
            balanceTimeline.add(Map.entry(entry.getKey(), currentBal));
        }

        List<ProductBasisInterestItem> items = new ArrayList<>();
        BigDecimal runningInterest = openingIntt;
        BigDecimal totalReceivable = BigDecimal.ZERO;
        BigDecimal totalPayable = BigDecimal.ZERO;

        for (int i = 0; i < balanceTimeline.size(); i++) {
            Map.Entry<LocalDate, BigDecimal> current = balanceTimeline.get(i);
            LocalDate entryDate = current.getKey();
            BigDecimal bal = current.getValue();

            LocalDate nextDate = (i + 1 < balanceTimeline.size()) ? balanceTimeline.get(i + 1).getKey() : end;
            long days = Math.max(0, ChronoUnit.DAYS.between(entryDate, nextDate));
            if (i == balanceTimeline.size() - 1 && days == 0) {
                days = 1;
            }

            BigDecimal inttReceivable = null;
            BigDecimal inttPayable = null;

            if (bal.signum() != 0 && days > 0) {
                BigDecimal divisor = "MONTHLY".equals(calcType) ? BigDecimal.valueOf(3000) : BigDecimal.valueOf(36500);
                BigDecimal interest = bal.abs()
                        .multiply(rate)
                        .multiply(BigDecimal.valueOf(days))
                        .divide(divisor, 2, RoundingMode.HALF_UP);

                if (bal.signum() > 0) {
                    inttReceivable = interest;
                    totalReceivable = totalReceivable.add(interest);
                    runningInterest = runningInterest.add(interest);
                } else {
                    inttPayable = interest;
                    totalPayable = totalPayable.add(interest);
                    runningInterest = runningInterest.subtract(interest);
                }
            }

            AccountEntryType balType = bal.signum() >= 0 ? AccountEntryType.DR : AccountEntryType.CR;
            AccountEntryType inttBalType = runningInterest.signum() >= 0 ? AccountEntryType.DR : AccountEntryType.CR;

            items.add(new ProductBasisInterestItem()
                    .date(entryDate)
                    .balance(bal.abs())
                    .balanceType(balType)
                    .days((int) days)
                    .inttReceivable(inttReceivable)
                    .inttPayable(inttPayable)
                    .balanceInterest(runningInterest.abs())
                    .balanceInterestType(inttBalType));
        }

        return new ProductBasisInterestResponse()
                .ledgerId(ledgerId)
                .ledgerName(ledger.getName())
                .calculationType(ProductBasisInterestResponse.CalculationTypeEnum.fromValue(calcType))
                .fromDate(start)
                .toDate(end)
                .interestRate(rate)
                .includeOpeningInterest(openingIntt)
                .totalInttReceivable(totalReceivable)
                .totalInttPayable(totalPayable)
                .netInterestBalance(runningInterest.abs())
                .netInterestBalanceType(runningInterest.signum() >= 0 ? AccountEntryType.DR : AccountEntryType.CR)
                .items(items);
    }

    @ShopContextTransactional
    public DateWiseInterestResponse getDateWiseInterest(
            Long ledgerId,
            LocalDate firstDrDate,
            LocalDate lastDrDate,
            LocalDate firstCrDate,
            LocalDate lastCrDate,
            Integer graceDays,
            BigDecimal yearlyRate,
            LocalDate asOnDate,
            Boolean includeOpening
    ) {
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new IllegalArgumentException("Ledger not found: " + ledgerId));

        Year financialYearStart = null;
        try {
            financialYearStart = JwtUtil.extractJwtClaim().financialYearStart();
        } catch (Exception ignored) {
        }
        int fy = financialYearStart != null ? financialYearStart.getValue() : 2026;

        LocalDate defaultStart = LocalDate.of(fy, 4, 1);
        LocalDate defaultEnd = LocalDate.of(fy + 1, 3, 31);

        LocalDate startDr = firstDrDate != null ? firstDrDate : defaultStart;
        LocalDate endDr = lastDrDate != null ? lastDrDate : defaultEnd;
        LocalDate startCr = firstCrDate != null ? firstCrDate : defaultStart;
        LocalDate endCr = lastCrDate != null ? lastCrDate : defaultEnd;

        LocalDate minStart = startDr.isBefore(startCr) ? startDr : startCr;
        LocalDate maxEnd = endDr.isAfter(endCr) ? endDr : endCr;

        int grace = graceDays != null ? graceDays : 0;
        BigDecimal rate = yearlyRate != null ? yearlyRate : BigDecimal.valueOf(24.0);
        LocalDate effectiveAsOn = asOnDate != null ? asOnDate : defaultEnd;
        boolean incOpening = Boolean.TRUE.equals(includeOpening);

        BigDecimal signedOpening = ledger.getOpeningBalance() != null ? ledger.getOpeningBalance() : BigDecimal.ZERO;
        if (ledger.getOpeningBalanceType() == AccountEntryType.CR) {
            signedOpening = signedOpening.negate();
        }

        BigDecimal priorNet = voucherEntryRepository.sumNetBefore(ledgerId, minStart);
        BigDecimal openingAsOf = signedOpening.add(priorNet != null ? priorNet : BigDecimal.ZERO);

        List<LedgerEntryWithBalanceProjection> rawEntries =
                voucherEntryRepository.findEntriesByLedgerIdsBetweenDates(List.of(ledgerId), minStart, maxEnd);

        List<Long> voucherIds = rawEntries.stream().map(LedgerEntryWithBalanceProjection::voucherId).distinct().toList();
        Map<Long, Voucher> voucherMap = loadVouchers(voucherIds);
        Map<Long, List<VoucherEntry>> entriesByVoucherId = loadSiblingEntries(voucherIds);
        Map<Long, Ledger> contraLedgerMap = loadContraLedgers(ledgerId, entriesByVoucherId);

        List<DateWiseInterestItem> items = new ArrayList<>();
        BigDecimal runningBal = openingAsOf;
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        BigDecimal totalInttDr = BigDecimal.ZERO;
        BigDecimal totalInttCr = BigDecimal.ZERO;
        long weightedDaysProduct = 0;
        BigDecimal totalAmountForAverage = BigDecimal.ZERO;

        if (incOpening && openingAsOf.signum() != 0) {
            BigDecimal deb = openingAsOf.signum() > 0 ? openingAsOf : null;
            BigDecimal cred = openingAsOf.signum() < 0 ? openingAsOf.abs() : null;
            if (deb != null) totalDebit = totalDebit.add(deb);
            if (cred != null) totalCredit = totalCredit.add(cred);

            items.add(new DateWiseInterestItem()
                    .date(minStart)
                    .particulars("To Opening Balance")
                    .debit(deb)
                    .credit(cred)
                    .balanceAmount(openingAsOf.abs())
                    .balanceType(openingAsOf.signum() >= 0 ? AccountEntryType.DR : AccountEntryType.CR)
                    .daysDr(null)
                    .inttDr(null)
                    .daysCr(null)
                    .inttCr(null));
        }

        for (LedgerEntryWithBalanceProjection entry : rawEntries) {
            BigDecimal debit = entry.debitAmount() != null ? entry.debitAmount() : BigDecimal.ZERO;
            BigDecimal credit = entry.creditAmount() != null ? entry.creditAmount() : BigDecimal.ZERO;

            boolean isDebit = debit.signum() > 0;
            boolean isCredit = credit.signum() > 0;

            if (isDebit && (entry.voucherDate().isBefore(startDr) || entry.voucherDate().isAfter(endDr))) {
                continue;
            }
            if (isCredit && (entry.voucherDate().isBefore(startCr) || entry.voucherDate().isAfter(endCr))) {
                continue;
            }

            totalDebit = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);
            runningBal = runningBal.add(debit).subtract(credit);

            Voucher v = voucherMap.get(entry.voucherId());
            String contraName = resolveContraLedgerName(entry, entriesByVoucherId, contraLedgerMap, ledgerId);
            String narration = v != null && v.getNarration() != null ? v.getNarration().trim() : "";
            String particulars = contraName;
            if (!narration.isEmpty()) {
                particulars = particulars.isEmpty() ? narration : particulars + "  " + narration;
            }

            LocalDate dueDate = entry.voucherDate().plusDays(grace);
            long days = Math.max(0, ChronoUnit.DAYS.between(dueDate, effectiveAsOn));

            Integer daysDr = null;
            BigDecimal inttDr = null;
            Integer daysCr = null;
            BigDecimal inttCr = null;

            if (isDebit) {
                daysDr = (int) days;
                inttDr = debit.multiply(rate)
                        .multiply(BigDecimal.valueOf(days))
                        .divide(BigDecimal.valueOf(36500), 2, RoundingMode.HALF_UP);
                totalInttDr = totalInttDr.add(inttDr);
                weightedDaysProduct += debit.longValue() * days;
                totalAmountForAverage = totalAmountForAverage.add(debit);
            } else if (isCredit) {
                daysCr = (int) days;
                inttCr = credit.multiply(rate)
                        .multiply(BigDecimal.valueOf(days))
                        .divide(BigDecimal.valueOf(36500), 2, RoundingMode.HALF_UP);
                totalInttCr = totalInttCr.add(inttCr);
                weightedDaysProduct += credit.longValue() * days;
                totalAmountForAverage = totalAmountForAverage.add(credit);
            }

            items.add(new DateWiseInterestItem()
                    .date(entry.voucherDate())
                    .particulars(particulars)
                    .debit(debit.signum() > 0 ? debit : null)
                    .credit(credit.signum() > 0 ? credit : null)
                    .balanceAmount(runningBal.abs())
                    .balanceType(runningBal.signum() >= 0 ? AccountEntryType.DR : AccountEntryType.CR)
                    .daysDr(daysDr)
                    .inttDr(inttDr)
                    .daysCr(daysCr)
                    .inttCr(inttCr));
        }

        int avgDays = (totalAmountForAverage.signum() > 0)
                ? (int) (weightedDaysProduct / totalAmountForAverage.longValue())
                : 0;

        BigDecimal netInterest = totalInttDr.subtract(totalInttCr);

        return new DateWiseInterestResponse()
                .ledgerId(ledgerId)
                .ledgerName(ledger.getName())
                .firstDrDate(startDr)
                .lastDrDate(endDr)
                .firstCrDate(startCr)
                .lastCrDate(endCr)
                .graceDays(grace)
                .yearlyRate(rate)
                .asOnDate(effectiveAsOn)
                .includeOpening(incOpening)
                .totalDebit(totalDebit)
                .totalCredit(totalCredit)
                .closingBalance(runningBal.abs())
                .closingBalanceType(runningBal.signum() >= 0 ? AccountEntryType.DR : AccountEntryType.CR)
                .totalInttDr(totalInttDr)
                .totalInttCr(totalInttCr)
                .netInterestBalance(netInterest.abs())
                .netInterestBalanceType(netInterest.signum() >= 0 ? AccountEntryType.DR : AccountEntryType.CR)
                .averageDays(avgDays)
                .items(items);
    }

    @ShopContextTransactional
    public ManualInterestResponse calculateManualInterest(ManualInterestCalculateRequest request) {
        Long ledgerId = request.getLedgerId();
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new IllegalArgumentException("Ledger not found: " + ledgerId));

        int grace = request.getGraceDays() != null ? request.getGraceDays() : 0;
        BigDecimal rate = request.getYearlyRate() != null ? request.getYearlyRate() : BigDecimal.valueOf(24.0);
        LocalDate asOn = request.getAsOnDate() != null ? request.getAsOnDate() : LocalDate.now();

        List<ManualInterestItem> items = new ArrayList<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        BigDecimal totalDebitIntt = BigDecimal.ZERO;
        BigDecimal totalCreditIntt = BigDecimal.ZERO;

        List<ManualInterestEntryRequest> entries = request.getEntries();
        if (entries != null) {
            for (ManualInterestEntryRequest entry : entries) {
                if (entry.getAmount() == null || entry.getDate() == null) {
                    continue;
                }

                boolean isDr = entry.getDrCr() == AccountEntryType.DR;
                BigDecimal amt = entry.getAmount().abs();

                if (isDr) {
                    totalDebit = totalDebit.add(amt);
                } else {
                    totalCredit = totalCredit.add(amt);
                }

                LocalDate dueDate = entry.getDate().plusDays(grace);
                long days = Math.max(0, ChronoUnit.DAYS.between(dueDate, asOn));

                BigDecimal interest = amt.multiply(rate)
                        .multiply(BigDecimal.valueOf(days))
                        .divide(BigDecimal.valueOf(36500), 2, RoundingMode.HALF_UP);

                if (isDr) {
                    totalDebitIntt = totalDebitIntt.add(interest);
                } else {
                    totalCreditIntt = totalCreditIntt.add(interest);
                }

                items.add(new ManualInterestItem()
                        .drCr(entry.getDrCr())
                        .date(entry.getDate())
                        .particulars(entry.getParticulars() != null ? entry.getParticulars() : "")
                        .amount(amt)
                        .days((int) days)
                        .interest(interest));
            }
        }

        BigDecimal netIntt = totalDebitIntt.subtract(totalCreditIntt);

        return new ManualInterestResponse()
                .ledgerId(ledgerId)
                .ledgerName(ledger.getName())
                .graceDays(grace)
                .yearlyRate(rate)
                .asOnDate(asOn)
                .totalDebit(totalDebit)
                .totalCredit(totalCredit)
                .totalDebitInterest(totalDebitIntt)
                .totalCreditInterest(totalCreditIntt)
                .netInterest(netIntt.abs())
                .netInterestType(netIntt.signum() >= 0 ? AccountEntryType.DR : AccountEntryType.CR)
                .items(items);
    }

    private Map<Long, Voucher> loadVouchers(List<Long> voucherIds) {
        if (voucherIds.isEmpty()) return Map.of();
        return voucherRepository.findAllById(voucherIds).stream()
                .collect(Collectors.toMap(Voucher::getId, Function.identity()));
    }

    private Map<Long, List<VoucherEntry>> loadSiblingEntries(List<Long> voucherIds) {
        if (voucherIds.isEmpty()) return Map.of();
        return voucherEntryRepository.findByVoucherIdIn(voucherIds).stream()
                .collect(Collectors.groupingBy(VoucherEntry::getVoucherId));
    }

    private Map<Long, Ledger> loadContraLedgers(Long ledgerId, Map<Long, List<VoucherEntry>> entriesByVoucherId) {
        Set<Long> ledgerIds = new HashSet<>();
        ledgerIds.add(ledgerId);
        entriesByVoucherId.values().stream()
                .flatMap(List::stream)
                .map(VoucherEntry::getLedgerId)
                .forEach(ledgerIds::add);
        return ledgerRepository.findAllById(ledgerIds).stream()
                .collect(Collectors.toMap(Ledger::getId, Function.identity()));
    }

    private String resolveContraLedgerName(
            LedgerEntryWithBalanceProjection entry,
            Map<Long, List<VoucherEntry>> entriesByVoucherId,
            Map<Long, Ledger> contraLedgerMap,
            Long currentLedgerId
    ) {
        List<VoucherEntry> siblings = entriesByVoucherId.getOrDefault(entry.voucherId(), List.of());
        List<VoucherEntry> contras = siblings.stream()
                .filter(ve -> !currentLedgerId.equals(ve.getLedgerId()))
                .toList();

        if (contras.isEmpty()) return "";

        return contras.stream()
                .map(ve -> {
                    Ledger l = contraLedgerMap.get(ve.getLedgerId());
                    return l != null ? l.getName() : "Ledger #" + ve.getLedgerId();
                })
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private LocalDate[] resolveDateRange(LocalDate fromDate, LocalDate toDate) {
        LocalDate start = fromDate;
        LocalDate end = toDate;
        if (start == null || end == null) {
            Year financialYearStart = null;
            try {
                financialYearStart = JwtUtil.extractJwtClaim().financialYearStart();
            } catch (Exception ignored) {
            }
            int fyYear = financialYearStart != null ? financialYearStart.getValue() : 2026;
            start = start != null ? start : LocalDate.of(fyYear, 4, 1);
            end = end != null ? end : LocalDate.of(fyYear + 1, 3, 31);
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }
        return new LocalDate[]{start, end};
    }
}
