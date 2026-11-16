package in.lekhai.accountbooks.bankreconciliation.service;

import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.domain.AccountGroup;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.voucher.entity.Voucher;
import in.lekhai.voucher.entity.VoucherEntry;
import in.lekhai.voucher.repository.LedgerEntryWithBalanceProjection;
import in.lekhai.voucher.repository.VoucherEntryRepository;
import in.lekhai.voucher.repository.VoucherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankReconciliationServiceTest {

    @Mock
    private AccountGroupRepository accountGroupRepository;

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private VoucherEntryRepository voucherEntryRepository;

    @Mock
    private VoucherRepository voucherRepository;

    private BankReconciliationService service;

    @BeforeEach
    void setUp() {
        service = new BankReconciliationService(
                accountGroupRepository,
                ledgerRepository,
                voucherEntryRepository,
                voucherRepository
        );
    }

    @Test
    void getBankReconciliation_modesFilterProperlyAndComputeBRS() {
        Long bankLedgerId = 19L;
        LocalDate fromDate = LocalDate.of(2026, 4, 1);
        LocalDate toDate = LocalDate.of(2027, 3, 31);

        Ledger bankLedger = new Ledger("HDFC 0343", "Legal", 30L, BigDecimal.ZERO, AccountEntryType.DR,
                BigDecimal.ZERO, null, null, null, null, null, null, null, null, null, null, null, null);
        bankLedger.setId(bankLedgerId);

        AccountGroup bankGroup = new AccountGroup("Bank Accounts", 6L, "ASSET", null, false);
        bankGroup.setId(30L);
        AccountGroup currentAssets = new AccountGroup("Current Assets", null, "ASSET", null, true);
        currentAssets.setId(6L);

        when(ledgerRepository.findById(bankLedgerId)).thenReturn(Optional.of(bankLedger));
        when(accountGroupRepository.findById(30L)).thenReturn(Optional.of(bankGroup));
        when(accountGroupRepository.findById(6L)).thenReturn(Optional.of(currentAssets));
        when(voucherEntryRepository.sumNetBefore(bankLedgerId, fromDate)).thenReturn(BigDecimal.valueOf(100000));

        // Entry 1: Deposit of 40,000, CLEARED on 2026-04-05
        LedgerEntryWithBalanceProjection entry1 = new LedgerEntryWithBalanceProjection(
                1L, 101L, bankLedgerId, 1, BigDecimal.valueOf(40000), BigDecimal.ZERO,
                LocalDate.of(2026, 4, 2), LocalDate.of(2026, 4, 5), BigDecimal.ZERO
        );

        // Entry 2: Withdrawal of 25,000, UNCLEARED (null)
        LedgerEntryWithBalanceProjection entry2 = new LedgerEntryWithBalanceProjection(
                2L, 102L, bankLedgerId, 2, BigDecimal.ZERO, BigDecimal.valueOf(25000),
                LocalDate.of(2026, 4, 10), null, BigDecimal.ZERO
        );

        when(voucherEntryRepository.findEntriesByLedgerIdsBetweenDates(List.of(bankLedgerId), fromDate, toDate))
                .thenReturn(List.of(entry1, entry2));

        Voucher v1 = new Voucher(VoucherType.RECEIPT, 1L, LocalDate.of(2026, 4, 2), "NEFT deposit");
        v1.setId(101L);

        Voucher v2 = new Voucher(VoucherType.PAYMENT, 1L, LocalDate.of(2026, 4, 10), "UPI payment");
        v2.setId(102L);

        when(voucherRepository.findAllById(any())).thenReturn(List.of(v1, v2));

        VoucherEntry sib1 = new VoucherEntry(101L, 22L, 2, BigDecimal.ZERO, BigDecimal.valueOf(40000), "");
        VoucherEntry sib2 = new VoucherEntry(102L, 13L, 1, BigDecimal.valueOf(25000), BigDecimal.ZERO, "");
        when(voucherEntryRepository.findByVoucherIdIn(any())).thenReturn(List.of(sib1, sib2));

        Ledger customer = new Ledger("AJAY AUTO PARTS", "L", 3L, BigDecimal.ZERO, AccountEntryType.DR, BigDecimal.ZERO, null, null, null, null, null, null, null, null, null, null, null, null);
        customer.setId(22L);
        Ledger vendor = new Ledger("BHARAT TEXTILES", "L", 4L, BigDecimal.ZERO, AccountEntryType.CR, BigDecimal.ZERO, null, null, null, null, null, null, null, null, null, null, null, null);
        vendor.setId(13L);
        when(ledgerRepository.findAllById(any())).thenReturn(List.of(bankLedger, customer, vendor));

        // Test ALL mode
        BankReconciliationResponse allResponse = service.getBankReconciliation(
                bankLedgerId, fromDate, toDate, BankReconciliationMode.ALL
        );
        assertThat(allResponse).isNotNull();
        assertThat(allResponse.getEntries()).hasSize(2);
        assertThat(allResponse.getClosingBalance()).isEqualByComparingTo(BigDecimal.valueOf(115000));
        assertThat(allResponse.getClosingBalanceType()).isEqualTo(AccountEntryType.DR);

        // BRS Statement Summary
        BankReconciliationStatementSummary summary = allResponse.getStatementSummary();
        assertThat(summary.getBalanceAsPerCompany()).isEqualByComparingTo(BigDecimal.valueOf(115000));
        assertThat(summary.getUnclearedDeposits()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getUnclearedWithdrawals()).isEqualByComparingTo(BigDecimal.valueOf(25000));
        // Bank balance = Company (115000) - uncleared deposits (0) + uncleared withdrawals (25000) = 140000 DR
        assertThat(summary.getBalanceAsPerBank()).isEqualByComparingTo(BigDecimal.valueOf(140000));

        // Test UNCLEARED mode
        BankReconciliationResponse unclearedResponse = service.getBankReconciliation(
                bankLedgerId, fromDate, toDate, BankReconciliationMode.UNCLEARED
        );
        assertThat(unclearedResponse.getEntries()).hasSize(1);
        assertThat(unclearedResponse.getEntries().get(0).getVoucherEntryId()).isEqualTo(2L);
        assertThat(unclearedResponse.getEntries().get(0).getIsCleared()).isFalse();

        // Test CLEARED mode
        BankReconciliationResponse clearedResponse = service.getBankReconciliation(
                bankLedgerId, fromDate, toDate, BankReconciliationMode.CLEARED
        );
        assertThat(clearedResponse.getEntries()).hasSize(1);
        assertThat(clearedResponse.getEntries().get(0).getVoucherEntryId()).isEqualTo(1L);
        assertThat(clearedResponse.getEntries().get(0).getIsCleared()).isTrue();
    }

    @Test
    void updateClearDate_updatesRepository() {
        when(voucherEntryRepository.updateClearDate(5L, LocalDate.of(2026, 4, 15))).thenReturn(1);

        UpdateClearDateResponse response = service.updateClearDate(5L, LocalDate.of(2026, 4, 15));

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getVoucherEntryId()).isEqualTo(5L);
        assertThat(response.getClearDate().get()).isEqualTo(LocalDate.of(2026, 4, 15));
    }
}
