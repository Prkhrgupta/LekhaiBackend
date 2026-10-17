package in.lekhai.voucher.service;

import in.lekhai.contract.model.VoucherType;
import in.lekhai.voucher.entity.VoucherCounter;
import in.lekhai.voucher.repository.VoucherCounterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherCounterServiceTest {

    @Mock
    private VoucherCounterRepository voucherCounterRepository;

    @InjectMocks
    private VoucherCounterService counterService;

    @Test
    void peekNextNumber_returnsCounterWithoutReserving() {
        when(voucherCounterRepository.findByVoucherType(VoucherType.PAYMENT))
                .thenReturn(Optional.of(new VoucherCounter(VoucherType.PAYMENT, 7L)));

        assertThat(counterService.peekNextNumber(VoucherType.PAYMENT)).isEqualTo(7L);
    }

    @Test
    void peekNextNumber_missingCounterStartsAtOne() {
        when(voucherCounterRepository.findByVoucherType(VoucherType.RECEIPT))
                .thenReturn(Optional.empty());

        assertThat(counterService.peekNextNumber(VoucherType.RECEIPT)).isEqualTo(1L);
    }
}
