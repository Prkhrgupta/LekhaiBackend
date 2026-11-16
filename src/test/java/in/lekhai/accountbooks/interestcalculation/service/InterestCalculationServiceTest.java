package in.lekhai.accountbooks.interestcalculation.service;

import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.LedgerRepository;
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
class InterestCalculationServiceTest {

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private VoucherEntryRepository voucherEntryRepository;

    @Mock
    private VoucherRepository voucherRepository;

    private InterestCalculationService service;

    private Ledger createLedger(Long id, String name, BigDecimal openingBalance, AccountEntryType openingType) {
        Ledger ledger = new Ledger(name, "Legal", 1L, openingBalance, openingType,
                BigDecimal.ZERO, null, null, null, null, null, null, null, null, null, null, null, null);
        ledger.setId(id);
        return ledger;
    }

    @BeforeEach
    void setUp() {
        service = new InterestCalculationService(ledgerRepository, voucherEntryRepository, voucherRepository);
    }

    @Test
    void getProductBasisInterest_calculatesTimelineCorrectly() {
        Long ledgerId = 10L;
        Ledger ledger = createLedger(ledgerId, "Customer A", BigDecimal.valueOf(10000), AccountEntryType.DR);

        when(ledgerRepository.findById(ledgerId)).thenReturn(Optional.of(ledger));
        when(voucherEntryRepository.sumNetBefore(eq(ledgerId), any(LocalDate.class))).thenReturn(BigDecimal.ZERO);

        LocalDate d1 = LocalDate.of(2026, 4, 1);
        LocalDate d2 = LocalDate.of(2026, 4, 11);
        LocalDate end = LocalDate.of(2026, 4, 21);

        LedgerEntryWithBalanceProjection entry1 = new LedgerEntryWithBalanceProjection(
                1L, 101L, ledgerId, 1, BigDecimal.valueOf(5000), BigDecimal.ZERO,
                d2, null, BigDecimal.valueOf(15000)
        );
        when(voucherEntryRepository.findEntriesByLedgerIdsBetweenDates(eq(List.of(ledgerId)), eq(d1), eq(end)))
                .thenReturn(List.of(entry1));

        ProductBasisInterestResponse response = service.getProductBasisInterest(
                ledgerId, d1, end, "YEARLY", BigDecimal.valueOf(12.0), BigDecimal.ZERO
        );

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(2);
        // First slice: 10 days at 10000 Dr
        // Interest = (10000 * 12 * 10) / 36500 = 32.88
        ProductBasisInterestItem item0 = response.getItems().get(0);
        assertThat(item0.getDate()).isEqualTo(d1);
        assertThat(item0.getBalance()).isEqualByComparingTo("10000");
        assertThat(item0.getDays()).isEqualTo(10);
        assertThat(item0.getInttReceivable().get()).isEqualByComparingTo("32.88");

        // Second slice: 10 days at 15000 Dr
        // Interest = (15000 * 12 * 10) / 36500 = 49.32
        ProductBasisInterestItem item1 = response.getItems().get(1);
        assertThat(item1.getDate()).isEqualTo(d2);
        assertThat(item1.getBalance()).isEqualByComparingTo("15000");
        assertThat(item1.getDays()).isEqualTo(10);
        assertThat(item1.getInttReceivable().get()).isEqualByComparingTo("49.32");
    }

    @Test
    void getDateWiseInterest_calculatesVouchersWithGraceDays() {
        Long ledgerId = 10L;
        Ledger ledger = createLedger(ledgerId, "Customer A", BigDecimal.ZERO, AccountEntryType.DR);

        when(ledgerRepository.findById(ledgerId)).thenReturn(Optional.of(ledger));
        when(voucherEntryRepository.sumNetBefore(eq(ledgerId), any(LocalDate.class))).thenReturn(BigDecimal.ZERO);

        LocalDate vDate = LocalDate.of(2026, 4, 1);
        LocalDate asOn = LocalDate.of(2026, 4, 21);

        LedgerEntryWithBalanceProjection entry1 = new LedgerEntryWithBalanceProjection(
                1L, 101L, ledgerId, 1, BigDecimal.valueOf(10000), BigDecimal.ZERO,
                vDate, null, BigDecimal.valueOf(10000)
        );
        when(voucherEntryRepository.findEntriesByLedgerIdsBetweenDates(eq(List.of(ledgerId)), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(entry1));
        when(voucherRepository.findAllById(any())).thenReturn(List.of());
        when(voucherEntryRepository.findByVoucherIdIn(any())).thenReturn(List.of());
        when(ledgerRepository.findAllById(any())).thenReturn(List.of(ledger));

        DateWiseInterestResponse response = service.getDateWiseInterest(
                ledgerId, vDate, asOn, vDate, asOn, 5, BigDecimal.valueOf(24.0), asOn, false
        );

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        DateWiseInterestItem item = response.getItems().get(0);
        // Due date = 2026-04-01 + 5 = 2026-04-06
        // Days = 2026-04-21 - 2026-04-06 = 15 days
        // Intt = 10000 * 24 * 15 / 36500 = 98.63
        assertThat(item.getDaysDr().get()).isEqualTo(15);
        assertThat(item.getInttDr().get()).isEqualByComparingTo("98.63");
    }

    @Test
    void calculateManualInterest_computesEntriesCorrectly() {
        Long ledgerId = 10L;
        Ledger ledger = createLedger(ledgerId, "Customer A", BigDecimal.ZERO, AccountEntryType.DR);

        when(ledgerRepository.findById(ledgerId)).thenReturn(Optional.of(ledger));

        ManualInterestCalculateRequest request = new ManualInterestCalculateRequest()
                .ledgerId(ledgerId)
                .graceDays(2)
                .yearlyRate(BigDecimal.valueOf(24.0))
                .asOnDate(LocalDate.of(2026, 5, 12))
                .entries(List.of(
                        new ManualInterestEntryRequest()
                                .drCr(AccountEntryType.DR)
                                .date(LocalDate.of(2026, 5, 1))
                                .particulars("Test Goods")
                                .amount(BigDecimal.valueOf(20000))
                ));

        ManualInterestResponse response = service.calculateManualInterest(request);
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        ManualInterestItem item = response.getItems().get(0);
        // Due date = 2026-05-01 + 2 = 2026-05-03
        // Days = 2026-05-12 - 2026-05-03 = 9 days
        // Intt = 20000 * 24 * 9 / 36500 = 118.36
        assertThat(item.getDays()).isEqualTo(9);
        assertThat(item.getInterest()).isEqualByComparingTo("118.36");
        assertThat(response.getTotalDebitInterest()).isEqualByComparingTo("118.36");
    }
}
