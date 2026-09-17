package in.lekhai.voucher.service;

import in.lekhai.core.account_master.domain.AccountGroup;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.error.controller.LekhaiClientException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class VoucherLedgerValidator {
    private static final String BANK_ACCOUNTS_GROUP = "Bank Accounts";
    private static final String CASH_IN_HAND_GROUP = "Cash in Hand";

    private final LedgerRepository ledgerRepository;
    private final AccountGroupRepository accountGroupRepository;

    public VoucherLedgerValidator(
            LedgerRepository ledgerRepository,
            AccountGroupRepository accountGroupRepository
    ) {
        this.ledgerRepository = ledgerRepository;
        this.accountGroupRepository = accountGroupRepository;
    }

    public Ledger requireLedger(Long ledgerId) {
        if (ledgerId == null) {
            throw new LekhaiClientException("Ledger id must not be null", HttpStatus.BAD_REQUEST);
        }
        Optional<Ledger> ledger = ledgerRepository.findById(ledgerId);
        if (ledger.isEmpty()) {
            throw new LekhaiClientException(
                    String.format("Ledger [%d] not found", ledgerId),
                    HttpStatus.NOT_FOUND
            );
        }
        return ledger.get();
    }

    public void requireCashOrBankLedger(Long ledgerId, String role) {
        Ledger ledger = requireLedger(ledgerId);
        if (!isInCashOrBankGroups(ledger)) {
            throw new LekhaiClientException(
                    String.format("Ledger [%d] (%s) must belong to Bank Accounts or Cash in Hand group",
                            ledgerId, role),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    public void requireNonCashOrBankLedger(Long ledgerId, String role) {
        Ledger ledger = requireLedger(ledgerId);
        if (isInCashOrBankGroups(ledger)) {
            throw new LekhaiClientException(
                    String.format("Ledger [%d] (%s) must not belong to Bank Accounts or Cash in Hand group, use Contra instead",
                            ledgerId, role),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private boolean isInCashOrBankGroups(Ledger ledger) {
        return cashOrBankGroupIds().contains(ledger.getAccountGroupId());
    }

    private Set<Long> cashOrBankGroupIds() {
        List<Long> rootIds = new ArrayList<>();
        rootIds.add(groupIdByName(BANK_ACCOUNTS_GROUP));
        rootIds.add(groupIdByName(CASH_IN_HAND_GROUP));
        rootIds.removeIf(id -> id == null);
        if (rootIds.isEmpty()) {
            return new HashSet<>();
        }
        return accountGroupRepository.findHierarchyIds(rootIds);
    }

    private Long groupIdByName(String name) {
        Optional<AccountGroup> group = accountGroupRepository.findByNameIgnoreCase(name);
        return group.map(AccountGroup::getId).orElse(null);
    }
}
