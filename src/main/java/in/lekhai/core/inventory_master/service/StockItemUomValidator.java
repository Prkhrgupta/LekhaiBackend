package in.lekhai.core.inventory_master.service;

import java.math.BigDecimal;

import in.lekhai.contract.model.StockItemRequest;
import in.lekhai.core.inventory_master.domain.Uom;
import in.lekhai.core.inventory_master.repository.UomRepository;
import in.lekhai.error.controller.LekhaiException;
import in.lekhai.error.controller.uom.exception.UomNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class StockItemUomValidator {

    private final UomRepository uomRepository;

    public StockItemUomValidator(UomRepository uomRepository) {
        this.uomRepository = uomRepository;
    }

    public void validate(StockItemRequest request) {
        if (request == null) {
            throw new LekhaiException("Stock item request must not be null");
        }
        if (request.getPrimaryUomId() == null) {
            throw new LekhaiException("Primary UOM must not be null");
        }
        resolveActiveUom(request.getPrimaryUomId());
        if (request.getAlternateUomId() == null) {
            rejectFactorWithoutAlternate(request.getConversionFactor());
            return;
        }
        if (request.getAlternateUomId().equals(request.getPrimaryUomId())) {
            throw new LekhaiException("Alternate UOM must differ from Primary UOM");
        }
        resolveActiveUom(request.getAlternateUomId());
        rejectMissingOrNonPositiveFactor(request.getConversionFactor());
    }

    public Uom resolveActiveUom(Long uomId) {
        Uom uom = uomRepository.findById(uomId).orElseThrow(() -> new UomNotFoundException(uomId));
        if (Boolean.TRUE.equals(uom.getDeleted())) {
            throw new UomNotFoundException(uomId);
        }
        return uom;
    }

    private static void rejectFactorWithoutAlternate(Double conversionFactor) {
        if (conversionFactor != null) {
            throw new LekhaiException("Conversion factor must be absent when Alternate UOM is absent");
        }
    }

    private static void rejectMissingOrNonPositiveFactor(Double conversionFactor) {
        if (conversionFactor == null || BigDecimal.valueOf(conversionFactor).signum() <= 0) {
            throw new LekhaiException("Conversion factor must be positive when Alternate UOM is present");
        }
    }
}
