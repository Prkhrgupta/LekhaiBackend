package in.lekhai.accountbooks.bankledger.service;

import in.lekhai.contract.model.AccountEntryType;
import in.lekhai.contract.model.AccountLedgerContraItem;
import in.lekhai.contract.model.BankLedgerEntryItem;
import in.lekhai.contract.model.BankLedgerResponse;
import in.lekhai.contract.model.VoucherType;
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
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class BankLedgerService {

    private static final String OPENING_BALANCE_LABEL = "Opening Balance";
    private static final Pattern INSTRUMENT_PATTERN = Pattern.compile(
            "\\b(NEFT|RTGS|IMPS|UPI|NET\\s*T/?F|TRANSFER|CHQ|CHEQUE|DD|CASH)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private final AccountGroupRepository accountGroupRepository;
    private final LedgerRepository ledgerRepository;
    private final VoucherEntryRepository voucherEntryRepository;
    private final VoucherRepository voucherRepository;

    public BankLedgerService(
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
    public BankLedgerResponse getBankLedger(
            Long bankLedgerId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Ledger bankLedger = ledgerRepository.findById(bankLedgerId)
                .orElseThrow(() -> new IllegalArgumentException("Bank ledger not found: " + bankLedgerId));

        String accountGroupHierarchy = resolveAccountGroupHierarchy(bankLedger.getAccountGroupId());

        LocalDate[] range = resolveDateRange(fromDate, toDate);
        LocalDate start = range[0];
        LocalDate end = range[1];

        BigDecimal signedOpening = bankLedger.getOpeningBalance() != null
                ? bankLedger.getOpeningBalance()
                : BigDecimal.ZERO;
        if (bankLedger.getOpeningBalanceType() == AccountEntryType.CR) {
            signedOpening = signedOpening.negate();
        }

        BigDecimal priorNet = voucherEntryRepository.sumNetBefore(bankLedgerId, start);
        BigDecimal openingAsOf = signedOpening.add(priorNet != null ? priorNet : BigDecimal.ZERO);

        List<LedgerEntryWithBalanceProjection> rawEntries =
                voucherEntryRepository.findEntriesByLedgerIdsBetweenDates(List.of(bankLedgerId), start, end);

        List<Long> voucherIds = rawEntries.stream()
                .map(LedgerEntryWithBalanceProjection::voucherId)
                .distinct()
                .toList();

        Map<Long, Voucher> voucherMap = loadVouchers(voucherIds);
        Map<Long, List<VoucherEntry>> entriesByVoucherId = loadSiblingEntries(voucherIds);
        Map<Long, Ledger> contraLedgerMap = loadContraLedgers(bankLedgerId, entriesByVoucherId);

        List<BankLedgerEntryItem> allEntries = new ArrayList<>();

        // 1. Initial Opening Balance Item
        AccountEntryType openingType = crdr(openingAsOf);
        String openingParticulars = openingAsOf.signum() >= 0 ? "To Opening Balance" : "By Opening Balance";
        allEntries.add(new BankLedgerEntryItem()
                .date(start.toString())
                .particulars(openingParticulars)
                .vchType(null)
                .instrumentMode(null)
                .debit(openingType == AccountEntryType.DR ? openingAsOf.abs() : null)
                .credit(openingType == AccountEntryType.CR ? openingAsOf.abs() : null)
                .balance(openingAsOf.abs())
                .balanceType(openingType)
                .entryType("OPENING"));

        BigDecimal runningBalance = openingAsOf;
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        // 2. Transaction Items
        for (LedgerEntryWithBalanceProjection entry : rawEntries) {
            Voucher voucher = voucherMap.get(entry.voucherId());
            boolean isDebit = entry.debitAmount() != null && entry.debitAmount().compareTo(BigDecimal.ZERO) > 0;
            BigDecimal debit = isDebit ? entry.debitAmount() : null;
            BigDecimal credit = !isDebit ? entry.creditAmount() : null;

            runningBalance = runningBalance
                    .add(debit != null ? debit : BigDecimal.ZERO)
                    .subtract(credit != null ? credit : BigDecimal.ZERO);

            List<AccountLedgerContraItem> contraList =
                    buildContraEntries(entry, entriesByVoucherId, contraLedgerMap, bankLedgerId);
            String contraName = joinContraNames(contraList);
            if (contraName.isBlank()) {
                contraName = voucher != null && voucher.getNarration() != null && !voucher.getNarration().isBlank()
                        ? voucher.getNarration()
                        : "Sundry Entries";
            }

            String particulars = isDebit ? "To " + contraName : "By " + contraName;
            String vchType = voucher != null ? mapShortVoucherType(voucher.getVoucherType()) : "";
            String instrumentMode = extractInstrumentMode(voucher, entry);

            allEntries.add(new BankLedgerEntryItem()
                    .date(entry.voucherDate() != null ? entry.voucherDate().toString() : "")
                    .particulars(particulars)
                    .vchType(vchType)
                    .instrumentMode(instrumentMode)
                    .debit(debit)
                    .credit(credit)
                    .balance(runningBalance.abs())
                    .balanceType(crdr(runningBalance))
                    .entryType("TRANSACTION")
                    .narration(voucher != null ? voucher.getNarration() : null));

            if (debit != null) totalDebit = totalDebit.add(debit);
            if (credit != null) totalCredit = totalCredit.add(credit);
        }

        return new BankLedgerResponse()
                .bankLedgerId(bankLedgerId)
                .bankName(bankLedger.getName())
                .accountGroup(accountGroupHierarchy)
                .fromDate(start)
                .toDate(end)
                .openingBalance(openingAsOf.abs())
                .openingBalanceType(openingType)
                .totalDebit(totalDebit)
                .totalCredit(totalCredit)
                .closingBalance(runningBalance.abs())
                .closingBalanceType(crdr(runningBalance))
                .entries(allEntries);
    }

    private String resolveAccountGroupHierarchy(Long groupId) {
        if (groupId == null) return "Bank Accounts";
        Optional<AccountGroup> groupOpt = accountGroupRepository.findById(groupId);
        if (groupOpt.isEmpty()) return "Bank Accounts";

        AccountGroup group = groupOpt.get();
        if (group.getParentId() != null) {
            Optional<AccountGroup> parentOpt = accountGroupRepository.findById(group.getParentId());
            if (parentOpt.isPresent()) {
                return group.getName() + " Under : " + parentOpt.get().getName();
            }
        }
        return group.getName();
    }

    private String mapShortVoucherType(VoucherType type) {
        if (type == null) return "";
        return switch (type) {
            case RECEIPT -> "Rcpt";
            case PAYMENT -> "Pymt";
            case CONTRA -> "Ctra";
            case SALES -> "Sales";
            case PURCHASE -> "Purch";
            case JOURNAL -> "Jrnl";
            default -> type.name();
        };
    }

    private String extractInstrumentMode(Voucher voucher, LedgerEntryWithBalanceProjection entry) {
        String textToSearch = "";
        if (voucher != null && voucher.getNarration() != null) {
            textToSearch += voucher.getNarration() + " ";
        }
        Matcher matcher = INSTRUMENT_PATTERN.matcher(textToSearch);
        if (matcher.find()) {
            String found = matcher.group(1).toUpperCase();
            if (found.startsWith("NET") || found.contains("T/F") || found.equals("TRANSFER")) {
                return "NET T/F";
            }
            if (found.equals("CHEQUE") || found.equals("CHQ")) {
                return "CHQ";
            }
            return found;
        }
        return "";
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

    private Map<Long, Ledger> loadContraLedgers(Long bankLedgerId, Map<Long, List<VoucherEntry>> entriesByVoucherId) {
        Set<Long> ledgerIds = new HashSet<>();
        ledgerIds.add(bankLedgerId);
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
            Long bankLedgerId
    ) {
        List<VoucherEntry> voucherEntries = entriesByVoucherId.get(entry.voucherId());
        if (voucherEntries == null) return List.of();

        boolean onDebitSide = !isZero(entry.debitAmount());
        boolean onCreditSide = !isZero(entry.creditAmount());

        List<VoucherEntry> contra = voucherEntries.stream()
                .filter(ve -> !bankLedgerId.equals(ve.getLedgerId()))
                .filter(ve -> onDebitSide
                        ? !isZero(ve.getCreditAmount())
                        : onCreditSide ? !isZero(ve.getDebitAmount()) : true)
                .sorted(Comparator.comparing(VoucherEntry::getLineNumber,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        if (contra.isEmpty()) {
            contra = voucherEntries.stream()
                    .filter(ve -> !bankLedgerId.equals(ve.getLedgerId()))
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
