package in.lekhai.accountbooks.balancesheet.service;

import in.lekhai.accountbooks.balancesheet.repository.LedgerNetTotalProjection;
import in.lekhai.accountbooks.balancesheet.repository.NatureTotalProjection;
import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.domain.AccountGroup;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import in.lekhai.voucher.repository.VoucherEntryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BalanceSheetService {

    private final AccountGroupRepository accountGroupRepository;
    private final LedgerRepository ledgerRepository;
    private final VoucherEntryRepository voucherEntryRepository;

    public BalanceSheetService(
            AccountGroupRepository accountGroupRepository,
            LedgerRepository ledgerRepository,
            VoucherEntryRepository voucherEntryRepository
    ) {
        this.accountGroupRepository = accountGroupRepository;
        this.ledgerRepository = ledgerRepository;
        this.voucherEntryRepository = voucherEntryRepository;
    }

    @ShopContextTransactional
    public BalanceSheetResponse getBalanceSheet(
            LocalDate fromDate,
            LocalDate toDate,
            boolean booksClosed,
            String format
    ) {
        LocalDate[] range = resolveDateRange(fromDate, toDate);
        LocalDate start = range[0];
        LocalDate end = range[1];
        boolean isDetail = "DETAIL".equalsIgnoreCase(format);

        // 1. Load groups
        List<AccountGroup> allGroups = accountGroupRepository.findAll();
        Map<Long, AccountGroup> groupMap = allGroups.stream()
                .collect(Collectors.toMap(AccountGroup::getId, g -> g));

        // 2. Load ledgers & voucher totals up to end date
        List<Ledger> allLedgers = ledgerRepository.findAll();
        List<LedgerNetTotalProjection> voucherTotals = voucherEntryRepository.sumNetByLedgerUpToDate(end);
        Map<Long, LedgerNetTotalProjection> totalByLedger = voucherTotals.stream()
                .collect(Collectors.toMap(LedgerNetTotalProjection::ledgerId, t -> t));

        // 3. Separate Ledgers into Liabilities and Assets
        Map<AccountGroup, Map<AccountGroup, List<LedgerItemData>>> liabilityTree = new LinkedHashMap<>();
        Map<AccountGroup, Map<AccountGroup, List<LedgerItemData>>> assetTree = new LinkedHashMap<>();

        for (Ledger ledger : allLedgers) {
            if (Boolean.FALSE.equals(ledger.getActive())) {
                continue;
            }
            AccountGroup group = groupMap.get(ledger.getAccountGroupId());
            if (group == null) {
                continue;
            }
            AccountGroup primary = findPrimaryAncestor(group, groupMap);
            String nature = primary.getNature() != null ? primary.getNature() : group.getNature();

            if (!"LIABILITY".equalsIgnoreCase(nature) && !"ASSET".equalsIgnoreCase(nature)) {
                continue;
            }

            BigDecimal signedOpening = signedOpeningBalance(ledger);
            LedgerNetTotalProjection totals = totalByLedger.get(ledger.getId());
            BigDecimal debit = totals != null && totals.totalDebit() != null ? totals.totalDebit() : BigDecimal.ZERO;
            BigDecimal credit = totals != null && totals.totalCredit() != null ? totals.totalCredit() : BigDecimal.ZERO;
            BigDecimal closingNetDebit = signedOpening.add(debit).subtract(credit);

            AccountGroup subGroup = findImmediateSubGroup(group, primary, groupMap);
            AccountEntryType crdr = closingNetDebit.signum() >= 0 ? AccountEntryType.DR : AccountEntryType.CR;

            BigDecimal displayAmount;
            if ("LIABILITY".equalsIgnoreCase(nature)) {
                displayAmount = closingNetDebit.negate();
                liabilityTree.computeIfAbsent(primary, k -> new LinkedHashMap<>())
                        .computeIfAbsent(subGroup, k -> new ArrayList<>())
                        .add(new LedgerItemData(ledger, displayAmount, crdr));
            } else {
                displayAmount = closingNetDebit;
                assetTree.computeIfAbsent(primary, k -> new LinkedHashMap<>())
                        .computeIfAbsent(subGroup, k -> new ArrayList<>())
                        .add(new LedgerItemData(ledger, displayAmount, crdr));
            }
        }

        // 4. Build Group Items for Liabilities and Assets
        List<BalanceSheetGroupItem> liabilityGroups = buildGroupItems(liabilityTree, isDetail);
        List<BalanceSheetGroupItem> assetGroups = buildGroupItems(assetTree, isDetail);

        BigDecimal totalLiabilities = liabilityGroups.stream()
                .map(BalanceSheetGroupItem::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAssets = assetGroups.stream()
                .map(BalanceSheetGroupItem::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. If books are NOT closed, compute Net Profit / Loss from Income and Expense
        BigDecimal netProfitLoss = BigDecimal.ZERO;
        boolean isProfit = true;

        if (!booksClosed) {
            List<NatureTotalProjection> natureTotals = voucherEntryRepository.sumIncomeExpenseBetweenDates(start, end);
            BigDecimal totalIncome = BigDecimal.ZERO;
            BigDecimal totalExpense = BigDecimal.ZERO;

            for (NatureTotalProjection nt : natureTotals) {
                BigDecimal deb = nt.totalDebit() != null ? nt.totalDebit() : BigDecimal.ZERO;
                BigDecimal cred = nt.totalCredit() != null ? nt.totalCredit() : BigDecimal.ZERO;
                if ("INCOME".equalsIgnoreCase(nt.nature())) {
                    totalIncome = cred.subtract(deb);
                } else if ("EXPENSE".equalsIgnoreCase(nt.nature())) {
                    totalExpense = deb.subtract(cred);
                }
            }

            netProfitLoss = totalIncome.subtract(totalExpense);
            isProfit = netProfitLoss.compareTo(BigDecimal.ZERO) >= 0;

            if (netProfitLoss.compareTo(BigDecimal.ZERO) != 0) {
                if (isProfit) {
                    BalanceSheetGroupItem plItem = new BalanceSheetGroupItem()
                            .groupId(10L)
                            .groupName("Profit & Loss A/c (Net Profit)")
                            .amount(netProfitLoss)
                            .subGroups(new ArrayList<>())
                            .ledgers(new ArrayList<>());
                    liabilityGroups.add(plItem);
                    totalLiabilities = totalLiabilities.add(netProfitLoss);
                } else {
                    BigDecimal loss = netProfitLoss.abs();
                    BalanceSheetGroupItem plItem = new BalanceSheetGroupItem()
                            .groupId(10L)
                            .groupName("Profit & Loss A/c (Net Loss)")
                            .amount(loss)
                            .subGroups(new ArrayList<>())
                            .ledgers(new ArrayList<>());
                    assetGroups.add(plItem);
                    totalAssets = totalAssets.add(loss);
                }
            }
        }

        BigDecimal differenceInBalance = totalAssets.subtract(totalLiabilities);

        return new BalanceSheetResponse()
                .fromDate(start.toString())
                .toDate(end.toString())
                .booksClosed(booksClosed)
                .format(isDetail ? "DETAIL" : "SUMMARY")
                .liabilities(liabilityGroups)
                .assets(assetGroups)
                .totalLiabilities(totalLiabilities)
                .totalAssets(totalAssets)
                .netProfitLoss(netProfitLoss)
                .isProfit(isProfit)
                .differenceInBalance(differenceInBalance);
    }

    private List<BalanceSheetGroupItem> buildGroupItems(
            Map<AccountGroup, Map<AccountGroup, List<LedgerItemData>>> tree,
            boolean isDetail
    ) {
        List<BalanceSheetGroupItem> result = new ArrayList<>();

        for (Map.Entry<AccountGroup, Map<AccountGroup, List<LedgerItemData>>> primaryEntry : tree.entrySet()) {
            AccountGroup primary = primaryEntry.getKey();
            Map<AccountGroup, List<LedgerItemData>> subMap = primaryEntry.getValue();

            BigDecimal groupTotal = BigDecimal.ZERO;
            List<BalanceSheetSubGroupItem> subGroupItems = new ArrayList<>();
            List<BalanceSheetLedgerItem> allPrimaryLedgers = new ArrayList<>();

            for (Map.Entry<AccountGroup, List<LedgerItemData>> subEntry : subMap.entrySet()) {
                AccountGroup subGroup = subEntry.getKey();
                List<LedgerItemData> ledgerDataList = subEntry.getValue();

                BigDecimal subTotal = BigDecimal.ZERO;
                List<BalanceSheetLedgerItem> subLedgers = new ArrayList<>();

                for (LedgerItemData item : ledgerDataList) {
                    subTotal = subTotal.add(item.amount);
                    if (isDetail) {
                        subLedgers.add(new BalanceSheetLedgerItem()
                                .ledgerId(item.ledger.getId())
                                .ledgerName(item.ledger.getName())
                                .amount(item.amount)
                                .crdr(item.crdr));
                    }
                }

                groupTotal = groupTotal.add(subTotal);
                if (isDetail) {
                    allPrimaryLedgers.addAll(subLedgers);
                    subGroupItems.add(new BalanceSheetSubGroupItem()
                            .groupId(subGroup.getId())
                            .groupName(subGroup.getName())
                            .amount(subTotal)
                            .ledgers(subLedgers));
                }
            }

            result.add(new BalanceSheetGroupItem()
                    .groupId(primary.getId())
                    .groupName(primary.getName())
                    .amount(groupTotal)
                    .subGroups(subGroupItems)
                    .ledgers(allPrimaryLedgers));
        }

        return result;
    }

    private AccountGroup findPrimaryAncestor(AccountGroup group, Map<Long, AccountGroup> groupMap) {
        AccountGroup curr = group;
        while (curr.getParentId() != null && groupMap.containsKey(curr.getParentId())) {
            curr = groupMap.get(curr.getParentId());
        }
        return curr;
    }

    private AccountGroup findImmediateSubGroup(AccountGroup group, AccountGroup primary, Map<Long, AccountGroup> groupMap) {
        if (Objects.equals(group.getId(), primary.getId()) || group.getParentId() == null) {
            return group;
        }
        AccountGroup curr = group;
        AccountGroup prev = group;
        while (curr.getParentId() != null && !Objects.equals(curr.getParentId(), primary.getId()) && groupMap.containsKey(curr.getParentId())) {
            prev = curr;
            curr = groupMap.get(curr.getParentId());
        }
        return curr.getParentId() != null && Objects.equals(curr.getParentId(), primary.getId()) ? curr : prev;
    }

    private BigDecimal signedOpeningBalance(Ledger ledger) {
        BigDecimal opening = ledger.getOpeningBalance() != null ? ledger.getOpeningBalance() : BigDecimal.ZERO;
        return ledger.getOpeningBalanceType() == AccountEntryType.CR
                ? opening.negate()
                : opening;
    }

    private LocalDate[] resolveDateRange(LocalDate fromDate, LocalDate toDate) {
        LocalDate start = fromDate;
        LocalDate end = toDate;
        if (start == null || end == null) {
            int fyYear = 2026;
            try {
                Year financialYearStart = JwtUtil.extractJwtClaim().financialYearStart();
                if (financialYearStart != null) {
                    fyYear = financialYearStart.getValue();
                }
            } catch (Exception ignored) {}
            start = start != null ? start : LocalDate.of(fyYear, 4, 1);
            end = end != null ? end : LocalDate.of(fyYear + 1, 3, 31);
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }
        return new LocalDate[]{start, end};
    }

    private record LedgerItemData(Ledger ledger, BigDecimal amount, AccountEntryType crdr) {}
}
