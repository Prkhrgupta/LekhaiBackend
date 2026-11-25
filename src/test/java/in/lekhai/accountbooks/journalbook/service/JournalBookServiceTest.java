package in.lekhai.accountbooks.journalbook.service;

import in.lekhai.contract.model.JournalBookResponse;
import in.lekhai.contract.model.VoucherType;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.voucher.entity.Voucher;
import in.lekhai.voucher.entity.VoucherEntry;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JournalBookServiceTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private VoucherEntryRepository voucherEntryRepository;

    @Mock
    private LedgerRepository ledgerRepository;

    private JournalBookService journalBookService;

    @BeforeEach
    void setUp() {
        journalBookService = new JournalBookService(
                voucherRepository,
                voucherEntryRepository,
                ledgerRepository
        );
    }

    @Test
    void getJournalBook_returnsEmptyWhenNoVouchers() {
        LocalDate from = LocalDate.of(2026, 4, 1);
        LocalDate to = LocalDate.of(2027, 3, 31);

        when(voucherRepository.findByVoucherTypeAndDateBetween(eq("JOURNAL"), eq(from), eq(to)))
                .thenReturn(List.of());

        JournalBookResponse response = journalBookService.getJournalBook(from, to);

        assertThat(response).isNotNull();
        assertThat(response.getEntries()).isEmpty();
        assertThat(response.getTotalDebit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getTotalCredit()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getJournalBook_processesJournalEntriesAndNarration() {
        LocalDate date = LocalDate.of(2026, 5, 23);
        Voucher v = new Voucher(VoucherType.JOURNAL, 1L, date, "25000*4");
        v.setId(10L);

        when(voucherRepository.findByVoucherTypeAndDateBetween(eq("JOURNAL"), any(), any()))
                .thenReturn(List.of(v));

        VoucherEntry debitEntry = new VoucherEntry(10L, 100L, 1, new BigDecimal("25000"), BigDecimal.ZERO, null);
        VoucherEntry creditEntry = new VoucherEntry(10L, 200L, 2, BigDecimal.ZERO, new BigDecimal("25000"), null);

        when(voucherEntryRepository.findByVoucherIdIn(List.of(10L)))
                .thenReturn(List.of(debitEntry, creditEntry));

        Ledger hdfc = new Ledger("HDFC 0343", null, 30L, BigDecimal.ZERO, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
        hdfc.setId(100L);
        Ledger vivek = new Ledger("VIVEK VASTRALAYA", null, 9L, BigDecimal.ZERO, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
        vivek.setId(200L);

        when(ledgerRepository.findAllById(Set.of(100L, 200L)))
                .thenReturn(List.of(hdfc, vivek));

        JournalBookResponse response = journalBookService.getJournalBook(date, date);

        assertThat(response).isNotNull();
        assertThat(response.getTotalDebit()).isEqualByComparingTo(new BigDecimal("25000"));
        assertThat(response.getTotalCredit()).isEqualByComparingTo(new BigDecimal("25000"));
        assertThat(response.getEntries()).hasSize(3); // 1 debit + 1 credit + 1 narration

        // Entry 1: Debit (BY HDFC 0343)
        assertThat(response.getEntries().get(0).getDate()).isEqualTo("2026-05-23");
        assertThat(response.getEntries().get(0).getParticulars()).isEqualTo("BY HDFC 0343");
        assertThat(response.getEntries().get(0).getDebitAmount()).isEqualByComparingTo(new BigDecimal("25000"));
        assertThat(response.getEntries().get(0).getCreditAmount()).isNull();

        // Entry 2: Credit (TO VIVEK VASTRALAYA)
        assertThat(response.getEntries().get(1).getDate()).isNull();
        assertThat(response.getEntries().get(1).getParticulars()).isEqualTo("TO VIVEK VASTRALAYA");
        assertThat(response.getEntries().get(1).getDebitAmount()).isNull();
        assertThat(response.getEntries().get(1).getCreditAmount()).isEqualByComparingTo(new BigDecimal("25000"));

        // Entry 3: Narration (25000*4)
        assertThat(response.getEntries().get(2).getParticulars()).isEqualTo("  25000*4");
        assertThat(response.getEntries().get(2).getIsNarration()).isTrue();
    }
}
