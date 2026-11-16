package in.lekhai.accountbooks.cashbook.service;

import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.domain.AccountGroup;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
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
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CashBookService {

    private static final String CASH_IN_HAND_GROUP = "Cash in Hand";
    private static final String OPENING_BALANCE_LABEL = "Opening Balance";
    private static final String CLOSING_BALANCE_LABEL = "Closing Balance";

    private final AccountGroupRepository accountGroupRepository;
    private final LedgerRepository ledgerRepository;
    private final VoucherEntryRepository voucherEntryRepository;
    private final VoucherRepository voucherRepository;

    public CashBookService(
            AccountGroupRepository accountGroupRepository,
            LedgerRepository ledgerRepository,
            VoucherEntryRepository voucherEntryRepository,
            VoucherRepository voucherRepository
    ) {
        this.accountGroupRepository = accountGroupRepository;
        this.ledgerRepository = ledgerRepository;
        this.voucherEntryRepository = voucherEntryRepository;
        this.voucherRepository = voucherRepository;
    }

    @ShopContextTransactional
    public CashBookResponse getCashBook(
            LocalDate fromDate,
            LocalDate toDate,
            String format,
            Boolean singleCashSaleEntry,
            Boolean hideTransactionDate,
            String closingInterval,
            Long ledgerId
    ) {
        LocalDate[] range = resolveDateRange(fromDate, toDate);
        LocalDate start = range[0];
        LocalDate end = range[1];

        String reportFormat = (format != null && !format.isBlank()) ? format.toUpperCase() : "DETAIL";
        boolean singleSale = Boolean.TRUE.equals(singleCashSaleEntry);
        boolean hideDate = Boolean.TRUE.equals(hideTransactionDate);
        String interval = "MONTHLY".equalsIgnoreCase(closingInterval) ? "MONTHLY" : "DAILY";

        List<Ledger> cashLedgers = resolveCashLedgers(ledgerId);
        List<Long> cashLedgerIds = cashLedgers.stream().map(Ledger::getId).toList();

        BigDecimal signedOpening = BigDecimal.ZERO;
        for (Ledger l : cashLedgers) {
            BigDecimal bal = l.getOpeningBalance() != null ? l.getOpeningBalance() : BigDecimal.ZERO;
            if (l.getOpeningBalanceType() == AccountEntryType.CR) {
                bal = bal.negate();
            }
            signedOpening = signedOpening.add(bal);
        }

        BigDecimal priorNet = cashLedgerIds.isEmpty()
                ? BigDecimal.ZERO
                : voucherEntryRepository.sumNetBeforeByLedgerIds(cashLedgerIds, start);
        BigDecimal openingAsOf = signedOpening.add(priorNet != null ? priorNet : BigDecimal.ZERO);

        List<LedgerEntryWithBalanceProjection> rawEntries = cashLedgerIds.isEmpty()
                ? List.of()
                : voucherEntryRepository.findEntriesByLedgerIdsBetweenDates(cashLedgerIds, start, end);

        List<Long> voucherIds = rawEntries.stream()
                .map(LedgerEntryWithBalanceProjection::voucherId)
                .distinct()
                .toList();

        Map<Long, Voucher> voucherMap = loadVouchers(voucherIds);
        Map<Long, List<VoucherEntry>> entriesByVoucherId = loadSiblingEntries(voucherIds);
        Map<Long, Ledger> contraLedgerMap = loadContraLedgers(cashLedgerIds, entriesByVoucherId);

        List<CashBookEntryItem> allEntries = new ArrayList<>();

        // 1. Initial Opening Balance Item
        AccountEntryType openingType = crdr(openingAsOf);
        allEntries.add(new CashBookEntryItem()
                .date(hideDate ? null : start.toString())
                .particulars(OPENING_BALANCE_LABEL)
                .vchType(null)
                .vchNo(null)
                .received(openingType == AccountEntryType.DR ? openingAsOf.abs() : null)
                .payment(openingType == AccountEntryType.CR ? openingAsOf.abs() : null)
                .balance(openingAsOf.abs())
                .balanceType(openingType)
                .entryType("OPENING"));

        BigDecimal runningBalance = openingAsOf;
        BigDecimal totalReceived = BigDecimal.ZERO;
        BigDecimal totalPayment = BigDecimal.ZERO;

        // Group entries by date
        Map<LocalDate, List<LedgerEntryWithBalanceProjection>> entriesByDate = rawEntries.stream()
                .collect(Collectors.groupingBy(
                        LedgerEntryWithBalanceProjection::voucherDate,
                        TreeMap::new,
                        Collectors.toList()
                ));

        if ("MONTHLY".equals(interval)) {
            // Group by YearMonth
            Map<YearMonth, List<LocalDate>> datesByMonth = entriesByDate.keySet().stream()
                    .collect(Collectors.groupingBy(YearMonth::from, TreeMap::new, Collectors.toList()));

            for (Map.Entry<YearMonth, List<LocalDate>> monthEntry : datesByMonth.entrySet()) {
                BigDecimal monthReceived = BigDecimal.ZERO;
                BigDecimal monthPayment = BigDecimal.ZERO;

                for (LocalDate date : monthEntry.getValue()) {
                    List<LedgerEntryWithBalanceProjection> dateEntries = entriesByDate.get(date);
                    List<CashBookEntryItem> dayItems = processDateEntries(
                            date, dateEntries, voucherMap, entriesByVoucherId, contraLedgerMap,
                            cashLedgerIds, singleSale, hideDate, reportFormat);

                    for (CashBookEntryItem item : dayItems) {
                        BigDecimal r = item.getReceived() != null ? item.getReceived() : BigDecimal.ZERO;
                        BigDecimal p = item.getPayment() != null ? item.getPayment() : BigDecimal.ZERO;
                        runningBalance = runningBalance.add(r).subtract(p);
                        item.balance(runningBalance.abs()).balanceType(crdr(runningBalance));
                        allEntries.add(item);

                        monthReceived = monthReceived.add(r);
                        monthPayment = monthPayment.add(p);
                        totalReceived = totalReceived.add(r);
                        totalPayment = totalPayment.add(p);
                    }
                }

                // Month Closing Row
                LocalDate monthEnd = monthEntry.getKey().atEndOfMonth();
                allEntries.add(new CashBookEntryItem()
                        .date(hideDate ? null : monthEnd.toString())
                        .particulars("By Closing Balance (Month-End)")
                        .vchType(null)
                        .vchNo(null)
                        .received(monthReceived)
                        .payment(monthPayment)
                        .balance(runningBalance.abs())
                        .balanceType(crdr(runningBalance))
                        .entryType("MONTHLY_CLOSING"));
            }
        } else {
            // DAILY Closing
            for (Map.Entry<LocalDate, List<LedgerEntryWithBalanceProjection>> dayEntry : entriesByDate.entrySet()) {
                LocalDate date = dayEntry.getKey();
                List<LedgerEntryWithBalanceProjection> dateEntries = dayEntry.getValue();

                List<CashBookEntryItem> dayItems = processDateEntries(
                        date, dateEntries, voucherMap, entriesByVoucherId, contraLedgerMap,
                        cashLedgerIds, singleSale, hideDate, reportFormat);

                BigDecimal dayReceived = BigDecimal.ZERO;
                BigDecimal dayPayment = BigDecimal.ZERO;

                for (CashBookEntryItem item : dayItems) {
                    BigDecimal r = item.getReceived() != null ? item.getReceived() : BigDecimal.ZERO;
                    BigDecimal p = item.getPayment() != null ? item.getPayment() : BigDecimal.ZERO;
                    runningBalance = runningBalance.add(r).subtract(p);
                    item.balance(runningBalance.abs()).balanceType(crdr(runningBalance));
                    allEntries.add(item);

                    dayReceived = dayReceived.add(r);
                    dayPayment = dayPayment.add(p);
                    totalReceived = totalReceived.add(r);
                    totalPayment = totalPayment.add(p);
                }

                // Day Closing Row
                allEntries.add(new CashBookEntryItem()
                        .date(hideDate ? null : date.toString())
                        .particulars("By Closing Balance (Daily)")
                        .vchType(null)
                        .vchNo(null)
                        .received(dayReceived)
                        .payment(dayPayment)
                        .balance(runningBalance.abs())
                        .balanceType(crdr(runningBalance))
                        .entryType("DAILY_CLOSING"));
            }
        }

        // Final Closing Balance
        AccountEntryType closingType = crdr(runningBalance);
        allEntries.add(new CashBookEntryItem()
                .date(hideDate ? null : end.toString())
                .particulars(CLOSING_BALANCE_LABEL)
                .vchType(null)
                .vchNo(null)
                .received(totalReceived)
                .payment(totalPayment)
                .balance(runningBalance.abs())
                .balanceType(closingType)
                .entryType("CLOSING"));

        CashBookResponse.FormatEnum formatEnum = "SUMMARY".equalsIgnoreCase(reportFormat)
                ? CashBookResponse.FormatEnum.SUMMARY
                : "TITLE".equalsIgnoreCase(reportFormat)
                ? CashBookResponse.FormatEnum.TITLE
                : CashBookResponse.FormatEnum.DETAIL;

        CashBookResponse.ClosingIntervalEnum intervalEnum = "MONTHLY".equalsIgnoreCase(interval)
                ? CashBookResponse.ClosingIntervalEnum.MONTHLY
                : CashBookResponse.ClosingIntervalEnum.DAILY;

        return new CashBookResponse()
                .fromDate(start)
                .toDate(end)
                .format(formatEnum)
                .singleCashSaleEntry(singleSale)
                .hideTransactionDate(hideDate)
                .closingInterval(intervalEnum)
                .openingBalance(openingAsOf.abs())
                .openingBalanceType(openingType)
                .totalReceived(totalReceived)
                .totalPayment(totalPayment)
                .closingBalance(runningBalance.abs())
                .closingBalanceType(closingType)
                .entries(allEntries);
    }

    private List<CashBookEntryItem> processDateEntries(
            LocalDate date,
            List<LedgerEntryWithBalanceProjection> dateEntries,
            Map<Long, Voucher> voucherMap,
            Map<Long, List<VoucherEntry>> entriesByVoucherId,
            Map<Long, Ledger> contraLedgerMap,
            List<Long> cashLedgerIds,
            boolean singleSale,
            boolean hideDate,
            String format
    ) {
        List<CashBookEntryItem> result = new ArrayList<>();
        List<LedgerEntryWithBalanceProjection> cashSalesEntries = new ArrayList<>();
        List<LedgerEntryWithBalanceProjection> otherEntries = new ArrayList<>();

        for (LedgerEntryWithBalanceProjection entry : dateEntries) {
            Voucher voucher = voucherMap.get(entry.voucherId());
            boolean isSalesVoucher = voucher != null && voucher.getVoucherType() == VoucherType.SALES;
            boolean isCashInflow = entry.debitAmount() != null && entry.debitAmount().compareTo(BigDecimal.ZERO) > 0;

            if (singleSale && isSalesVoucher && isCashInflow) {
                cashSalesEntries.add(entry);
            } else {
                otherEntries.add(entry);
            }
        }

        // Process single aggregated cash sale if any
        if (!cashSalesEntries.isEmpty()) {
            BigDecimal totalSaleReceived = cashSalesEntries.stream()
                    .map(LedgerEntryWithBalanceProjection::debitAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(new CashBookEntryItem()
                    .date(hideDate ? null : date.toString())
                    .particulars("TITLE".equalsIgnoreCase(format) ? "To Cash Sales A/c" : "Cash Sales")
                    .vchType(VoucherType.SALES.name())
                    .vchNo(cashSalesEntries.size() > 1 ? cashSalesEntries.size() + " entries" : getVoucherNo(voucherMap, cashSalesEntries.get(0)))
                    .received(totalSaleReceived)
                    .payment(null)
                    .entryType("TRANSACTION")
                    .narration(cashSalesEntries.size() > 1 ? "Consolidated date-wise cash sales" : null));
        }

        // Process other entries
        for (LedgerEntryWithBalanceProjection entry : otherEntries) {
            Voucher voucher = voucherMap.get(entry.voucherId());
            String vchType = voucher != null && voucher.getVoucherType() != null ? voucher.getVoucherType().name() : "";
            String vchNo = getVoucherNo(voucherMap, entry);

            List<AccountLedgerContraItem> contraList = buildContraEntries(entry, entriesByVoucherId, contraLedgerMap, cashLedgerIds);
            String contraName = joinContraNames(contraList);
            if (contraName.isBlank()) {
                contraName = voucher != null && voucher.getNarration() != null && !voucher.getNarration().isBlank()
                        ? voucher.getNarration()
                        : "Sundry Entries";
            }

            boolean isDebit = entry.debitAmount() != null && entry.debitAmount().compareTo(BigDecimal.ZERO) > 0;
            String particulars = contraName;
            if ("TITLE".equalsIgnoreCase(format)) {
                particulars = isDebit ? "To " + contraName : "By " + contraName;
            }

            CashBookEntryItem item = new CashBookEntryItem()
                    .date(hideDate ? null : date.toString())
                    .particulars(particulars)
                    .vchType(vchType)
                    .vchNo(vchNo)
                    .received(isZero(entry.debitAmount()) ? null : entry.debitAmount())
                    .payment(isZero(entry.creditAmount()) ? null : entry.creditAmount())
                    .entryType("TRANSACTION")
                    .narration(voucher != null ? voucher.getNarration() : null);

            if ("DETAIL".equalsIgnoreCase(format) && !contraList.isEmpty()) {
                item.contraEntries(contraList);
            }

            result.add(item);
        }

        return result;
    }

    private String getVoucherNo(Map<Long, Voucher> voucherMap, LedgerEntryWithBalanceProjection entry) {
        Voucher v = voucherMap.get(entry.voucherId());
        return (v != null && v.getVoucherNumber() != null) ? v.getVoucherNumber().toString() : "";
    }

    private List<Ledger> resolveCashLedgers(Long ledgerId) {
        if (ledgerId != null) {
            return ledgerRepository.findById(ledgerId)
                    .map(List::of)
                    .orElse(List.of());
        }

        List<AccountGroup> allGroups = accountGroupRepository.findAll();
        Optional<AccountGroup> cashInHandGroup = allGroups.stream()
                .filter(g -> CASH_IN_HAND_GROUP.equalsIgnoreCase(g.getName()))
                .findFirst();

        Set<Long> cashGroupIds = new HashSet<>();
        if (cashInHandGroup.isPresent()) {
            cashGroupIds.add(cashInHandGroup.get().getId());
            collectSubGroupIds(cashInHandGroup.get().getId(), allGroups, cashGroupIds);
        }

        List<Ledger> ledgers = cashGroupIds.isEmpty()
                ? List.of()
                : ledgerRepository.findByAccountGroupIdIn(cashGroupIds);

        if (ledgers.isEmpty()) {
            ledgers = ledgerRepository.findAll().stream()
                    .filter(l -> l.getName() != null && l.getName().toLowerCase().contains("cash"))
                    .toList();
        }

        return ledgers;
    }

    private void collectSubGroupIds(Long parentId, List<AccountGroup> allGroups, Set<Long> result) {
        for (AccountGroup g : allGroups) {
            if (parentId.equals(g.getParentId()) && !result.contains(g.getId())) {
                result.add(g.getId());
                collectSubGroupIds(g.getId(), allGroups, result);
            }
        }
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

    private Map<Long, Ledger> loadContraLedgers(List<Long> cashLedgerIds, Map<Long, List<VoucherEntry>> entriesByVoucherId) {
        Set<Long> ledgerIds = new HashSet<>(cashLedgerIds);
        entriesByVoucherId.values().stream()
                .flatMap(List::stream)
                .map(VoucherEntry::getLedgerId)
                .forEach(ledgerIds::add);
        return ledgerRepository.findAllById(ledgerIds).stream()
                .collect(Collectors.toMap(Ledger::getId, Function.identity()));
    }

    private List<AccountLedgerContraItem> buildContraEntries(
            LedgerEntryWithBalanceProjection entry,
            Map<Long, List<VoucherEntry>> entriesByVoucherId,
            Map<Long, Ledger> ledgerMap,
            List<Long> cashLedgerIds
    ) {
        List<VoucherEntry> voucherEntries = entriesByVoucherId.get(entry.voucherId());
        if (voucherEntries == null) return List.of();

        boolean onDebitSide = !isZero(entry.debitAmount());
        boolean onCreditSide = !isZero(entry.creditAmount());

        List<VoucherEntry> contra = voucherEntries.stream()
                .filter(ve -> !cashLedgerIds.contains(ve.getLedgerId()))
                .filter(ve -> onDebitSide
                        ? !isZero(ve.getCreditAmount())
                        : onCreditSide ? !isZero(ve.getDebitAmount()) : true)
                .sorted(Comparator.comparing(VoucherEntry::getLineNumber,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        if (contra.isEmpty()) {
            contra = voucherEntries.stream()
                    .filter(ve -> !cashLedgerIds.contains(ve.getLedgerId()))
                    .sorted(Comparator.comparing(VoucherEntry::getLineNumber,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
        }

        return contra.stream()
                .map(ve -> new AccountLedgerContraItem()
                        .ledgerId(ve.getLedgerId())
                        .ledgerName(ledgerName(ledgerMap, ve.getLedgerId()))
                        .debitAmt(isZero(ve.getDebitAmount()) ? null : ve.getDebitAmount())
                        .creditAmt(isZero(ve.getCreditAmount()) ? null : ve.getCreditAmount())
                        .remarks(ve.getRemarks()))
                .toList();
    }

    private static String joinContraNames(List<AccountLedgerContraItem> contraEntries) {
        return contraEntries.stream()
                .map(AccountLedgerContraItem::getLedgerName)
                .filter(name -> name != null && !name.isEmpty())
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private static String ledgerName(Map<Long, Ledger> ledgerMap, Long ledgerId) {
        Ledger ledger = ledgerMap.get(ledgerId);
        return ledger != null ? ledger.getName() : "";
    }

    private LocalDate[] resolveDateRange(LocalDate fromDate, LocalDate toDate) {
        LocalDate start = fromDate;
        LocalDate end = toDate;
        if (start == null || end == null) {
            Year financialYearStart = JwtUtil.extractJwtClaim().financialYearStart();
            int fyYear = financialYearStart != null ? financialYearStart.getValue() : 2026;
            start = start != null ? start : LocalDate.of(fyYear, 4, 1);
            end = end != null ? end : LocalDate.of(fyYear + 1, 3, 31);
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }
        return new LocalDate[]{start, end};
    }

    private static AccountEntryType crdr(BigDecimal balance) {
        return balance.signum() >= 0 ? AccountEntryType.DR : AccountEntryType.CR;
    }

    private static boolean isZero(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }
}
