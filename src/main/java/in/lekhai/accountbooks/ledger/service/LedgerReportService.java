package in.lekhai.accountbooks.ledger.service;

import in.lekhai.contract.model.AccountEntryType;
import in.lekhai.contract.model.AccountLedgerContraItem;
import in.lekhai.contract.model.AccountLedgerEntryItem;
import in.lekhai.contract.model.AccountLedgerPageResponse;
import in.lekhai.contract.model.PaginationMeta;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import in.lekhai.voucher.entity.Voucher;
import in.lekhai.voucher.entity.VoucherEntry;
import in.lekhai.voucher.repository.LedgerEntryWithBalanceProjection;
import in.lekhai.voucher.repository.VoucherEntryRepository;
import in.lekhai.voucher.repository.VoucherRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LedgerReportService {
    private static final String OPENING_BALANCE_LABEL = "Opening Balance";

    private final LedgerRepository ledgerRepository;
    private final VoucherEntryRepository voucherEntryRepository;
    private final VoucherRepository voucherRepository;

    public LedgerReportService(
            LedgerRepository ledgerRepository,
            VoucherEntryRepository voucherEntryRepository,
            VoucherRepository voucherRepository
    ) {
        this.ledgerRepository = ledgerRepository;
        this.voucherEntryRepository = voucherEntryRepository;
        this.voucherRepository = voucherRepository;
    }

    @ShopContextTransactional
    public AccountLedgerPageResponse getAccountLedgerEntries(
            Long ledgerId,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        LocalDate[] range = resolveDateRange(fromDate, toDate);
        LocalDate start = range[0];
        LocalDate end = range[1];

        Ledger ledger = loadLedgerOrThrow(ledgerId);
        BigDecimal signedOpening = signedOpeningBalance(ledger);
        BigDecimal openingAsOf = signedOpening.add(voucherEntryRepository.sumNetBefore(ledgerId, start));

        List<LedgerEntryWithBalanceProjection> pageEntries = voucherEntryRepository.findPageWithRunningBalance(
                ledgerId, start, end, pageable.getPageSize(), (int) pageable.getOffset());
        long totalCount = voucherEntryRepository.countByLedgerIdAndDateBetween(ledgerId, start, end);

        List<Long> voucherIds = pageEntries.stream()
                .map(LedgerEntryWithBalanceProjection::voucherId)
                .distinct()
                .toList();

        Map<Long, Voucher> voucherMap = loadVouchers(voucherIds);
        Map<Long, List<VoucherEntry>> entriesByVoucherId = loadSiblingEntries(voucherIds);
        Map<Long, Ledger> ledgerMap = loadContraLedgers(ledgerId, entriesByVoucherId);

        List<AccountLedgerEntryItem> items = new ArrayList<>();
        if (pageable.getPageNumber() == 0 && !isZero(openingAsOf)) {
            items.add(buildOpeningItem(openingAsOf));
        }
        for (LedgerEntryWithBalanceProjection entry : pageEntries) {
            items.add(buildEntryItem(entry, voucherMap, entriesByVoucherId, ledgerMap, signedOpening));
        }

        return new AccountLedgerPageResponse()
                .data(items)
                .pagination(buildPagination(pageable, totalCount));
    }

    private Ledger loadLedgerOrThrow(Long ledgerId) {
        return ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new RuntimeException("Ledger not found: " + ledgerId));
    }

    private BigDecimal signedOpeningBalance(Ledger ledger) {
        BigDecimal openingBalance = ledger.getOpeningBalance() != null ? ledger.getOpeningBalance() : BigDecimal.ZERO;
        return ledger.getOpeningBalanceType() == AccountEntryType.CR
                ? openingBalance.negate()
                : openingBalance;
    }

    private Map<Long, Voucher> loadVouchers(List<Long> voucherIds) {
        if (voucherIds.isEmpty()) {
            return Map.of();
        }
        return voucherRepository.findAllById(voucherIds)
                .stream()
                .collect(Collectors.toMap(Voucher::getId, Function.identity()));
    }

    private Map<Long, List<VoucherEntry>> loadSiblingEntries(List<Long> voucherIds) {
        if (voucherIds.isEmpty()) {
            return Map.of();
        }
        return voucherEntryRepository.findByVoucherIdIn(voucherIds)
                .stream()
                .collect(Collectors.groupingBy(VoucherEntry::getVoucherId));
    }

    private Map<Long, Ledger> loadContraLedgers(Long ledgerId, Map<Long, List<VoucherEntry>> entriesByVoucherId) {
        Set<Long> ledgerIds = new HashSet<>();
        ledgerIds.add(ledgerId);
        entriesByVoucherId.values().stream()
                .flatMap(List::stream)
                .map(VoucherEntry::getLedgerId)
                .forEach(ledgerIds::add);
        return ledgerRepository.findAllById(ledgerIds)
                .stream()
                .collect(Collectors.toMap(Ledger::getId, Function.identity()));
    }

    private AccountLedgerEntryItem buildOpeningItem(BigDecimal openingAsOf) {
        AccountEntryType crdr = crdr(openingAsOf);
        return new AccountLedgerEntryItem()
                .date(null)
                .ledgerName(OPENING_BALANCE_LABEL)
                .vtype(null)
                .debitAmt(crdr == AccountEntryType.DR ? openingAsOf.abs() : null)
                .creditAmt(crdr == AccountEntryType.CR ? openingAsOf.abs() : null)
                .balance(openingAsOf.abs())
                .crdr(crdr);
    }

    private AccountLedgerEntryItem buildEntryItem(
            LedgerEntryWithBalanceProjection entry,
            Map<Long, Voucher> voucherMap,
            Map<Long, List<VoucherEntry>> entriesByVoucherId,
            Map<Long, Ledger> ledgerMap,
            BigDecimal signedOpening
    ) {
        Voucher voucher = voucherMap.get(entry.voucherId());
        BigDecimal balance = signedOpening.add(entry.runningNet());

        List<AccountLedgerContraItem> contraEntries =
                buildContraEntries(entry, entriesByVoucherId, ledgerMap);

        return new AccountLedgerEntryItem()
                .date(voucher.getVoucherDate().toString())
                .ledgerName(joinContraNames(contraEntries))
                .vtype(voucher.getVoucherType().name())
                .debitAmt(isZero(entry.debitAmount()) ? null : entry.debitAmount())
                .creditAmt(isZero(entry.creditAmount()) ? null : entry.creditAmount())
                .balance(balance.abs())
                .crdr(crdr(balance))
                .contraEntries(contraEntries);
    }

    private List<AccountLedgerContraItem> buildContraEntries(
            LedgerEntryWithBalanceProjection entry,
            Map<Long, List<VoucherEntry>> entriesByVoucherId,
            Map<Long, Ledger> ledgerMap
    ) {
        List<VoucherEntry> voucherEntries = entriesByVoucherId.get(entry.voucherId());
        if (voucherEntries == null) {
            return List.of();
        }

        boolean onDebitSide = !isZero(entry.debitAmount());
        boolean onCreditSide = !isZero(entry.creditAmount());

        List<VoucherEntry> contra = voucherEntries.stream()
                .filter(ve -> !ve.getLedgerId().equals(entry.ledgerId()))
                .filter(ve -> onDebitSide
                        ? !isZero(ve.getCreditAmount())
                        : onCreditSide ? !isZero(ve.getDebitAmount()) : true)
                .sorted(Comparator.comparing(VoucherEntry::getLineNumber,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        if (contra.isEmpty()) {
            contra = voucherEntries.stream()
                    .filter(ve -> !ve.getLedgerId().equals(entry.ledgerId()))
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

    private PaginationMeta buildPagination(Pageable pageable, long totalCount) {
        int totalPages = (int) Math.ceil((double) totalCount / pageable.getPageSize());
        return new PaginationMeta()
                .totalPages(totalPages)
                .totalElements(totalCount)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize());
    }

    private LocalDate[] resolveDateRange(LocalDate fromDate, LocalDate toDate) {
        LocalDate start = fromDate;
        LocalDate end = toDate;
        if (start == null || end == null) {
            Year financialYearStart = JwtUtil.extractJwtClaim().financialYearStart();
            int fyYear = financialYearStart.getValue();
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
