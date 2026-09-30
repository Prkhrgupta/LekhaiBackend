package in.lekhai.core.inventory_master.service;

import in.lekhai.contract.model.StockItemRequest;
import in.lekhai.contract.model.StockItemResponse;
import in.lekhai.contract.model.Uqc;
import in.lekhai.core.inventory_master.domain.StockItem;
import in.lekhai.core.inventory_master.domain.Uom;
import in.lekhai.core.inventory_master.repository.CommodityRepository;
import in.lekhai.core.inventory_master.repository.ItemCategoryRepository;
import in.lekhai.core.inventory_master.repository.ItemFactoryRepository;
import in.lekhai.core.inventory_master.repository.StockItemRepository;
import in.lekhai.core.inventory_master.repository.UomRepository;
import in.lekhai.error.controller.LekhaiException;
import in.lekhai.error.controller.uom.exception.UomNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockItemServiceTest {

    @Mock
    private StockItemRepository stockItemRepository;
    @Mock
    private CommodityRepository commodityRepository;
    @Mock
    private ItemCategoryRepository itemCategoryRepository;
    @Mock
    private ItemFactoryRepository itemFactoryRepository;
    @Mock
    private UomRepository uomRepository;

    private StockItemService stockItemService;

    @BeforeEach
    void setUp() {
        stockItemService = new StockItemService(
                stockItemRepository,
                commodityRepository,
                itemCategoryRepository,
                itemFactoryRepository,
                uomRepository,
                new StockItemUomValidator(uomRepository));
    }

    @Test
    void createStockItem_persistsSingleUnitItemWithoutFactor() {
        when(uomRepository.findById(1L)).thenReturn(Optional.of(activeUom(1L, "Piece", "PCS")));
        when(stockItemRepository.save(any(StockItem.class))).thenAnswer(invocation -> {
            StockItem item = invocation.getArgument(0);
            item.setId(10L);
            return item;
        });

        StockItemResponse actual = stockItemService.createStockItem(singleUnitRequest());

        assertThat(actual.getId()).isEqualTo(10L);
        assertThat(actual.getPrimaryUomId()).isEqualTo(1L);
        assertThat(actual.getAlternateUomId()).isNull();
        assertThat(actual.getConversionFactor()).isNull();
        assertThat(actual.getOpeningQty()).isEqualTo(100.0);
        ArgumentCaptor<StockItem> captor = ArgumentCaptor.forClass(StockItem.class);
        verify(stockItemRepository).save(captor.capture());
        assertThat(captor.getValue().getPrimaryUomId()).isEqualTo(1L);
        assertThat(captor.getValue().getConversionFactor()).isNull();
    }

    @Test
    void createStockItem_persistsAlternateUnitWithPositiveFactor() {
        when(uomRepository.findById(1L)).thenReturn(Optional.of(activeUom(1L, "Piece", "PCS")));
        when(uomRepository.findById(2L)).thenReturn(Optional.of(activeUom(2L, "Box", "BOX")));
        when(stockItemRepository.save(any(StockItem.class))).thenAnswer(invocation -> {
            StockItem item = invocation.getArgument(0);
            item.setId(11L);
            return item;
        });

        StockItemRequest request = singleUnitRequest().alternateUomId(2L).conversionFactor(10.0);

        StockItemResponse actual = stockItemService.createStockItem(request);

        assertThat(actual.getAlternateUomId()).isEqualTo(2L);
        assertThat(actual.getConversionFactor()).isEqualTo(10.0);
        assertThat(actual.getPrimaryUomName()).isEqualTo("Piece");
        assertThat(actual.getAlternateUomName()).isEqualTo("Box");
        assertThat(actual.getPrimaryQuantityCode()).isEqualTo(Uqc.PCS);
        assertThat(actual.getAlternateQuantityCode()).isEqualTo(Uqc.BOX);
    }

    @Test
    void createStockItem_rejectsMissingPrimaryUom() {
        assertThatThrownBy(() -> stockItemService.createStockItem(singleUnitRequest().primaryUomId(null)))
                .isInstanceOf(LekhaiException.class)
                .hasMessageContaining("Primary UOM");
    }

    @Test
    void createStockItem_rejectsUnknownPrimaryUom() {
        when(uomRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockItemService.createStockItem(singleUnitRequest().primaryUomId(9L)))
                .isInstanceOf(UomNotFoundException.class)
                .hasMessageContaining("9");
    }

    @Test
    void createStockItem_rejectsAlternateEqualToPrimary() {
        when(uomRepository.findById(1L)).thenReturn(Optional.of(activeUom(1L, "Piece", "PCS")));

        StockItemRequest request = singleUnitRequest().alternateUomId(1L).conversionFactor(10.0);

        assertThatThrownBy(() -> stockItemService.createStockItem(request))
                .isInstanceOf(LekhaiException.class)
                .hasMessageContaining("must differ");
    }

    @Test
    void createStockItem_rejectsMissingFactorWithAlternate() {
        when(uomRepository.findById(1L)).thenReturn(Optional.of(activeUom(1L, "Piece", "PCS")));
        when(uomRepository.findById(2L)).thenReturn(Optional.of(activeUom(2L, "Box", "BOX")));

        assertThatThrownBy(() -> stockItemService.createStockItem(singleUnitRequest().alternateUomId(2L)))
                .isInstanceOf(LekhaiException.class)
                .hasMessageContaining("Conversion factor must be positive");
    }

    @Test
    void createStockItem_rejectsZeroFactorWithAlternate() {
        when(uomRepository.findById(1L)).thenReturn(Optional.of(activeUom(1L, "Piece", "PCS")));
        when(uomRepository.findById(2L)).thenReturn(Optional.of(activeUom(2L, "Box", "BOX")));

        StockItemRequest request = singleUnitRequest().alternateUomId(2L).conversionFactor(0.0);

        assertThatThrownBy(() -> stockItemService.createStockItem(request))
                .isInstanceOf(LekhaiException.class)
                .hasMessageContaining("Conversion factor must be positive");
    }

    @Test
    void createStockItem_rejectsFactorWithoutAlternate() {
        when(uomRepository.findById(1L)).thenReturn(Optional.of(activeUom(1L, "Piece", "PCS")));

        assertThatThrownBy(() -> stockItemService.createStockItem(singleUnitRequest().conversionFactor(10.0)))
                .isInstanceOf(LekhaiException.class)
                .hasMessageContaining("must be absent");
    }

    @Test
    void updateStockItem_persistsAlternateUnitWithPositiveFactor() {
        StockItem stored = new StockItem();
        stored.setId(10L);
        stored.setItemName("Cotton Shirt");
        stored.setPrimaryUomId(1L);
        when(stockItemRepository.findById(10L)).thenReturn(Optional.of(stored));
        when(uomRepository.findById(1L)).thenReturn(Optional.of(activeUom(1L, "Piece", "PCS")));
        when(uomRepository.findById(2L)).thenReturn(Optional.of(activeUom(2L, "Box", "BOX")));
        when(stockItemRepository.save(any(StockItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockItemResponse actual = stockItemService.updateStockItem(
                10L, singleUnitRequest().alternateUomId(2L).conversionFactor(10.0));

        assertThat(actual.getAlternateUomId()).isEqualTo(2L);
        assertThat(actual.getConversionFactor()).isEqualTo(10.0);
    }

    @Test
    void updateStockItem_rejectsFactorWithoutAlternate() {
        StockItem stored = new StockItem();
        stored.setId(10L);
        stored.setItemName("Cotton Shirt");
        stored.setPrimaryUomId(1L);
        when(stockItemRepository.findById(10L)).thenReturn(Optional.of(stored));
        when(uomRepository.findById(1L)).thenReturn(Optional.of(activeUom(1L, "Piece", "PCS")));

        assertThatThrownBy(() -> stockItemService.updateStockItem(
                10L, singleUnitRequest().conversionFactor(10.0)))
                .isInstanceOf(LekhaiException.class)
                .hasMessageContaining("must be absent");
    }

    private static StockItemRequest singleUnitRequest() {
        return new StockItemRequest()
                .itemName("Cotton Shirt")
                .primaryUomId(1L)
                .openingQty(100.0)
                .openingRate(50.0)
                .openingValue(5000.0);
    }

    private static Uom activeUom(Long id, String name, String code) {
        Uom uom = new Uom();
        uom.setId(id);
        uom.setUnitName(name);
        uom.setQuantityCode(code);
        return uom;
    }
}
