package in.lekhai.accountbooks.multipleledgerprinting.service;

import in.lekhai.contract.model.AccountEntryType;
import in.lekhai.contract.model.MultipleLedgerItem;
import in.lekhai.contract.model.MultipleLedgerPrintingResponse;
import in.lekhai.core.account_master.domain.AccountGroup;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
import in.lekhai.core.account_master.repository.LedgerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultipleLedgerPrintingServiceTest {

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private AccountGroupRepository accountGroupRepository;

    private MultipleLedgerPrintingService service;

    @BeforeEach
    void setUp() {
        service = new MultipleLedgerPrintingService(ledgerRepository, accountGroupRepository);
    }

    @Test
    void getMultipleLedgerPrinting_emptyLedgers_returnsEmpty() {
        when(accountGroupRepository.findAll()).thenReturn(List.of());
        when(ledgerRepository.findAll()).thenReturn(List.of());

        MultipleLedgerPrintingResponse response = service.getMultipleLedgerPrinting();

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Index for Account Ledgers");
        assertThat(response.getTotalLedgers()).isEqualTo(0);
        assertThat(response.getItems()).isEmpty();
    }

    @Test
    void getMultipleLedgerPrinting_ordersGroupsAndSequentialNumbering() {
        // Group 1: Capital Account (Liability, root)
        AccountGroup capitalGroup = new AccountGroup("Capital Account", null, "LIABILITY", null, true);
        capitalGroup.setId(1L);

        // Group 2: Loans (Liability, root)
        AccountGroup loansGroup = new AccountGroup("Loans (Liability)", null, "LIABILITY", null, true);
        loansGroup.setId(2L);

        // Group 17: Secured Loans (child of 2)
        AccountGroup securedLoansGroup = new AccountGroup("Secured Loans", 2L, "LIABILITY", null, false);
        securedLoansGroup.setId(17L);

        // Group 4: Fixed Assets (Asset, root)
        AccountGroup fixedAssetsGroup = new AccountGroup("Fixed Assets", null, "ASSET", null, true);
        fixedAssetsGroup.setId(4L);

        when(accountGroupRepository.findAll()).thenReturn(List.of(capitalGroup, loansGroup, securedLoansGroup, fixedAssetsGroup));

        Ledger ledger1 = new Ledger("NARAIN DAS OMAR \"PROP\"", "Legal", 1L, BigDecimal.ZERO, AccountEntryType.CR, BigDecimal.ZERO, null, null, null, null, null, null, null, null, null, null, null, null);
        ledger1.setId(101L);

        Ledger ledger2 = new Ledger("BUSINESS LOAN \"MSME A/C\"", "Legal", 17L, BigDecimal.ZERO, AccountEntryType.CR, BigDecimal.ZERO, null, null, null, null, null, null, null, null, null, null, null, null);
        ledger2.setId(102L);

        Ledger ledger3 = new Ledger("CAR", "Legal", 4L, BigDecimal.ZERO, AccountEntryType.DR, BigDecimal.ZERO, null, null, null, null, null, null, null, null, null, null, null, null);
        ledger3.setId(103L);

        Ledger ledger4 = new Ledger("AIR CONDITIONER", "Legal", 4L, BigDecimal.ZERO, AccountEntryType.DR, BigDecimal.ZERO, null, null, null, null, null, null, null, null, null, null, null, null);
        ledger4.setId(104L);

        when(ledgerRepository.findAll()).thenReturn(List.of(ledger1, ledger2, ledger3, ledger4));

        MultipleLedgerPrintingResponse response = service.getMultipleLedgerPrinting();

        assertThat(response).isNotNull();
        assertThat(response.getTotalLedgers()).isEqualTo(4);

        List<MultipleLedgerItem> items = response.getItems();
        // Expected order:
        // 1. Group Header: Capital Account
        // 2. Ledger: 1. NARAIN DAS OMAR "PROP"
        // 3. Group Header: Secured Loans
        // 4. Ledger: 2. BUSINESS LOAN "MSME A/C"
        // 5. Group Header: Fixed Assets
        // 6. Ledger: 3. AIR CONDITIONER (alphabetical before CAR)
        // 7. Ledger: 4. CAR

        assertThat(items).hasSize(7);

        assertThat(items.get(0).getIsGroup()).isTrue();
        assertThat(items.get(0).getParticulars()).isEqualTo("Capital Account");

        assertThat(items.get(1).getIsGroup()).isFalse();
        assertThat(items.get(1).getSerialNumber().get()).isEqualTo(1);
        assertThat(items.get(1).getParticulars()).isEqualTo("NARAIN DAS OMAR \"PROP\"");

        assertThat(items.get(2).getIsGroup()).isTrue();
        assertThat(items.get(2).getParticulars()).isEqualTo("Secured Loans");

        assertThat(items.get(3).getIsGroup()).isFalse();
        assertThat(items.get(3).getSerialNumber().get()).isEqualTo(2);
        assertThat(items.get(3).getParticulars()).isEqualTo("BUSINESS LOAN \"MSME A/C\"");

        assertThat(items.get(4).getIsGroup()).isTrue();
        assertThat(items.get(4).getParticulars()).isEqualTo("Fixed Assets");

        assertThat(items.get(5).getIsGroup()).isFalse();
        assertThat(items.get(5).getSerialNumber().get()).isEqualTo(3);
        assertThat(items.get(5).getParticulars()).isEqualTo("AIR CONDITIONER");

        assertThat(items.get(6).getIsGroup()).isFalse();
        assertThat(items.get(6).getSerialNumber().get()).isEqualTo(4);
        assertThat(items.get(6).getParticulars()).isEqualTo("CAR");
    }
}
