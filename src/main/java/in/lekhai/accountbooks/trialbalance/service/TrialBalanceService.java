package in.lekhai.accountbooks.trialbalance.service;

import in.lekhai.accountbooks.balancesheet.repository.LedgerNetTotalProjection;
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
public class TrialBalanceService {

    private final AccountGroupRepository accountGroupRepository;
    private final LedgerRepository ledgerRepository;
    private final VoucherEntryRepository voucherEntryRepository;

    public TrialBalanceService(
            AccountGroupRepository accountGroupRepository,
            LedgerRepository ledgerRepository,
            VoucherEntryRepository voucherEntryRepository
    ) {
        this.accountGroupRepository = accountGroupRepository;
        this.ledgerRepository = ledgerRepository;
        this.voucherEntryRepository = voucherEntryRepository;
    }

    @ShopContextTransactional
    public TrialBalanceResponse getTrialBalance(
            LocalDate fromDate,
            LocalDate toDate,
            String format,
            boolean withNilBalance
    ) {
        LocalDate[] range = resolveDateRange(fromDate, toDate);
        LocalDate start = range[0];
        LocalDate end = range[1];
        boolean isDetail = "DETAIL".equalsIgnoreCase(format);

        // 1. Load groups & map
        List<AccountGroup> allGroups = accountGroupRepository.findAll();
        Map<Long, AccountGroup> groupMap = allGroups.stream()
                .collect(Collectors.toMap(AccountGroup::getId, g -> g));

        // 2. Load ledgers
        List<Ledger> allLedgers = ledgerRepository.findAll();

        // 3. Query voucher movements before fromDate and between fromDate..toDate
        List<LedgerNetTotalProjection> beforeMovements = voucherEntryRepository.sumNetByLedgerBeforeDate(start);
        Map<Long, LedgerNetTotalProjection> beforeMap = beforeMovements.stream()
                .collect(Collectors.toMap(LedgerNetTotalProjection::ledgerId, p -> p));

        List<LedgerNetTotalProjection> betweenMovements = voucherEntryRepository.sumNetByLedgerBetweenDates(start, end);
        Map<Long, LedgerNetTotalProjection> betweenMap = betweenMovements.stream()
                .collect(Collectors.toMap(LedgerNetTotalProjection::ledgerId, p -> p));

        // 4. Calculate for each ledger: opening Dr/Cr, transactions Dr/Cr, closing Dr/Cr
        Map<Long, TrialBalanceLedgerItem> ledgerItemMap = new HashMap<>();

        for (Ledger ledger : allLedgers) {
            BigDecimal masterOpening = ledger.getOpeningBalance() != null ? ledger.getOpeningBalance() : BigDecimal.ZERO;
            AccountEntryType opType = ledger.getOpeningBalanceType();
            // In signed net balance: DR is positive (+), CR is negative (-)
            BigDecimal initialNet = AccountEntryType.CR.equals(opType) ? masterOpening.negate() : masterOpening;

            LedgerNetTotalProjection b = beforeMap.get(ledger.getId());
            BigDecimal drBefore = (b != null && b.totalDebit() != null) ? b.totalDebit() : BigDecimal.ZERO;
            BigDecimal crBefore = (b != null && b.totalCredit() != null) ? b.totalCredit() : BigDecimal.ZERO;
            BigDecimal netBefore = drBefore.subtract(crBefore);

            BigDecimal effectiveOpeningNet = initialNet.add(netBefore);
            BigDecimal openingDr = BigDecimal.ZERO;
            BigDecimal openingCr = BigDecimal.ZERO;
            if (effectiveOpeningNet.compareTo(BigDecimal.ZERO) > 0) {
                openingDr = effectiveOpeningNet;
            } else if (effectiveOpeningNet.compareTo(BigDecimal.ZERO) < 0) {
                openingCr = effectiveOpeningNet.abs();
            }

            LedgerNetTotalProjection t = betweenMap.get(ledger.getId());
            BigDecimal transactionsDr = (t != null && t.totalDebit() != null) ? t.totalDebit() : BigDecimal.ZERO;
            BigDecimal transactionsCr = (t != null && t.totalCredit() != null) ? t.totalCredit() : BigDecimal.ZERO;

            BigDecimal closingNet = effectiveOpeningNet.add(transactionsDr).subtract(transactionsCr);
            BigDecimal closingDr = BigDecimal.ZERO;
            BigDecimal closingCr = BigDecimal.ZERO;
            if (closingNet.compareTo(BigDecimal.ZERO) > 0) {
                closingDr = closingNet;
            } else if (closingNet.compareTo(BigDecimal.ZERO) < 0) {
                closingCr = closingNet.abs();
            }

            boolean isNil = openingDr.compareTo(BigDecimal.ZERO) == 0
                    && openingCr.compareTo(BigDecimal.ZERO) == 0
                    && transactionsDr.compareTo(BigDecimal.ZERO) == 0
                    && transactionsCr.compareTo(BigDecimal.ZERO) == 0
                    && closingDr.compareTo(BigDecimal.ZERO) == 0
                    && closingCr.compareTo(BigDecimal.ZERO) == 0;

            if (withNilBalance || !isNil) {
                ledgerItemMap.put(ledger.getId(), new TrialBalanceLedgerItem()
                        .ledgerId(ledger.getId())
                        .ledgerName(ledger.getName())
                        .openingDr(openingDr)
                        .openingCr(openingCr)
                        .transactionsDr(transactionsDr)
                        .transactionsCr(transactionsCr)
                        .closingDr(closingDr)
                        .closingCr(closingCr));
            }
        }

        // 5. Build group hierarchy trees
        List<AccountGroup> rootGroups = allGroups.stream()
                .filter(g -> g.getParentId() == null || Boolean.TRUE.equals(g.getPrimary()))
                .sorted(Comparator.comparing(AccountGroup::getId))
                .toList();

        List<TrialBalanceGroupItem> resultGroups = new ArrayList<>();

        BigDecimal totalOpeningDr = BigDecimal.ZERO;
        BigDecimal totalOpeningCr = BigDecimal.ZERO;
        BigDecimal totalTransactionsDr = BigDecimal.ZERO;
        BigDecimal totalTransactionsCr = BigDecimal.ZERO;
        BigDecimal totalClosingDr = BigDecimal.ZERO;
        BigDecimal totalClosingCr = BigDecimal.ZERO;

        for (AccountGroup root : rootGroups) {
            TrialBalanceGroupItem groupItem = buildGroupItem(root, groupMap, allLedgers, ledgerItemMap, isDetail, withNilBalance);
            if (groupItem != null) {
                resultGroups.add(groupItem);
                totalOpeningDr = totalOpeningDr.add(groupItem.getOpeningDr());
                totalOpeningCr = totalOpeningCr.add(groupItem.getOpeningCr());
                totalTransactionsDr = totalTransactionsDr.add(groupItem.getTransactionsDr());
                totalTransactionsCr = totalTransactionsCr.add(groupItem.getTransactionsCr());
                totalClosingDr = totalClosingDr.add(groupItem.getClosingDr());
                totalClosingCr = totalClosingCr.add(groupItem.getClosingCr());
            }
        }

        // Calculate differences
        BigDecimal openingDifference = totalOpeningDr.subtract(totalOpeningCr).abs();
        String openingDiffType = resolveDiffType(totalOpeningDr, totalOpeningCr);

        BigDecimal closingDifference = totalClosingDr.subtract(totalClosingCr).abs();
        String closingDiffType = resolveDiffType(totalClosingDr, totalClosingCr);

        return new TrialBalanceResponse()
                .fromDate(start.toString())
                .toDate(end.toString())
                .format(format != null ? format.toUpperCase() : "SUMMARY")
                .withNilBalance(withNilBalance)
                .groups(resultGroups)
                .totalOpeningDr(totalOpeningDr)
                .totalOpeningCr(totalOpeningCr)
                .openingDifference(openingDifference)
                .openingDifferenceType(openingDiffType)
                .totalTransactionsDr(totalTransactionsDr)
                .totalTransactionsCr(totalTransactionsCr)
                .totalClosingDr(totalClosingDr)
                .totalClosingCr(totalClosingCr)
                .closingDifference(closingDifference)
                .closingDifferenceType(closingDiffType);
    }

