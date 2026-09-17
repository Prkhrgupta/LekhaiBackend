package in.lekhai.voucher.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import in.lekhai.contract.model.ContraVoucherRequest;
import in.lekhai.contract.model.JournalVoucherRequest;
import in.lekhai.contract.model.PaymentVoucherRequest;
import in.lekhai.contract.model.ReceiptVoucherRequest;
import in.lekhai.contract.model.VoucherEntry;
import in.lekhai.contract.model.VoucherResponse;
import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.voucher.dto.posting.PostingRequest;
import in.lekhai.voucher.entity.Voucher;
import in.lekhai.voucher.entity.VoucherType;
import in.lekhai.voucher.mapper.VoucherPostingMapper;
import in.lekhai.voucher.service.posting.VoucherPostingService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherIntakeServiceTest {

    @Mock
    private VoucherPostingService voucherPostingService;
    @Mock
    private VoucherPostingMapper voucherPostingMapper;
    @Mock
    private VoucherLedgerValidator ledgerValidator;

    @InjectMocks
    private VoucherIntakeService intakeService;

    @Test
    void processPayment_postsItemDebitsWithSingleCashCredit() {
        PaymentVoucherRequest request = new PaymentVoucherRequest()
                .voucherDate(LocalDate.of(2025, 6, 15))
                .paymentAccountId(10L)
                .narration("supplier payout")
                .addItemsItem(item(20L, "500"));
        Voucher saved = new Voucher(VoucherType.PAYMENT, 7L, request.getVoucherDate(), "supplier payout");
        when(voucherPostingService.post(any())).thenReturn(saved);
        when(voucherPostingMapper.mapToCommonVoucherResponse(saved))
                .thenReturn(new VoucherResponse().id(1L).voucherNumber("PY-7"));

        VoucherResponse actual = intakeService.processPayment(request);

        assertThat(actual.getVoucherNumber()).isEqualTo("PY-7");
        PostingRequest posting = capturePosting();
        assertThat(posting.voucherType()).isEqualTo(VoucherType.PAYMENT);
        assertThat(posting.entries()).hasSize(2);
        assertThat(posting.entries().get(0).debitAmount()).isEqualByComparingTo("500");
        assertThat(posting.entries().get(0).creditAmount()).isEqualByComparingTo("0");
        assertThat(posting.entries().get(1).ledgerId()).isEqualTo(10L);
        assertThat(posting.entries().get(1).creditAmount()).isEqualByComparingTo("500");
    }

    @Test
    void processPayment_rejectsNullRequest() {
        assertThatThrownBy(() -> intakeService.processPayment(null))
                .isInstanceOf(LekhaiClientException.class)
                .hasMessage("Payment voucher request must not be null");
    }

    @Test
    void processPayment_rejectsEmptyItems() {
        PaymentVoucherRequest request = new PaymentVoucherRequest()
                .voucherDate(LocalDate.of(2025, 6, 15))
                .paymentAccountId(10L);

        assertThatThrownBy(() -> intakeService.processPayment(request))
                .isInstanceOf(LekhaiClientException.class)
                .hasMessage("Payment voucher must contain at least one entry");
    }

    @Test
    void processReceipt_postsItemCreditsWithSingleCashDebit() {
        ReceiptVoucherRequest request = new ReceiptVoucherRequest()
                .voucherDate(LocalDate.of(2025, 6, 15))
                .receiptAccountId(11L)
                .narration("customer receipt")
                .addItemsItem(item(21L, "250"));
        Voucher saved = new Voucher(VoucherType.RECEIPT, 3L, request.getVoucherDate(), "customer receipt");
        when(voucherPostingService.post(any())).thenReturn(saved);
        when(voucherPostingMapper.mapToCommonVoucherResponse(saved))
                .thenReturn(new VoucherResponse().id(2L).voucherNumber("RC-3"));

        VoucherResponse actual = intakeService.processReceipt(request);

        assertThat(actual.getVoucherNumber()).isEqualTo("RC-3");
        PostingRequest posting = capturePosting();
        assertThat(posting.voucherType()).isEqualTo(VoucherType.RECEIPT);
        assertThat(posting.entries()).hasSize(2);
        assertThat(posting.entries().get(0).creditAmount()).isEqualByComparingTo("250");
        assertThat(posting.entries().get(1).ledgerId()).isEqualTo(11L);
        assertThat(posting.entries().get(1).debitAmount()).isEqualByComparingTo("250");
    }

    @Test
    void processContra_rejectsUnbalancedSides() {
        ContraVoucherRequest request = new ContraVoucherRequest()
                .voucherDate(LocalDate.of(2025, 6, 15))
                .narration("cash to bank")
                .addDebitEntriesItem(item(12L, "100"))
                .addCreditEntriesItem(item(13L, "200"));

        assertThatThrownBy(() -> intakeService.processContra(request))
                .isInstanceOf(LekhaiClientException.class)
                .hasMessage("Contra voucher total debit must equal total credit");
    }

    @Test
    void processJournal_postsTwoSidedEntries() {
        JournalVoucherRequest request = new JournalVoucherRequest()
                .voucherDate(LocalDate.of(2025, 6, 15))
                .narration("correction")
                .addDebitEntriesItem(item(30L, "300"))
                .addCreditEntriesItem(item(31L, "300"));
        Voucher saved = new Voucher(VoucherType.JOURNAL, 1L, request.getVoucherDate(), "correction");
        when(voucherPostingService.post(any())).thenReturn(saved);
        when(voucherPostingMapper.mapToCommonVoucherResponse(saved))
                .thenReturn(new VoucherResponse().id(3L).voucherNumber("JN-1"));

        VoucherResponse actual = intakeService.processJournal(request);

        assertThat(actual.getVoucherNumber()).isEqualTo("JN-1");
        PostingRequest posting = capturePosting();
        assertThat(posting.voucherType()).isEqualTo(VoucherType.JOURNAL);
        assertThat(posting.entries()).hasSize(2);
        assertThat(posting.entries().get(0).debitAmount()).isEqualByComparingTo("300");
        assertThat(posting.entries().get(1).creditAmount()).isEqualByComparingTo("300");
    }

    private PostingRequest capturePosting() {
        ArgumentCaptor<PostingRequest> posting = ArgumentCaptor.forClass(PostingRequest.class);
        verify(voucherPostingService).post(posting.capture());
        return posting.getValue();
    }

    private VoucherEntry item(Long ledgerId, String amount) {
        return new VoucherEntry().accountId(ledgerId).amount(new BigDecimal(amount));
    }
}
