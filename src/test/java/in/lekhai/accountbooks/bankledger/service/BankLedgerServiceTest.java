package in.lekhai.accountbooks.bankledger.service;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankLedgerServiceTest {

    @Mock
    private AccountGroupRepository accountGroupRepository;

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private VoucherEntryRepository voucherEntryRepository;

    @Mock
    private VoucherRepository voucherRepository;

    @InjectMocks
    private BankLedgerService bankLedgerService;

    private AccountGroup currentAssets;
    private AccountGroup bankAccounts;
    private Ledger hdfcBank;
    private Ledger customerLedger;
    private Ledger supplierLedger;

    @BeforeEach
    void setUp() {
        currentAssets = new AccountGroup("Current Assets", null, "ASSET", in.lekhai.common.AccountEntryType.DR, true);
        currentAssets.setId(6L);

        bankAccounts = new AccountGroup("Bank Accounts", 6L, "ASSET", in.lekhai.common.AccountEntryType.DR, false);
        bankAccounts.setId(30L);

        hdfcBank = createLedger(19L, "HDFC 0343", 30L, BigDecimal.valueOf(50000), AccountEntryType.DR);
        customerLedger = createLedger(22L, "AJAY AUTO PARTS", 3L, BigDecimal.ZERO, AccountEntryType.DR);
        supplierLedger = createLedger(13L, "BHARAT TEXTILES", 4L, BigDecimal.ZERO, AccountEntryType.CR);

        lenient().when(ledgerRepository.findById(19L)).thenReturn(Optional.of(hdfcBank));
        lenient().when(accountGroupRepository.findById(30L)).thenReturn(Optional.of(bankAccounts));
        lenient().when(accountGroupRepository.findById(6L)).thenReturn(Optional.of(currentAssets));
        lenient().when(ledgerRepository.findAllById(any())).thenReturn(List.of(hdfcBank, customerLedger, supplierLedger));
    }

    private Ledger createLedger(Long id, String name, Long groupId, BigDecimal opening, AccountEntryType type) {
        Ledger ledger = new Ledger(name, null, groupId, opening, type,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
        ledger.setId(id);
        ledger.setActive(true);
        return ledger;
    }

    @Test
    void testGetBankLedger_OpeningOnly() {
        LocalDate fromDate = LocalDate.of(2026, 4, 1);
        LocalDate toDate = LocalDate.of(2026, 4, 30);

        when(voucherEntryRepository.sumNetBefore(19L, fromDate)).thenReturn(BigDecimal.ZERO);
        when(voucherEntryRepository.findEntriesByLedgerIdsBetweenDates(anyList(), eq(fromDate), eq(toDate)))
                .thenReturn(List.of());

        BankLedgerResponse response = bankLedgerService.getBankLedger(19L, fromDate, toDate);

        assertNotNull(response);
        assertEquals("HDFC 0343", response.getBankName());
        assertEquals("Bank Accounts Under : Current Assets", response.getAccountGroup());
        assertEquals(new BigDecimal("50000"), response.getOpeningBalance());
        assertEquals(AccountEntryType.DR, response.getOpeningBalanceType());
        assertEquals(BigDecimal.ZERO, response.getTotalDebit());
        assertEquals(BigDecimal.ZERO, response.getTotalCredit());
        assertEquals(new BigDecimal("50000"), response.getClosingBalance());
        assertEquals(AccountEntryType.DR, response.getClosingBalanceType());

        assertEquals(1, response.getEntries().size());
        assertEquals("OPENING", response.getEntries().get(0).getEntryType());
        assertEquals("To Opening Balance", response.getEntries().get(0).getParticulars());
    }

    @Test
    void testGetBankLedger_WithReceiptAndPayment() {
        LocalDate fromDate = LocalDate.of(2026, 4, 1);
        LocalDate toDate = LocalDate.of(2026, 4, 30);

        when(voucherEntryRepository.sumNetBefore(19L, fromDate)).thenReturn(BigDecimal.ZERO);

        LocalDate d1 = LocalDate.of(2026, 4, 5);
        LocalDate d2 = LocalDate.of(2026, 4, 10);

        LedgerEntryWithBalanceProjection e1 = new LedgerEntryWithBalanceProjection(
                1L, 101L, 19L, 1, BigDecimal.valueOf(40000), BigDecimal.ZERO, d1, BigDecimal.ZERO);
        LedgerEntryWithBalanceProjection e2 = new LedgerEntryWithBalanceProjection(
                2L, 102L, 19L, 2, BigDecimal.ZERO, BigDecimal.valueOf(15000), d2, BigDecimal.ZERO);

        when(voucherEntryRepository.findEntriesByLedgerIdsBetweenDates(anyList(), eq(fromDate), eq(toDate)))
                .thenReturn(List.of(e1, e2));

        Voucher v1 = new Voucher(VoucherType.RECEIPT, 1L, d1, "Payment received via NEFT");
        v1.setId(101L);
        Voucher v2 = new Voucher(VoucherType.PAYMENT, 2L, d2, "Vendor payment via UPI transfer");
        v2.setId(102L);
        when(voucherRepository.findAllById(anyList())).thenReturn(List.of(v1, v2));

        VoucherEntry ve1 = new VoucherEntry(101L, 22L, 2, BigDecimal.ZERO, BigDecimal.valueOf(40000), "");
        VoucherEntry ve2 = new VoucherEntry(102L, 13L, 1, BigDecimal.valueOf(15000), BigDecimal.ZERO, "");
        when(voucherEntryRepository.findByVoucherIdIn(anyList())).thenReturn(List.of(ve1, ve2));

        BankLedgerResponse response = bankLedgerService.getBankLedger(19L, fromDate, toDate);

        assertNotNull(response);
        assertEquals(new BigDecimal("50000"), response.getOpeningBalance());
        assertEquals(new BigDecimal("40000"), response.getTotalDebit());
        assertEquals(new BigDecimal("15000"), response.getTotalCredit());
        assertEquals(new BigDecimal("75000"), response.getClosingBalance());
        assertEquals(AccountEntryType.DR, response.getClosingBalanceType());

        // Entries: Opening + 2 transactions = 3
        assertEquals(3, response.getEntries().size());

        BankLedgerEntryItem receiptItem = response.getEntries().get(1);
        assertEquals("To AJAY AUTO PARTS", receiptItem.getParticulars());
        assertEquals("Rcpt", receiptItem.getVchType());
        assertEquals("NEFT", receiptItem.getInstrumentMode());
        assertEquals(new BigDecimal("40000"), receiptItem.getDebit());
        assertNull(receiptItem.getCredit());
        assertEquals(new BigDecimal("90000"), receiptItem.getBalance());

        BankLedgerEntryItem paymentItem = response.getEntries().get(2);
        assertEquals("By BHARAT TEXTILES", paymentItem.getParticulars());
        assertEquals("Pymt", paymentItem.getVchType());
        assertEquals("UPI", paymentItem.getInstrumentMode());
        assertNull(paymentItem.getDebit());
        assertEquals(new BigDecimal("15000"), paymentItem.getCredit());
        assertEquals(new BigDecimal("75000"), paymentItem.getBalance());
    }
}