    private String resolveDiffType(BigDecimal dr, BigDecimal cr) {
        int cmp = dr.compareTo(cr);
        if (cmp > 0) return "EXCESS_DR";
        if (cmp < 0) return "EXCESS_CR";
        return "BALANCED";
    }

    private TrialBalanceGroupItem buildGroupItem(
            AccountGroup root,
            Map<Long, AccountGroup> groupMap,
            List<Ledger> allLedgers,
            Map<Long, TrialBalanceLedgerItem> ledgerItemMap,
            boolean isDetail,
            boolean withNilBalance
    ) {
        // Direct ledgers under root group
        List<TrialBalanceLedgerItem> directLedgers = allLedgers.stream()
                .filter(l -> Objects.equals(l.getAccountGroupId(), root.getId()))
                .map(l -> ledgerItemMap.get(l.getId()))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(TrialBalanceLedgerItem::getLedgerName))
                .toList();

        // Direct sub-groups
        List<AccountGroup> directSubGroups = groupMap.values().stream()
                .filter(g -> Objects.equals(g.getParentId(), root.getId()))
                .sorted(Comparator.comparing(AccountGroup::getName))
                .toList();

        List<TrialBalanceSubGroupItem> subGroupItems = new ArrayList<>();

        BigDecimal grpOpeningDr = sumLedgerField(directLedgers, TrialBalanceLedgerItem::getOpeningDr);
        BigDecimal grpOpeningCr = sumLedgerField(directLedgers, TrialBalanceLedgerItem::getOpeningCr);
        BigDecimal grpTransDr = sumLedgerField(directLedgers, TrialBalanceLedgerItem::getTransactionsDr);
        BigDecimal grpTransCr = sumLedgerField(directLedgers, TrialBalanceLedgerItem::getTransactionsCr);
        BigDecimal grpClosingDr = sumLedgerField(directLedgers, TrialBalanceLedgerItem::getClosingDr);
        BigDecimal grpClosingCr = sumLedgerField(directLedgers, TrialBalanceLedgerItem::getClosingCr);

