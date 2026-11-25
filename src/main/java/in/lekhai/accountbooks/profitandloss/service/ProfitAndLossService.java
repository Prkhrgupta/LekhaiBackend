package in.lekhai.accountbooks.profitandloss.service;

import in.lekhai.accountbooks.balancesheet.repository.LedgerNetTotalProjection;
import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.domain.AccountGroup;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.salesinvoice.repository.StockLedgerRepository;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import in.lekhai.voucher.repository.VoucherEntryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProfitAndLossService {

    public static final long GROUP_DIRECT_INCOMES = 11L;
    public static final long GROUP_INDIRECT_INCOMES = 12L;
    public static final long GROUP_DIRECT_EXPENSES = 13L;
    public static final long GROUP_INDIRECT_EXPENSES = 14L;
    public static final long GROUP_STOCK_IN_HAND = 34L;

    private final AccountGroupRepository accountGroupRepository;
    private final LedgerRepository ledgerRepository;
    private final VoucherEntryRepository voucherEntryRepository;
    private final StockLedgerRepository stockLedgerRepository;

    public ProfitAndLossService(
            AccountGroupRepository accountGroupRepository,
            LedgerRepository ledgerRepository,
            VoucherEntryRepository voucherEntryRepository,
            StockLedgerRepository stockLedgerRepository
    ) {
        this.accountGroupRepository = accountGroupRepository;
        this.ledgerRepository = ledgerRepository;
        this.voucherEntryRepository = voucherEntryRepository;
        this.stockLedgerRepository = stockLedgerRepository;
    }

    @ShopContextTransactional
    public ProfitAndLossResponse getProfitAndLoss(
            LocalDate fromDate,
            LocalDate toDate,
            String stockMethod,
            BigDecimal manualOpeningStock,
            BigDecimal manualClosingStock,
            String format
    ) {
        LocalDate[] range = resolveDateRange(fromDate, toDate);
        LocalDate start = range[0];
        LocalDate end = range[1];
        boolean isDetail = "DETAIL".equalsIgnoreCase(format);
        String resolvedStockMethod = (stockMethod == null || stockMethod.isBlank())
                ? "MANUAL"
                : stockMethod.toUpperCase();

        // 1. Resolve stock values
        BigDecimal openingStock = BigDecimal.ZERO;
        BigDecimal closingStock = BigDecimal.ZERO;

        if ("AUTO".equals(resolvedStockMethod)) {
            openingStock = calculateAutoStockBefore(start);
            closingStock = calculateAutoStockUpTo(end);
        } else {
            if (manualOpeningStock != null) openingStock = manualOpeningStock;
            if (manualClosingStock != null) closingStock = manualClosingStock;
        }

        // 2. Load account groups
        List<AccountGroup> allGroups = accountGroupRepository.findAll();
        Map<Long, AccountGroup> groupMap = allGroups.stream()
                .collect(Collectors.toMap(AccountGroup::getId, g -> g));

        // 3. Load ledgers & voucher movements between fromDate and toDate
        List<Ledger> allLedgers = ledgerRepository.findAll();
        List<LedgerNetTotalProjection> movements = voucherEntryRepository.sumNetByLedgerBetweenDates(start, end);
        Map<Long, LedgerNetTotalProjection> movementByLedger = movements.stream()
                .collect(Collectors.toMap(LedgerNetTotalProjection::ledgerId, m -> m));

        // 4. Calculate net signed balances for Income and Expense ledgers
        Map<Long, BigDecimal> ledgerAmountMap = new HashMap<>();
        Map<Long, AccountEntryType> ledgerSideMap = new HashMap<>();

        for (Ledger ledger : allLedgers) {
            AccountGroup group = groupMap.get(ledger.getAccountGroupId());
            if (group == null) continue;
            String nature = group.getNature();
            if (!"INCOME".equalsIgnoreCase(nature) && !"EXPENSE".equalsIgnoreCase(nature)) {
                continue;
            }

            LedgerNetTotalProjection mov = movementByLedger.get(ledger.getId());
            BigDecimal dr = (mov != null && mov.totalDebit() != null) ? mov.totalDebit() : BigDecimal.ZERO;
            BigDecimal cr = (mov != null && mov.totalCredit() != null) ? mov.totalCredit() : BigDecimal.ZERO;

            if ("INCOME".equalsIgnoreCase(nature)) {
                // Income: Credit is positive
                BigDecimal net = cr.subtract(dr);
                if (net.compareTo(BigDecimal.ZERO) >= 0) {
                    ledgerAmountMap.put(ledger.getId(), net);
                    ledgerSideMap.put(ledger.getId(), AccountEntryType.CR);
                } else {
                    ledgerAmountMap.put(ledger.getId(), net.negate());
                    ledgerSideMap.put(ledger.getId(), AccountEntryType.DR);
                }
            } else {
                // Expense: Debit is positive
                BigDecimal net = dr.subtract(cr);
                if (net.compareTo(BigDecimal.ZERO) >= 0) {
                    ledgerAmountMap.put(ledger.getId(), net);
                    ledgerSideMap.put(ledger.getId(), AccountEntryType.DR);
                } else {
                    ledgerAmountMap.put(ledger.getId(), net.negate());
                    ledgerSideMap.put(ledger.getId(), AccountEntryType.CR);
                }
            }
        }

        // 5. Build group hierarchy trees for the 4 primary groups
        ProfitAndLossGroupItem directIncomesGroup = buildGroupTree(
                GROUP_DIRECT_INCOMES, "Direct Incomes", groupMap, allLedgers, ledgerAmountMap, ledgerSideMap, isDetail);
        ProfitAndLossGroupItem directExpensesGroup = buildGroupTree(
                GROUP_DIRECT_EXPENSES, "Direct Expenses", groupMap, allLedgers, ledgerAmountMap, ledgerSideMap, isDetail);
        ProfitAndLossGroupItem indirectIncomesGroup = buildGroupTree(
                GROUP_INDIRECT_INCOMES, "Indirect Incomes", groupMap, allLedgers, ledgerAmountMap, ledgerSideMap, isDetail);
        ProfitAndLossGroupItem indirectExpensesGroup = buildGroupTree(
                GROUP_INDIRECT_EXPENSES, "Indirect Expenses", groupMap, allLedgers, ledgerAmountMap, ledgerSideMap, isDetail);

        BigDecimal totalDirectIncomes = directIncomesGroup.getAmount();
        BigDecimal totalDirectExpenses = directExpensesGroup.getAmount();
        BigDecimal totalIndirectIncomes = indirectIncomesGroup.getAmount();
        BigDecimal totalIndirectExpenses = indirectExpensesGroup.getAmount();

        // 6. Compute Trading Account & Gross Profit
        // Total Trading Credit = Direct Incomes + Closing Stock
        // Total Trading Debit = Opening Stock + Direct Expenses
        BigDecimal tradingCredit = totalDirectIncomes.add(closingStock);
        BigDecimal tradingDebit = openingStock.add(totalDirectExpenses);
        BigDecimal grossProfit = tradingCredit.subtract(tradingDebit);
        boolean isGrossProfit = grossProfit.compareTo(BigDecimal.ZERO) >= 0;
        BigDecimal absGrossProfit = grossProfit.abs();

        // 7. Compute P&L Account & Net Profit
        // P&L Credit = (isGrossProfit ? grossProfit : 0) + Indirect Incomes
        // P&L Debit = (!isGrossProfit ? grossLoss : 0) + Indirect Expenses
        BigDecimal pnlCredit = (isGrossProfit ? absGrossProfit : BigDecimal.ZERO).add(totalIndirectIncomes);
        BigDecimal pnlDebit = (!isGrossProfit ? absGrossProfit : BigDecimal.ZERO).add(totalIndirectExpenses);
        BigDecimal netProfit = pnlCredit.subtract(pnlDebit);
        boolean isNetProfit = netProfit.compareTo(BigDecimal.ZERO) >= 0;
        BigDecimal absNetProfit = netProfit.abs();

        // 8. Assemble Expenses list (Trading Debit + P&L Debit + Balances)
        List<ProfitAndLossGroupItem> expensesList = new ArrayList<>();
        expensesList.add(new ProfitAndLossGroupItem()
                .groupId(-1L)
                .groupName("Opening Stock")
                .amount(openingStock)
                .subGroups(List.of())
                .ledgers(List.of()));

        expensesList.add(directExpensesGroup);

        if (isGrossProfit) {
            expensesList.add(new ProfitAndLossGroupItem()
                    .groupId(-2L)
                    .groupName("Gross Profit c/d")
                    .amount(absGrossProfit)
                    .subGroups(List.of())
                    .ledgers(List.of()));
        } else {
            // Gross loss brought down on P&L debit side
            expensesList.add(new ProfitAndLossGroupItem()
                    .groupId(-3L)
                    .groupName("Gross Loss b/d")
                    .amount(absGrossProfit)
                    .subGroups(List.of())
                    .ledgers(List.of()));
        }

        expensesList.add(indirectExpensesGroup);

        if (isNetProfit) {
            expensesList.add(new ProfitAndLossGroupItem()
                    .groupId(-4L)
                    .groupName("Net Profit")
                    .amount(absNetProfit)
                    .subGroups(List.of())
                    .ledgers(List.of()));
        }

        // 9. Assemble Income list (Trading Credit + P&L Credit + Balances)
        List<ProfitAndLossGroupItem> incomeList = new ArrayList<>();
        incomeList.add(directIncomesGroup);
        incomeList.add(new ProfitAndLossGroupItem()
                .groupId(-5L)
                .groupName("Closing Stock")
                .amount(closingStock)
                .subGroups(List.of())
                .ledgers(List.of()));

        if (!isGrossProfit) {
            incomeList.add(new ProfitAndLossGroupItem()
                    .groupId(-6L)
                    .groupName("Gross Loss c/d")
                    .amount(absGrossProfit)
                    .subGroups(List.of())
                    .ledgers(List.of()));
        } else {
            // Gross profit brought down on P&L credit side
            incomeList.add(new ProfitAndLossGroupItem()
                    .groupId(-7L)
                    .groupName("Gross Profit b/d")
                    .amount(absGrossProfit)
                    .subGroups(List.of())
                    .ledgers(List.of()));
        }

        incomeList.add(indirectIncomesGroup);

        if (!isNetProfit) {
            incomeList.add(new ProfitAndLossGroupItem()
                    .groupId(-8L)
                    .groupName("Net Loss")
                    .amount(absNetProfit)
                    .subGroups(List.of())
                    .ledgers(List.of()));
        }

        // Calculate total displayed on each side
        BigDecimal totalExpenses = expensesList.stream()
                .map(ProfitAndLossGroupItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalIncome = incomeList.stream()
                .map(ProfitAndLossGroupItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ProfitAndLossResponse()
                .fromDate(start.toString())
                .toDate(end.toString())
                .stockMethod(resolvedStockMethod)
                .openingStock(openingStock)
                .closingStock(closingStock)
                .format(format != null ? format.toUpperCase() : "SUMMARY")
                .expenses(expensesList)
                .income(incomeList)
                .totalExpenses(totalExpenses)
                .totalIncome(totalIncome)
                .grossProfit(absGrossProfit)
                .isGrossProfit(isGrossProfit)
                .netProfit(absNetProfit)
                .isNetProfit(isNetProfit);
    }

    private BigDecimal calculateAutoStockBefore(LocalDate date) {
        try {
            BigDecimal stockVal = stockLedgerRepository.sumNetValueBeforeDate(date);
            if (stockVal != null && stockVal.compareTo(BigDecimal.ZERO) != 0) {
                return stockVal;
            }
        } catch (Exception ignored) {
        }
        return getStockInHandLedgerBalance(date, true);
    }

    private BigDecimal calculateAutoStockUpTo(LocalDate date) {
        try {
            BigDecimal stockVal = stockLedgerRepository.sumNetValueUpToDate(date);
            if (stockVal != null && stockVal.compareTo(BigDecimal.ZERO) != 0) {
                return stockVal;
            }
        } catch (Exception ignored) {
        }
        return getStockInHandLedgerBalance(date, false);
    }

    private BigDecimal getStockInHandLedgerBalance(LocalDate date, boolean before) {
        List<Ledger> allLedgers = ledgerRepository.findAll();
        BigDecimal total = BigDecimal.ZERO;
        for (Ledger l : allLedgers) {
            if (l.getAccountGroupId() != null && l.getAccountGroupId() == GROUP_STOCK_IN_HAND) {
                BigDecimal opening = l.getOpeningBalance() != null ? l.getOpeningBalance() : BigDecimal.ZERO;
                BigDecimal netVouchers = before
                        ? voucherEntryRepository.sumNetBefore(l.getId(), date)
                        : voucherEntryRepository.sumNetByLedgerUpToDate(date).stream()
                                .filter(p -> p.ledgerId().equals(l.getId()))
                                .findFirst()
                                .map(p -> p.totalDebit().subtract(p.totalCredit()))
                                .orElse(BigDecimal.ZERO);
                BigDecimal bal = opening.add(netVouchers != null ? netVouchers : BigDecimal.ZERO);
                total = total.add(bal);
            }
        }
        return total.max(BigDecimal.ZERO);
    }

    private ProfitAndLossGroupItem buildGroupTree(
            Long rootGroupId,
            String fallbackName,
            Map<Long, AccountGroup> groupMap,
            List<Ledger> allLedgers,
            Map<Long, BigDecimal> ledgerAmountMap,
            Map<Long, AccountEntryType> ledgerSideMap,
            boolean isDetail
    ) {
        AccountGroup rootGroup = groupMap.get(rootGroupId);
        String groupName = rootGroup != null ? rootGroup.getName() : fallbackName;

        // Find all sub-groups whose primary/root ancestor is rootGroupId
        List<AccountGroup> directSubGroups = groupMap.values().stream()
                .filter(g -> Objects.equals(g.getParentId(), rootGroupId))
                .sorted(Comparator.comparing(AccountGroup::getName))
                .toList();

        // Ledgers directly under rootGroupId
        List<ProfitAndLossLedgerItem> directLedgerItems = allLedgers.stream()
                .filter(l -> Objects.equals(l.getAccountGroupId(), rootGroupId))
                .filter(l -> ledgerAmountMap.getOrDefault(l.getId(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0)
                .map(l -> new ProfitAndLossLedgerItem()
                        .ledgerId(l.getId())
                        .ledgerName(l.getName())
                        .amount(ledgerAmountMap.get(l.getId()))
                        .crdr(ledgerSideMap.get(l.getId())))
                .sorted(Comparator.comparing(ProfitAndLossLedgerItem::getLedgerName))
                .toList();

        BigDecimal directLedgersTotal = directLedgerItems.stream()
                .map(ProfitAndLossLedgerItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ProfitAndLossSubGroupItem> subGroupItems = new ArrayList<>();
        BigDecimal subGroupsTotal = BigDecimal.ZERO;

        for (AccountGroup sub : directSubGroups) {
            Set<Long> descendantGroupIds = getDescendantGroupIds(sub.getId(), groupMap);
            descendantGroupIds.add(sub.getId());

            List<ProfitAndLossLedgerItem> subLedgers = allLedgers.stream()
                    .filter(l -> descendantGroupIds.contains(l.getAccountGroupId()))
                    .filter(l -> ledgerAmountMap.getOrDefault(l.getId(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0)
                    .map(l -> new ProfitAndLossLedgerItem()
                            .ledgerId(l.getId())
                            .ledgerName(l.getName())
                            .amount(ledgerAmountMap.get(l.getId()))
                            .crdr(ledgerSideMap.get(l.getId())))
                    .sorted(Comparator.comparing(ProfitAndLossLedgerItem::getLedgerName))
                    .toList();

            BigDecimal subTotal = subLedgers.stream()
                    .map(ProfitAndLossLedgerItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (subTotal.compareTo(BigDecimal.ZERO) > 0 || isDetail) {
                subGroupItems.add(new ProfitAndLossSubGroupItem()
                        .groupId(sub.getId())
                        .groupName(sub.getName())
                        .amount(subTotal)
                        .ledgers(isDetail ? subLedgers : List.of()));
                subGroupsTotal = subGroupsTotal.add(subTotal);
            }
        }

        BigDecimal grandTotal = directLedgersTotal.add(subGroupsTotal);

        return new ProfitAndLossGroupItem()
                .groupId(rootGroupId)
                .groupName(groupName)
                .amount(grandTotal)
                .subGroups(isDetail ? subGroupItems : List.of())
                .ledgers(isDetail ? directLedgerItems : List.of());
    }

    private Set<Long> getDescendantGroupIds(Long parentId, Map<Long, AccountGroup> groupMap) {
        Set<Long> descendants = new HashSet<>();
        for (AccountGroup g : groupMap.values()) {
            if (Objects.equals(g.getParentId(), parentId)) {
                descendants.add(g.getId());
                descendants.addAll(getDescendantGroupIds(g.getId(), groupMap));
            }
        }
        return descendants;
    }

    private LocalDate[] resolveDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null) {
            return new LocalDate[]{fromDate, toDate};
        }
        int fyStartYear = Year.now().getValue();
        try {
            Year financialYearStart = JwtUtil.extractJwtClaim().financialYearStart();
            if (financialYearStart != null) {
                fyStartYear = financialYearStart.getValue();
            }
        } catch (Exception ignored) {
        }
        LocalDate defaultStart = LocalDate.of(fyStartYear, 4, 1);
        LocalDate defaultEnd = LocalDate.of(fyStartYear + 1, 3, 31);
        return new LocalDate[]{
                fromDate != null ? fromDate : defaultStart,
                toDate != null ? toDate : defaultEnd
        };
    }
}
