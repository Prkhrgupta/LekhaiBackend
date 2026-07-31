package in.lekhai.accountbooks.ledger.service;

import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import in.lekhai.voucher.entity.Voucher;
import in.lekhai.voucher.entity.VoucherEntry;
import in.lekhai.voucher.repository.VoucherEntryRepository;
import in.lekhai.voucher.repository.VoucherRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LedgerReportService {
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
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new RuntimeException("Ledger not found: " + ledgerId));

        BigDecimal openingBalance = ledger.getOpeningBalance() != null ? ledger.getOpeningBalance() : BigDecimal.ZERO;
        AccountEntryType openingBalanceType = ledger.getOpeningBalanceType() != null
                ? ledger.getOpeningBalanceType() : AccountEntryType.DR;

        BigDecimal signedOpeningBalance = openingBalanceType == AccountEntryType.CR
                ? openingBalance.negate()
                : openingBalance;

        List<VoucherEntry> entries = voucherEntryRepository.findByLedgerIdOrderByVoucherDate(ledgerId, pageable);
        long totalCount = voucherEntryRepository.countByLedgerId(ledgerId);

        if (entries.isEmpty() && isZero(openingBalance)) {
            var pagination = new PaginationMeta()
                    .totalPages(0)
                    .totalElements(0L)
                    .page(pageable.getPageNumber())
                    .size(pageable.getPageSize());
            return new AccountLedgerPageResponse()
                    .data(List.of())
                    .pagination(pagination);
        }

        List<Long> voucherIds = entries.stream()
                .map(VoucherEntry::getVoucherId)
                .distinct()
                .toList();

        Map<Long, Voucher> voucherMap = voucherRepository.findAllById(voucherIds)
                .stream()
                .collect(Collectors.toMap(Voucher::getId, Function.identity()));

        List<VoucherEntry> allEntriesForVouchers = voucherEntryRepository.findByVoucherIdIn(voucherIds);

        Map<Long, List<VoucherEntry>> entriesByVoucherId = allEntriesForVouchers.stream()
                .collect(Collectors.groupingBy(VoucherEntry::getVoucherId));

        Set<Long> allLedgerIds = new HashSet<>();
        allLedgerIds.add(ledgerId);
        allEntriesForVouchers.stream()
                .map(VoucherEntry::getLedgerId)
                .forEach(allLedgerIds::add);

        Map<Long, Ledger> ledgerMap = ledgerRepository.findAllById(allLedgerIds)
                .stream()
                .collect(Collectors.toMap(Ledger::getId, Function.identity()));

        BigDecimal runningBalance = signedOpeningBalance;
        if (!entries.isEmpty() && pageable.getPageNumber() > 0) {
            VoucherEntry firstEntry = entries.getFirst();
            Voucher firstVoucher = voucherMap.get(firstEntry.getVoucherId());
            BigDecimal priorNet = voucherEntryRepository.sumBeforeEntry(
                    ledgerId, firstVoucher.getVoucherDate(), firstEntry.getLineNumber());
            runningBalance = signedOpeningBalance.add(priorNet);
        }

        List<AccountLedgerEntryItem> items = new ArrayList<>();

        if (!isZero(openingBalance) && pageable.getPageNumber() == 0) {
            items.add(new AccountLedgerEntryItem()
                    .date(null)
                    .ledgerName("Opening Balance")
                    .vtype(null)
                    .debitAmt(openingBalanceType == AccountEntryType.DR ? openingBalance : null)
                    .creditAmt(openingBalanceType == AccountEntryType.CR ? openingBalance : null)
                    .balance(openingBalance)
                    .crdr(openingBalanceType));
        }

        for (VoucherEntry entry : entries) {
            Voucher voucher = voucherMap.get(entry.getVoucherId());

            String contraLedgerName = "";
            List<VoucherEntry> voucherEntries = entriesByVoucherId.get(entry.getVoucherId());
            if (voucherEntries != null) {
                contraLedgerName = voucherEntries.stream()
                        .filter(ve -> !ve.getLedgerId().equals(ledgerId))
                        .findFirst()
                        .map(ve -> {
                            Ledger l = ledgerMap.get(ve.getLedgerId());
                            return l != null ? l.getName() : "";
                        })
                        .orElse("");
            }

            runningBalance = runningBalance.add(entry.getDebitAmount()).subtract(entry.getCreditAmount());

            AccountEntryType crdr;
            BigDecimal balance;
            if (runningBalance.compareTo(BigDecimal.ZERO) >= 0) {
                crdr = AccountEntryType.DR;
                balance = runningBalance;
            } else {
                crdr = AccountEntryType.CR;
                balance = runningBalance.abs();
            }

            var item = new AccountLedgerEntryItem()
                    .date(voucher.getVoucherDate().toString())
                    .ledgerName(contraLedgerName)
                    .vtype(voucher.getVoucherType().name())
                    .debitAmt(isZero(entry.getDebitAmount()) ? null : entry.getDebitAmount())
                    .creditAmt(isZero(entry.getCreditAmount()) ? null : entry.getCreditAmount())
                    .balance(balance)
                    .crdr(crdr);

            items.add(item);
        }

        int totalPages = (int) Math.ceil((double) totalCount / pageable.getPageSize());
        var pagination = new PaginationMeta()
                .totalPages(totalPages)
                .totalElements(totalCount)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize());

        return new AccountLedgerPageResponse()
                .data(items)
                .pagination(pagination);
    }

    private static boolean isZero(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }
}
