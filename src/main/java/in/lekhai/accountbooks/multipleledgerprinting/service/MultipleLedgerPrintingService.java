package in.lekhai.accountbooks.multipleledgerprinting.service;

import in.lekhai.contract.model.MultipleLedgerItem;
import in.lekhai.contract.model.MultipleLedgerPrintingResponse;
import in.lekhai.core.account_master.domain.AccountGroup;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MultipleLedgerPrintingService {

    private final LedgerRepository ledgerRepository;
    private final AccountGroupRepository accountGroupRepository;

    public MultipleLedgerPrintingService(
            LedgerRepository ledgerRepository,
            AccountGroupRepository accountGroupRepository
    ) {
        this.ledgerRepository = ledgerRepository;
        this.accountGroupRepository = accountGroupRepository;
    }

    @ShopContextTransactional
    public MultipleLedgerPrintingResponse getMultipleLedgerPrinting() {
        List<AccountGroup> allGroups = accountGroupRepository.findAll();
        Map<Long, AccountGroup> groupMap = allGroups.stream()
                .collect(Collectors.toMap(AccountGroup::getId, g -> g, (existing, replacement) -> existing));

        List<Ledger> allLedgers = ledgerRepository.findAll();
        if (allLedgers == null || allLedgers.isEmpty()) {
            return new MultipleLedgerPrintingResponse()
                    .title("Index for Account Ledgers")
                    .totalLedgers(0)
                    .items(List.of());
        }

        // Filter active ledgers
        List<Ledger> activeLedgers = allLedgers.stream()
                .filter(l -> l.getActive() == null || Boolean.TRUE.equals(l.getActive()))
                .toList();

        // Group ledgers by accountGroupId
        Map<Long, List<Ledger>> ledgersByGroup = activeLedgers.stream()
                .collect(Collectors.groupingBy(l -> l.getAccountGroupId() != null ? l.getAccountGroupId() : -1L));

        // Sort unique groups that have ledgers
        List<AccountGroup> presentGroups = new ArrayList<>();
        AccountGroup unknownGroup = null;

        for (Map.Entry<Long, List<Ledger>> entry : ledgersByGroup.entrySet()) {
            Long groupId = entry.getKey();
            AccountGroup group = groupMap.get(groupId);
            if (group != null) {
                presentGroups.add(group);
            } else {
                if (unknownGroup == null) {
                    unknownGroup = new AccountGroup("Other Accounts", null, "OTHER", null, false);
                    unknownGroup.setId(-1L);
                }
            }
        }

        // Sort groups by:
        // 1. Root group nature priority (LIABILITY -> ASSET -> INCOME -> EXPENSE)
        // 2. Root group ID
        // 3. Subgroup ID
        presentGroups.sort((g1, g2) -> {
            AccountGroup root1 = findRootGroup(g1, groupMap);
            AccountGroup root2 = findRootGroup(g2, groupMap);

            int natureOrder1 = getNaturePriority(root1.getNature());
            int natureOrder2 = getNaturePriority(root2.getNature());
            if (natureOrder1 != natureOrder2) {
                return Integer.compare(natureOrder1, natureOrder2);
            }

            int rootIdCmp = Long.compare(
                    root1.getId() != null ? root1.getId() : 0L,
                    root2.getId() != null ? root2.getId() : 0L
            );
            if (rootIdCmp != 0) {
                return rootIdCmp;
            }

            return Long.compare(
                    g1.getId() != null ? g1.getId() : 0L,
                    g2.getId() != null ? g2.getId() : 0L
            );
        });

        if (unknownGroup != null) {
            presentGroups.add(unknownGroup);
        }

        List<MultipleLedgerItem> items = new ArrayList<>();
        int serialNumber = 0;

        for (AccountGroup group : presentGroups) {
            List<Ledger> groupLedgers = ledgersByGroup.get(group.getId());
            if (groupLedgers == null || groupLedgers.isEmpty()) {
                continue;
            }

            // Group header row
            MultipleLedgerItem headerRow = new MultipleLedgerItem()
                    .particulars(group.getName())
                    .isGroup(true)
                    .groupName(group.getName())
                    .serialNumber(null)
                    .ledgerId(null)
                    .pageNo(null);
            items.add(headerRow);

            // Sort ledgers alphabetically by name
            List<Ledger> sortedLedgers = groupLedgers.stream()
                    .sorted(Comparator.comparing(l -> l.getName() != null ? l.getName().toLowerCase() : ""))
                    .toList();

            for (Ledger ledger : sortedLedgers) {
                serialNumber++;
                MultipleLedgerItem ledgerRow = new MultipleLedgerItem()
                        .serialNumber(serialNumber)
                        .ledgerId(ledger.getId())
                        .particulars(ledger.getName())
                        .isGroup(false)
                        .groupName(group.getName())
                        .pageNo(null);
                items.add(ledgerRow);
            }
        }

        return new MultipleLedgerPrintingResponse()
                .title("Index for Account Ledgers")
                .totalLedgers(serialNumber)
                .items(items);
    }

    private AccountGroup findRootGroup(AccountGroup group, Map<Long, AccountGroup> groupMap) {
        AccountGroup current = group;
        Set<Long> visited = new HashSet<>();
        while (current != null && current.getParentId() != null && !visited.contains(current.getId())) {
            visited.add(current.getId());
            AccountGroup parent = groupMap.get(current.getParentId());
            if (parent == null) {
                break;
            }
            current = parent;
        }
        return current != null ? current : group;
    }

    private int getNaturePriority(String nature) {
        if (nature == null) return 5;
        return switch (nature.toUpperCase()) {
            case "LIABILITY" -> 1;
            case "ASSET" -> 2;
            case "INCOME" -> 3;
            case "EXPENSE" -> 4;
            default -> 5;
        };
    }
}