        for (AccountGroup sub : directSubGroups) {
            Set<Long> descendantGroupIds = getDescendantGroupIds(sub.getId(), groupMap);
            descendantGroupIds.add(sub.getId());

            List<TrialBalanceLedgerItem> subLedgers = allLedgers.stream()
                    .filter(l -> descendantGroupIds.contains(l.getAccountGroupId()))
                    .map(l -> ledgerItemMap.get(l.getId()))
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(TrialBalanceLedgerItem::getLedgerName))
                    .toList();

            BigDecimal subOpDr = sumLedgerField(subLedgers, TrialBalanceLedgerItem::getOpeningDr);
            BigDecimal subOpCr = sumLedgerField(subLedgers, TrialBalanceLedgerItem::getOpeningCr);
            BigDecimal subTrDr = sumLedgerField(subLedgers, TrialBalanceLedgerItem::getTransactionsDr);
            BigDecimal subTrCr = sumLedgerField(subLedgers, TrialBalanceLedgerItem::getTransactionsCr);
            BigDecimal subClDr = sumLedgerField(subLedgers, TrialBalanceLedgerItem::getClosingDr);
            BigDecimal subClCr = sumLedgerField(subLedgers, TrialBalanceLedgerItem::getClosingCr);

            boolean isSubNil = subOpDr.compareTo(BigDecimal.ZERO) == 0
                    && subOpCr.compareTo(BigDecimal.ZERO) == 0
                    && subTrDr.compareTo(BigDecimal.ZERO) == 0
                    && subTrCr.compareTo(BigDecimal.ZERO) == 0
                    && subClDr.compareTo(BigDecimal.ZERO) == 0
                    && subClCr.compareTo(BigDecimal.ZERO) == 0;

            if (withNilBalance || !isSubNil) {
                subGroupItems.add(new TrialBalanceSubGroupItem()
                        .groupId(sub.getId())
                        .groupName(sub.getName())
                        .openingDr(subOpDr)
                        .openingCr(subOpCr)
                        .transactionsDr(subTrDr)
                        .transactionsCr(subTrCr)
                        .closingDr(subClDr)
                        .closingCr(subClCr)
                        .ledgers(isDetail ? subLedgers : List.of()));

                grpOpeningDr = grpOpeningDr.add(subOpDr);
                grpOpeningCr = grpOpeningCr.add(subOpCr);
                grpTransDr = grpTransDr.add(subTrDr);
                grpTransCr = grpTransCr.add(subTrCr);
                grpClosingDr = grpClosingDr.add(subClDr);
                grpClosingCr = grpClosingCr.add(subClCr);
            }
        }

        boolean isGroupNil = grpOpeningDr.compareTo(BigDecimal.ZERO) == 0
                && grpOpeningCr.compareTo(BigDecimal.ZERO) == 0
                && grpTransDr.compareTo(BigDecimal.ZERO) == 0
                && grpTransCr.compareTo(BigDecimal.ZERO) == 0
                && grpClosingDr.compareTo(BigDecimal.ZERO) == 0
                && grpClosingCr.compareTo(BigDecimal.ZERO) == 0;

        if (!withNilBalance && isGroupNil) {
            return null;
        }

        return new TrialBalanceGroupItem()
                .groupId(root.getId())
                .groupName(root.getName())
                .openingDr(grpOpeningDr)
                .openingCr(grpOpeningCr)
                .transactionsDr(grpTransDr)
                .transactionsCr(grpTransCr)
                .closingDr(grpClosingDr)
                .closingCr(grpClosingCr)
                .subGroups(isDetail ? subGroupItems : List.of())
                .ledgers(isDetail ? directLedgers : List.of());
    }

    private BigDecimal sumLedgerField(List<TrialBalanceLedgerItem> items, java.util.function.Function<TrialBalanceLedgerItem, BigDecimal> getter) {
        return items.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
