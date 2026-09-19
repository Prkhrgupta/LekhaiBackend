package in.lekhai.core.account_master.service;

import in.lekhai.contract.model.ItcEligibility;
import in.lekhai.contract.model.PurchaseLedgerSettingRequest;
import in.lekhai.contract.model.PurchaseLedgerSettingResponse;
import in.lekhai.contract.model.PurchaseType;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.domain.PurchaseLedgerSetting;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.account_master.repository.PurchaseLedgerSettingRepository;
import in.lekhai.error.controller.LekhaiClientException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseLedgerSettingServiceTest {

    @Mock
    private PurchaseLedgerSettingRepository purchaseLedgerSettingRepository;
    @Mock
    private LedgerRepository ledgerRepository;

    @InjectMocks
    private PurchaseLedgerSettingService service;

    @Test
    void create_derivesCgstAndSgstFromRate() {
        stubLedgersExist();
        when(purchaseLedgerSettingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PurchaseLedgerSettingResponse response = service.createPurchaseLedgerSetting(new PurchaseLedgerSettingRequest()
                .purchaseLedgerId(10L)
                .purchaseType(PurchaseType.IN_STATE)
                .gstRate(18.0)
                .cgstLedgerId(1L)
                .sgstLedgerId(2L));

        ArgumentCaptor<PurchaseLedgerSetting> saved = ArgumentCaptor.forClass(PurchaseLedgerSetting.class);
        verify(purchaseLedgerSettingRepository).save(saved.capture());
        assertEquals(0, saved.getValue().getCgstPercentage().compareTo(BigDecimal.valueOf(9)));
        assertEquals(0, saved.getValue().getSgstPercentage().compareTo(BigDecimal.valueOf(9)));
        assertEquals(0, saved.getValue().getIgstPercentage().signum());
        assertEquals("TAXABLE", saved.getValue().getTaxability());
        assertEquals("ELIGIBLE", saved.getValue().getItcEligibility());
        assertFalse(saved.getValue().getReverseCharge());

        assertEquals(9.0, response.getCgstPercentage());
        assertEquals(ItcEligibility.ELIGIBLE, response.getItcEligibility());
    }

    @Test
    void create_rejectsIgstLedgerOnInStatePurchase() {
        LekhaiClientException e = assertThrows(LekhaiClientException.class,
                () -> service.createPurchaseLedgerSetting(new PurchaseLedgerSettingRequest()
                        .purchaseLedgerId(10L)
                        .purchaseType(PurchaseType.IN_STATE)
                        .gstRate(18.0)
                        .cgstLedgerId(1L)
                        .sgstLedgerId(2L)
                        .igstLedgerId(3L)));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        verify(purchaseLedgerSettingRepository, never()).save(any());
    }

    @Test
    void create_rejectsReverseChargeWithoutPayableLedgers() {
        LekhaiClientException e = assertThrows(LekhaiClientException.class,
                () -> service.createPurchaseLedgerSetting(new PurchaseLedgerSettingRequest()
                        .purchaseLedgerId(10L)
                        .purchaseType(PurchaseType.OUT_STATE)
                        .gstRate(5.0)
                        .igstLedgerId(3L)
                        .reverseCharge(true)));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        verify(purchaseLedgerSettingRepository, never()).save(any());
    }

    @Test
    void create_rejectsSecondSettingForSameLedgerAndType() {
        stubLedgersExist();
        when(purchaseLedgerSettingRepository.existsActiveByPurchaseLedgerIdAndPurchaseType(10L, "IMPORT", 0L))
                .thenReturn(true);

        LekhaiClientException e = assertThrows(LekhaiClientException.class,
                () -> service.createPurchaseLedgerSetting(new PurchaseLedgerSettingRequest()
                        .purchaseLedgerId(10L)
                        .purchaseType(PurchaseType.IMPORT)
                        .gstRate(18.0)
                        .igstLedgerId(3L)));

        assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
        verify(purchaseLedgerSettingRepository, never()).save(any());
    }

    @Test
    void create_rejectsUnknownLedger() {
        when(ledgerRepository.findAllById(anyCollection())).thenReturn(List.of());

        LekhaiClientException e = assertThrows(LekhaiClientException.class,
                () -> service.createPurchaseLedgerSetting(new PurchaseLedgerSettingRequest()
                        .purchaseLedgerId(10L)
                        .purchaseType(PurchaseType.IMPORT)
                        .gstRate(18.0)
                        .igstLedgerId(3L)));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
    }

    /** Every requested ledger id resolves to a ledger. */
    @SuppressWarnings("unchecked")
    private void stubLedgersExist() {
        when(ledgerRepository.findAllById(anyCollection())).thenAnswer(inv ->
                ((Collection<Long>) inv.getArgument(0)).stream().map(PurchaseLedgerSettingServiceTest::ledger).toList());
    }

    private static Ledger ledger(Long id) {
        Ledger ledger = new Ledger("Ledger " + id, null, null, BigDecimal.ZERO, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null);
        ledger.setId(id);
        return ledger;
    }
}
