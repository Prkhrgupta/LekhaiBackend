package in.lekhai.core.inventory_master.service;

import in.lekhai.contract.model.UomRequest;
import in.lekhai.contract.model.UomResponse;
import in.lekhai.contract.model.Uqc;
import in.lekhai.core.inventory_master.domain.Uom;
import in.lekhai.core.inventory_master.repository.StockItemRepository;
import in.lekhai.core.inventory_master.repository.UomRepository;
import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.error.controller.LekhaiException;
import in.lekhai.error.controller.uom.exception.UomInUseException;
import in.lekhai.error.controller.uom.exception.UomNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UomServiceTest {

    @Mock
    private UomRepository uomRepository;
    @Mock
    private StockItemRepository stockItemRepository;

    @InjectMocks
    private UomService uomService;

    @Test
    void createUom_persistsTrimmedLabelWithQuantityCode() {
        when(uomRepository.save(any(Uom.class))).thenAnswer(invocation -> {
            Uom uom = invocation.getArgument(0);
            uom.setId(1L);
            return uom;
        });

        UomResponse actual = uomService.createUom(new UomRequest().unitName("  Gunny Bag ").quantityCode(Uqc.BAG));

        assertThat(actual.getId()).isEqualTo(1L);
        assertThat(actual.getUnitName()).isEqualTo("Gunny Bag");
        assertThat(actual.getQuantityCode()).isEqualTo(Uqc.BAG);
    }

    @Test
    void createUom_rejectsBlankUnitName() {
        assertThatThrownBy(() -> uomService.createUom(new UomRequest().unitName("  ").quantityCode(Uqc.BAG)))
                .isInstanceOf(LekhaiException.class)
                .hasMessageContaining("unit name");
    }

    @Test
    void createUom_rejectsMissingQuantityCode() {
        assertThatThrownBy(() -> uomService.createUom(new UomRequest().unitName("Gunny Bag")))
                .isInstanceOf(LekhaiException.class)
                .hasMessageContaining("quantity code");
    }

    @Test
    void updateUom_allowsRenamingLabel() {
        Uom stored = storedUom(7L, "Gunny Bag", "BAG");
        when(uomRepository.findById(7L)).thenReturn(Optional.of(stored));
        when(uomRepository.save(any(Uom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UomResponse actual = uomService.updateUom(7L, new UomRequest().unitName("Jute Bag").quantityCode(Uqc.BAG));

        assertThat(actual.getUnitName()).isEqualTo("Jute Bag");
        assertThat(actual.getQuantityCode()).isEqualTo(Uqc.BAG);
    }

    @Test
    void updateUom_blocksQuantityCodeChangeWhenInUse() {
        Uom stored = storedUom(7L, "Gunny Bag", "BAG");
        when(uomRepository.findById(7L)).thenReturn(Optional.of(stored));
        when(stockItemRepository.countActiveByUomId(7L)).thenReturn(2L);

        assertThatThrownBy(() -> uomService.updateUom(
                7L, new UomRequest().unitName("Gunny Bag").quantityCode(Uqc.BOX)))
                .isInstanceOf(UomInUseException.class)
                .isInstanceOf(LekhaiClientException.class)
                .hasMessageContaining("Quantity code")
                .satisfies(e -> assertThat(((LekhaiClientException) e).getStatusCode().value()).isEqualTo(409));
    }

    @Test
    void updateUom_allowsQuantityCodeChangeWhenUnused() {
        Uom stored = storedUom(7L, "Gunny Bag", "BAG");
        when(uomRepository.findById(7L)).thenReturn(Optional.of(stored));
        when(stockItemRepository.countActiveByUomId(7L)).thenReturn(0L);
        when(uomRepository.save(any(Uom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UomResponse actual = uomService.updateUom(
                7L, new UomRequest().unitName("Gunny Bag").quantityCode(Uqc.BOX));

        assertThat(actual.getQuantityCode()).isEqualTo(Uqc.BOX);
    }

    @Test
    void deleteUom_blocksWhenReferencedByStockItems() {
        when(uomRepository.findById(7L)).thenReturn(Optional.of(storedUom(7L, "Gunny Bag", "BAG")));
        when(stockItemRepository.countActiveByUomId(7L)).thenReturn(1L);

        assertThatThrownBy(() -> uomService.deleteUom(7L))
                .isInstanceOf(UomInUseException.class)
                .isInstanceOf(LekhaiClientException.class)
                .hasMessageContaining("cannot be deleted")
                .satisfies(e -> assertThat(((LekhaiClientException) e).getStatusCode().value()).isEqualTo(409));
    }

    @Test
    void deleteUom_softDeletesWhenUnreferenced() {
        Uom stored = storedUom(7L, "Gunny Bag", "BAG");
        when(uomRepository.findById(7L)).thenReturn(Optional.of(stored));
        when(stockItemRepository.countActiveByUomId(7L)).thenReturn(0L);
        when(uomRepository.save(any(Uom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        uomService.deleteUom(7L);

        ArgumentCaptor<Uom> captor = ArgumentCaptor.forClass(Uom.class);
        verify(uomRepository).save(captor.capture());
        assertThat(captor.getValue().getDeleted()).isTrue();
    }

    @Test
    void getUom_rejectsUnknownId() {
        when(uomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uomService.getUomById(99L))
                .isInstanceOf(UomNotFoundException.class)
                .hasMessageContaining("99");
    }

    private static Uom storedUom(Long id, String name, String code) {
        Uom uom = new Uom();
        uom.setId(id);
        uom.setUnitName(name);
        uom.setQuantityCode(code);
        return uom;
    }
}
