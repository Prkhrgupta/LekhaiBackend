package in.lekhai.accountbooks.journalbook.service;

import in.lekhai.contract.model.JournalBookEntryItem;
import in.lekhai.contract.model.JournalBookResponse;
import in.lekhai.contract.model.VoucherType;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import in.lekhai.voucher.entity.Voucher;
import in.lekhai.voucher.entity.VoucherEntry;
import in.lekhai.voucher.repository.VoucherEntryRepository;
import in.lekhai.voucher.repository.VoucherRepository;
import in.lekhai.voucher.util.VoucherTypePrefixes;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JournalBookService {

    private final VoucherRepository voucherRepository;
    private final VoucherEntryRepository voucherEntryRepository;
    private final LedgerRepository ledgerRepository;

    public JournalBookService(
            VoucherRepository voucherRepository,
            VoucherEntryRepository voucherEntryRepository,
            LedgerRepository ledgerRepository
    ) {
        this.voucherRepository = voucherRepository;
        this.voucherEntryRepository = voucherEntryRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @ShopContextTransactional
    public JournalBookResponse getJournalBook(LocalDate fromDate, LocalDate toDate) {
        LocalDate[] range = resolveDateRange(fromDate, toDate);
        LocalDate start = range[0];
        LocalDate end = range[1];

        List<Voucher> vouchers = voucherRepository.findByVoucherTypeAndDateBetween(
                VoucherType.JOURNAL.name(), start, end);

        if (vouchers == null || vouchers.isEmpty()) {
            return new JournalBookResponse()
                    .fromDate(start)
                    .toDate(end)
                    .totalDebit(BigDecimal.ZERO)
                    .totalCredit(BigDecimal.ZERO)
                    .entries(List.of());
        }

        List<Long> voucherIds = vouchers.stream().map(Voucher::getId).toList();
        List<VoucherEntry> allEntries = voucherEntryRepository.findByVoucherIdIn(voucherIds);

        Map<Long, List<VoucherEntry>> entriesByVoucherId = (allEntries != null ? allEntries : List.<VoucherEntry>of())
                .stream()
                .collect(Collectors.groupingBy(VoucherEntry::getVoucherId));

        Set<Long> ledgerIds = (allEntries != null ? allEntries : List.<VoucherEntry>of())
                .stream()
                .map(VoucherEntry::getLedgerId)
                .collect(Collectors.toSet());

        Map<Long, String> ledgerNames = new HashMap<>();
        if (!ledgerIds.isEmpty()) {
            ledgerRepository.findAllById(ledgerIds)
                    .forEach(l -> ledgerNames.put(l.getId(), l.getName()));
        }

        List<JournalBookEntryItem> resultEntries = new ArrayList<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (Voucher voucher : vouchers) {
            List<VoucherEntry> entries = entriesByVoucherId.getOrDefault(voucher.getId(), List.of());
            if (entries.isEmpty()) {
                continue;
            }

            // In journal entries, show Debits first, then Credits, maintaining line order
            List<VoucherEntry> sortedEntries = entries.stream()
                    .sorted(Comparator.comparing((VoucherEntry e) -> e.getDebitAmount() != null && e.getDebitAmount().signum() > 0 ? 0 : 1)
                            .thenComparing(e -> e.getLineNumber() != null ? e.getLineNumber() : 0))
                    .toList();

            String prefix = VoucherTypePrefixes.get(VoucherType.JOURNAL);
            String vchNumberStr = (prefix != null ? prefix : "JN") + "-" + voucher.getVoucherNumber();

            boolean isFirstRowOfVoucher = true;

            for (VoucherEntry entry : sortedEntries) {
                String ledgerName = ledgerNames.getOrDefault(entry.getLedgerId(), "Ledger #" + entry.getLedgerId());
                boolean isDebit = entry.getDebitAmount() != null && entry.getDebitAmount().signum() > 0;
                boolean isCredit = entry.getCreditAmount() != null && entry.getCreditAmount().signum() > 0;

                String particulars;
                BigDecimal debit = null;
                BigDecimal credit = null;

                if (isDebit) {
                    particulars = "BY " + ledgerName;
                    debit = entry.getDebitAmount();
                    totalDebit = totalDebit.add(debit);
                } else if (isCredit) {
                    particulars = "TO " + ledgerName;
                    credit = entry.getCreditAmount();
                    totalCredit = totalCredit.add(credit);
                } else {
                    particulars = ledgerName;
                }

                JournalBookEntryItem item = new JournalBookEntryItem()
                        .date(isFirstRowOfVoucher ? voucher.getVoucherDate().toString() : null)
                        .voucherNumber(vchNumberStr)
                        .voucherId(voucher.getId())
                        .particulars(particulars)
                        .debitAmount(debit)
                        .creditAmount(credit)
                        .entryType("ENTRY")
                        .isNarration(false);

                resultEntries.add(item);
                isFirstRowOfVoucher = false;
            }

            // Append narration row if narration is present
            if (voucher.getNarration() != null && !voucher.getNarration().isBlank()) {
                JournalBookEntryItem narrationItem = new JournalBookEntryItem()
                        .date(null)
                        .voucherNumber(vchNumberStr)
                        .voucherId(voucher.getId())
                        .particulars("  " + voucher.getNarration().trim())
                        .debitAmount(null)
                        .creditAmount(null)
                        .entryType("NARRATION")
                        .isNarration(true);

                resultEntries.add(narrationItem);
            }
        }

        return new JournalBookResponse()
                .fromDate(start)
                .toDate(end)
                .totalDebit(totalDebit)
                .totalCredit(totalCredit)
                .entries(resultEntries);
    }

    private LocalDate[] resolveDateRange(LocalDate fromDate, LocalDate toDate) {
        Year financialYearStart = null;
        try {
            financialYearStart = JwtUtil.extractJwtClaim().financialYearStart();
        } catch (Exception ignored) {
        }
        int startYear = financialYearStart != null ? financialYearStart.getValue() : 2026;

        LocalDate defaultStart = LocalDate.of(startYear, 4, 1);
        LocalDate defaultEnd = LocalDate.of(startYear + 1, 3, 31);

        LocalDate start = fromDate != null ? fromDate : defaultStart;
        LocalDate end = toDate != null ? toDate : defaultEnd;

        return new LocalDate[]{start, end};
    }
}
