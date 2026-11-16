package in.lekhai.accountbooks.bankreconciliation.service;

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
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class BankReconciliationService {

    private static final Pattern INSTRUMENT_PATTERN = Pattern.compile(
            "\\b(NEFT|RTGS|IMPS|UPI|NET\\s*T/?F|TRANSFER|CHQ|CHEQUE|DD|CASH)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private final AccountGroupRepository accountGroupRepository;
    private final LedgerRepository ledgerRepository;
    private final VoucherEntryRepository voucherEntryRepository;
    private final VoucherRepository voucherRepository;

    public BankReconciliationService(
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
    public BankReconciliationResponse getBankReconciliation(
            Long bankLedgerId,
            LocalDate fromDate,
            LocalDate toDate,
            BankReconciliationMode mode
    ) {
        BankReconciliationMode activeMode = mode != null ? mode : BankReconciliationMode.ALL;

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

        List<BankReconciliationEntryItem> allItems = new ArrayList<>();
        BigDecimal runningBalance = openingAsOf;
        BigDecimal totalDeposits = BigDecimal.ZERO;
        BigDecimal totalWithdrawals = BigDecimal.ZERO;
        BigDecimal unclearedDeposits = BigDecimal.ZERO;
        BigDecimal unclearedWithdrawals = BigDecimal.ZERO;

        for (LedgerEntryWithBalanceProjection entry : rawEntries) {
            BigDecimal debit = entry.debitAmount() != null ? entry.debitAmount() : BigDecimal.ZERO;
            BigDecimal credit = entry.creditAmount() != null ? entry.creditAmount() : BigDecimal.ZERO;

            totalDeposits = totalDeposits.add(debit);
            totalWithdrawals = totalWithdrawals.add(credit);
            runningBalance = runningBalance.add(debit).subtract(credit);

            Voucher voucher = voucherMap.get(entry.voucherId());
            String contraName = resolveContraLedgerName(entry, entriesByVoucherId, contraLedgerMap, bankLedgerId);
            String narration = voucher != null && voucher.getNarration() != null ? voucher.getNarration().trim() : "";

            String particulars = contraName;
            if (!narration.isEmpty()) {
                particulars = particulars.isEmpty() ? narration : particulars + "  " + narration;
            }

            String cheque = extractInstrumentMode(voucher, entry);
            boolean isCleared = entry.clearDate() != null;

            if (!isCleared) {
                unclearedDeposits = unclearedDeposits.add(debit);
                unclearedWithdrawals = unclearedWithdrawals.add(credit);
            }

            BankReconciliationEntryItem item = new BankReconciliationEntryItem()
                    .voucherEntryId(entry.id())
                    .voucherId(entry.voucherId())
                    .inputDate(entry.voucherDate())
                    .particulars(particulars)
                    .cheque(cheque)
                    .deposits(debit.signum() > 0 ? debit : null)
                    .withdrawals(credit.signum() > 0 ? credit : null)
                    .balance(runningBalance.abs())
                    .balanceType(runningBalance.signum() >= 0 ? AccountEntryType.DR : AccountEntryType.CR)
                    .clearDate(entry.clearDate())
                    .isCleared(isCleared);

            allItems.add(item);
        }

        // BRS Statement Summary
        AccountEntryType closingType = runningBalance.signum() >= 0 ? AccountEntryType.DR : AccountEntryType.CR;
        BigDecimal companySigned = runningBalance;
        BigDecimal bankSigned = companySigned.subtract(unclearedDeposits).add(unclearedWithdrawals);

        BankReconciliationStatementSummary statementSummary = new BankReconciliationStatementSummary()
                .balanceAsPerCompany(runningBalance.abs())
                .balanceAsPerCompanyType(closingType)
                .unclearedDeposits(unclearedDeposits)
                .unclearedWithdrawals(unclearedWithdrawals)
                .balanceAsPerBank(bankSigned.abs())
                .balanceAsPerBankType(bankSigned.signum() >= 0 ? AccountEntryType.DR : AccountEntryType.CR);

        // Filter items based on requested mode
        List<BankReconciliationEntryItem> filteredItems = switch (activeMode) {
            case ALL -> allItems;
            case UNCLEARED, STATEMENT -> allItems.stream().filter(e -> !Boolean.TRUE.equals(e.getIsCleared())).toList();
            case CLEARED -> allItems.stream().filter(e -> Boolean.TRUE.equals(e.getIsCleared())).toList();
        };

        return new BankReconciliationResponse()
                .bankLedgerId(bankLedgerId)
                .bankName(bankLedger.getName())
                .accountGroup(accountGroupHierarchy)
                .mode(activeMode)
                .fromDate(start)
                .toDate(end)
                .openingBalance(openingAsOf.abs())
                .openingBalanceType(openingAsOf.signum() >= 0 ? AccountEntryType.DR : AccountEntryType.CR)
                .totalDeposits(totalDeposits)
                .totalWithdrawals(totalWithdrawals)
                .closingBalance(runningBalance.abs())
                .closingBalanceType(closingType)
                .statementSummary(statementSummary)
                .entries(filteredItems);
    }

    @ShopContextTransactional
    public UpdateClearDateResponse updateClearDate(Long voucherEntryId, LocalDate clearDate) {
        if (voucherEntryId == null) {
            throw new IllegalArgumentException("voucherEntryId cannot be null");
        }

        int rows = voucherEntryRepository.updateClearDate(voucherEntryId, clearDate);
        if (rows == 0) {
            return new UpdateClearDateResponse()
                    .success(false)
                    .message("Voucher entry not found: " + voucherEntryId)
                    .voucherEntryId(voucherEntryId)
                    .clearDate(clearDate);
        }

        return new UpdateClearDateResponse()
                .success(true)
                .message("Clear date updated successfully")
                .voucherEntryId(voucherEntryId)
                .clearDate(clearDate);
    }

    private String resolveAccountGroupHierarchy(Long accountGroupId) {
        if (accountGroupId == null) return "Bank Accounts";
        AccountGroup group = accountGroupRepository.findById(accountGroupId).orElse(null);
        if (group == null) return "Bank Accounts";

        if (group.getParentId() != null) {
            AccountGroup parent = accountGroupRepository.findById(group.getParentId()).orElse(null);
            if (parent != null) {
                return group.getName() + " Under : " + parent.getName();
            }
        }
        return group.getName();
    }

    private String resolveContraLedgerName(
            LedgerEntryWithBalanceProjection entry,
            Map<Long, List<VoucherEntry>> entriesByVoucherId,
            Map<Long, Ledger> contraLedgerMap,
            Long bankLedgerId
    ) {
        List<VoucherEntry> siblings = entriesByVoucherId.getOrDefault(entry.voucherId(), List.of());
        List<VoucherEntry> contras = siblings.stream()
                .filter(ve -> !bankLedgerId.equals(ve.getLedgerId()))
                .toList();

        if (contras.isEmpty()) {
            return "";
        }

        return contras.stream()
                .map(ve -> {
                    Ledger l = contraLedgerMap.get(ve.getLedgerId());
                    return l != null ? l.getName() : "Ledger #" + ve.getLedgerId();
                })
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private String extractInstrumentMode(Voucher voucher, LedgerEntryWithBalanceProjection entry) {
        if (voucher != null && voucher.getNarration() != null) {
            Matcher matcher = INSTRUMENT_PATTERN.matcher(voucher.getNarration());
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
        }
        return "NEFT";
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
