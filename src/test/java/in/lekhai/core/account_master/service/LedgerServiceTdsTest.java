package in.lekhai.core.account_master.service;

import in.lekhai.contract.model.DeducteeType;
import in.lekhai.contract.model.LedgerRequest;
import in.lekhai.contract.model.LedgerResponse;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.domain.TdsSection;
import in.lekhai.core.account_master.repository.AddressRepository;
import in.lekhai.core.account_master.repository.GstDetailsRepository;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.account_master.repository.TdsSectionRepository;
import in.lekhai.error.controller.LekhaiClientException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTdsTest {

    @Mock
    private LedgerRepository ledgerRepository;
    @Mock
    private GstDetailsRepository gstDetailsRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private TdsSectionRepository tdsSectionRepository;

    @InjectMocks
    private LedgerService ledgerService;

    @Test
    void createLedger_savesTdsDetailsWithLowerDeductionCertificate() {
        when(tdsSectionRepository.findById(2L)).thenReturn(Optional.of(section194C()));
        stubSave();

        LedgerResponse response = ledgerService.createLedger(baseRequest()
                .tdsApplicable(true)
                .tdsSectionId(2L)
                .deducteeType(DeducteeType.INDIVIDUAL_HUF)
                .ldcCertificateNumber("LDC123")
                .ldcRate(0.5)
                .ldcValidFrom(LocalDate.of(2026, 4, 1))
                .ldcValidTo(LocalDate.of(2027, 3, 31)));

        ArgumentCaptor<Ledger> saved = ArgumentCaptor.forClass(Ledger.class);
        verify(ledgerRepository).save(saved.capture());
        assertTrue(saved.getValue().getTdsApplicable());
        assertEquals(2L, saved.getValue().getTdsSectionId());
        assertEquals(0, saved.getValue().getLdcRate().compareTo(new BigDecimal("0.5")));
        assertEquals("194C - Payment to contractors", response.getTdsSectionLabel());
    }

    @Test
    void createLedger_clearsTdsFieldsWhenNotApplicable() {
        stubSave();

        ledgerService.createLedger(baseRequest()
                .tdsApplicable(false)
                .tdsSectionId(2L)
                .ldcCertificateNumber("LDC123"));

        ArgumentCaptor<Ledger> saved = ArgumentCaptor.forClass(Ledger.class);
        verify(ledgerRepository).save(saved.capture());
        assertFalse(saved.getValue().getTdsApplicable());
        assertNull(saved.getValue().getTdsSectionId());
        assertNull(saved.getValue().getLdcCertificateNumber());
        verifyNoInteractions(tdsSectionRepository);
    }

    @Test
    void createLedger_rejectsTdsWithoutSection() {
        assertBadRequest(() -> ledgerService.createLedger(baseRequest()
                .tdsApplicable(true)
                .deducteeType(DeducteeType.OTHERS)));
    }

    @Test
    void createLedger_rejectsIncompleteLowerDeductionCertificate() {
        when(tdsSectionRepository.findById(2L)).thenReturn(Optional.of(section194C()));

        assertBadRequest(() -> ledgerService.createLedger(baseRequest()
                .tdsApplicable(true)
                .tdsSectionId(2L)
                .deducteeType(DeducteeType.OTHERS)
                .ldcCertificateNumber("LDC123")));
    }

    @Test
    void createLedger_rejectsCertificateRateNotBelowSectionRate() {
        when(tdsSectionRepository.findById(2L)).thenReturn(Optional.of(section194C()));

        // 194C for individuals is 1%
        assertBadRequest(() -> ledgerService.createLedger(baseRequest()
                .tdsApplicable(true)
                .tdsSectionId(2L)
                .deducteeType(DeducteeType.INDIVIDUAL_HUF)
                .ldcCertificateNumber("LDC123")
                .ldcRate(1.0)
                .ldcValidFrom(LocalDate.of(2026, 4, 1))
                .ldcValidTo(LocalDate.of(2027, 3, 31))));
    }

    @Test
    void createLedger_rejectsCertificateEndingBeforeItStarts() {
        when(tdsSectionRepository.findById(2L)).thenReturn(Optional.of(section194C()));

        assertBadRequest(() -> ledgerService.createLedger(baseRequest()
                .tdsApplicable(true)
                .tdsSectionId(2L)
                .deducteeType(DeducteeType.OTHERS)
                .ldcCertificateNumber("LDC123")
                .ldcRate(0.5)
                .ldcValidFrom(LocalDate.of(2027, 3, 31))
                .ldcValidTo(LocalDate.of(2026, 4, 1))));
    }

    /** Echo the ledger back with the audit timestamp the database would set. */
    private void stubSave() {
        when(ledgerRepository.save(any())).thenAnswer(inv -> {
            Ledger ledger = inv.getArgument(0);
            ledger.setCreatedAt(Instant.now());
            return ledger;
        });
    }

    private void assertBadRequest(Runnable call) {
        LekhaiClientException e = assertThrows(LekhaiClientException.class, call::run);
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        verify(ledgerRepository, never()).save(any());
    }

    private static LedgerRequest baseRequest() {
        return new LedgerRequest().name("Ramesh Transport").openingBalance(BigDecimal.ZERO);
    }

    private static TdsSection section194C() {
        TdsSection section = new TdsSection();
        ReflectionTestUtils.setField(section, "id", 2L);
        ReflectionTestUtils.setField(section, "code", "194C");
        ReflectionTestUtils.setField(section, "description", "Payment to contractors");
        ReflectionTestUtils.setField(section, "rateIndividualHuf", BigDecimal.ONE);
        ReflectionTestUtils.setField(section, "rateOthers", BigDecimal.valueOf(2));
        ReflectionTestUtils.setField(section, "rateNoPan", BigDecimal.valueOf(20));
        ReflectionTestUtils.setField(section, "isActive", Boolean.TRUE);
        return section;
    }
}
