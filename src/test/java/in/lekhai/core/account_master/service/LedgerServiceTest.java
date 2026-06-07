package in.lekhai.core.account_master.service;

import in.lekhai.contract.model.LedgerSummaryItem;
import in.lekhai.contract.model.LedgerSummaryResponse;
import in.lekhai.core.account_master.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock private LedgerRepository ledgerRepository;
    @Mock private GstDetailsRepository gstDetailsRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private AreaRepository areaRepository;
    @Mock private BrokerRepository brokerRepository;
    @Mock private TransportRepository transportRepository;
    @Mock private AccountGroupRepository accountGroupRepository;
    @Mock private StateRepository stateRepository;
    @Mock private LedgerSummaryRepository ledgerSummaryRepository;

    @InjectMocks
    private LedgerService ledgerService;

    @Test
    void testListLedgerSummaries_normalPagination() {
        when(ledgerSummaryRepository.countSummaries("test", "name")).thenReturn(100L);
        
        List<LedgerSummaryItem> mockItems = List.of(new LedgerSummaryItem().id(1L).name("Ledger 1"));
        when(ledgerSummaryRepository.findSummaries(
                "test", "name", "name", "asc", 0, 10
        )).thenReturn(mockItems);

        LedgerSummaryResponse response = ledgerService.listLedgerSummaries(
                1, 10, "test", "name", "name", "asc"
        );

        assertNotNull(response);
        assertEquals(mockItems, response.getData());
        assertEquals(1, response.getPagination().getPage());
        assertEquals(10, response.getPagination().getPageSize());
        assertEquals(100L, response.getPagination().getTotalItems());
        assertEquals(10, response.getPagination().getTotalPages());
        assertTrue(response.getPagination().getHasNext());
        assertFalse(response.getPagination().getHasPrevious());
    }

    @Test
    void testListLedgerSummaries_pageOutOfBounds() {
        when(ledgerSummaryRepository.countSummaries("test", "name")).thenReturn(100L);

        LedgerSummaryResponse response = ledgerService.listLedgerSummaries(
                12, 10, "test", "name", "name", "asc"
        );

        assertNotNull(response);
        assertTrue(response.getData().isEmpty());
        assertEquals(12, response.getPagination().getPage());
        assertEquals(10, response.getPagination().getPageSize());
        assertEquals(100L, response.getPagination().getTotalItems());
        assertEquals(10, response.getPagination().getTotalPages());
        assertFalse(response.getPagination().getHasNext());
        assertTrue(response.getPagination().getHasPrevious());

        verify(ledgerSummaryRepository, never()).findSummaries(any(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void testListLedgerSummaries_defaultsAndBounds() {
        when(ledgerSummaryRepository.countSummaries(null, null)).thenReturn(5L);
        
        List<LedgerSummaryItem> mockItems = List.of(new LedgerSummaryItem().id(1L).name("Ledger 1"));
        when(ledgerSummaryRepository.findSummaries(
                null, null, "name", "asc", 0, 50
        )).thenReturn(mockItems);

        LedgerSummaryResponse response = ledgerService.listLedgerSummaries(
                null, null, null, null, null, null
        );

        assertNotNull(response);
        assertEquals(1, response.getPagination().getPage());
        assertEquals(50, response.getPagination().getPageSize());
        assertEquals(1, response.getPagination().getTotalPages());
    }
}
