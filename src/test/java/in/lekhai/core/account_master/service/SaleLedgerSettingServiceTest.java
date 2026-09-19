package in.lekhai.core.account_master.service;

import in.lekhai.contract.model.GstTaxability;
import in.lekhai.contract.model.SaleLedgerSettingRequest;
import in.lekhai.contract.model.SaleType;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.domain.SaleLedgerSetting;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.account_master.repository.SaleLedgerSettingRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleLedgerSettingServiceTest {

    @Mock
    private SaleLedgerSettingRepository saleLedgerSettingRepository;
    @Mock
    private LedgerRepository ledgerRepository;

    @InjectMocks
    private SaleLedgerSettingService service;

    @Test
    void create_outStateChargesFullRateAsIgst() {
        stubLedgersExist();
        when(saleLedgerSettingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.createSaleLedgerSetting(new SaleLedgerSettingRequest()
                .saleLedgerId(10L)
                .saleType(SaleType.OUT_STATE)
                .gstRate(18.0)
                .igstLedgerId(3L));

        ArgumentCaptor<SaleLedgerSetting> saved = ArgumentCaptor.forClass(SaleLedgerSetting.class);
        verify(saleLedgerSettingRepository).save(saved.capture());
        assertEquals(0, saved.getValue().getIgstPercentage().compareTo(BigDecimal.valueOf(18)));
        assertEquals(0, saved.getValue().getCgstPercentage().signum());
    }

    @Test
    void create_exemptSaleStoresZeroRate() {
        stubLedgersExist();
        when(saleLedgerSettingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.createSaleLedgerSetting(new SaleLedgerSettingRequest()
                .saleLedgerId(10L)
                .saleType(SaleType.IN_STATE)
                .taxability(GstTaxability.EXEMPT)
                .gstRate(18.0));

        ArgumentCaptor<SaleLedgerSetting> saved = ArgumentCaptor.forClass(SaleLedgerSetting.class);
        verify(saleLedgerSettingRepository).save(saved.capture());
        assertEquals("EXEMPT", saved.getValue().getTaxability());
        assertEquals(0, saved.getValue().getGstRate().signum());
        assertEquals(0, saved.getValue().getCgstPercentage().signum());
    }

    @Test
    void create_rejectsTaxLedgerOnExportUnderLut() {
        LekhaiClientException e = assertThrows(LekhaiClientException.class,
                () -> service.createSaleLedgerSetting(new SaleLedgerSettingRequest()
                        .saleLedgerId(10L)
                        .saleType(SaleType.EXPORT_UNDER_LUT)
                        .igstLedgerId(3L)));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        verify(saleLedgerSettingRepository, never()).save(any());
    }

    @Test
    void update_duplicateCheckExcludesTheRowBeingUpdated() {
        SaleLedgerSetting existing = new SaleLedgerSetting();
        existing.setId(7L);
        when(saleLedgerSettingRepository.findById(7L)).thenReturn(Optional.of(existing));
        stubLedgersExist();
        when(saleLedgerSettingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateSaleLedgerSetting(7L, new SaleLedgerSettingRequest()
                .saleLedgerId(10L)
                .saleType(SaleType.IN_STATE)
                .gstRate(5.0)
                .cgstLedgerId(1L)
                .sgstLedgerId(2L));

        verify(saleLedgerSettingRepository).existsActiveBySaleLedgerIdAndSaleType(10L, "IN_STATE", 7L);
    }

    @SuppressWarnings("unchecked")
    private void stubLedgersExist() {
        when(ledgerRepository.findAllById(anyCollection())).thenAnswer(inv ->
                ((Collection<Long>) inv.getArgument(0)).stream().map(SaleLedgerSettingServiceTest::ledger).toList());
    }

    private static Ledger ledger(Long id) {
        Ledger ledger = new Ledger("Ledger " + id, null, null, BigDecimal.ZERO, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null);
        ledger.setId(id);
        return ledger;
    }
}
