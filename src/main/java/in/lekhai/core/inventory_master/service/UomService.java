package in.lekhai.core.inventory_master.service;

import in.lekhai.contract.model.DropdownItem;
import in.lekhai.contract.model.PaginationMeta;
import in.lekhai.contract.model.UomRequest;
import in.lekhai.contract.model.UomResponse;
import in.lekhai.contract.model.UomSearchableField;
import in.lekhai.contract.model.UomSummaryPageResponse;
import in.lekhai.contract.model.Uqc;
import in.lekhai.core.inventory_master.domain.Uom;
import in.lekhai.core.inventory_master.repository.StockItemRepository;
import in.lekhai.core.inventory_master.repository.UomRepository;
import in.lekhai.error.controller.LekhaiException;
import in.lekhai.error.controller.uom.exception.UomInUseException;
import in.lekhai.error.controller.uom.exception.UomNotFoundException;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class UomService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final UomRepository uomRepository;
    private final StockItemRepository stockItemRepository;

    public UomService(UomRepository uomRepository, StockItemRepository stockItemRepository) {
        this.uomRepository = uomRepository;
        this.stockItemRepository = stockItemRepository;
    }

    @ShopContextTransactional
    public UomResponse createUom(UomRequest request) {
        validateRequest(request);
        Uom saved = uomRepository.save(mapToEntity(request, new Uom()));
        log.info("Saved UOM :: id {}", saved.getId());
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public UomResponse updateUom(Long id, UomRequest request) {
        validateRequest(request);
        Uom uom = findActiveById(id);
        rejectQuantityCodeChangeWhenInUse(uom, request);
        Uom saved = uomRepository.save(mapToEntity(request, uom));
        log.info("Updated UOM :: id {}", saved.getId());
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public void deleteUom(Long id) {
        Uom uom = findActiveById(id);
        if (stockItemRepository.countActiveByUomId(id) > 0) {
            log.warn("Delete blocked: UOM id {} is used by stock items", id);
            throw new UomInUseException(id);
        }
        uom.setDeleted(Boolean.TRUE);
        uomRepository.save(uom);
        log.info("Soft-deleted UOM :: id {}", uom.getId());
    }

    @ShopContextTransactional
    public UomResponse getUomById(Long id) {
        return mapToResponse(findActiveById(id));
    }

    @ShopContextTransactional
    public List<DropdownItem> listUoms() {
        return StreamSupport.stream(uomRepository.findAll().spliterator(), false)
                .filter(uom -> !Boolean.TRUE.equals(uom.getDeleted()))
                .map(uom -> new DropdownItem().id(uom.getId()).label(uom.getUnitName()))
                .toList();
    }

    @ShopContextTransactional
    public UomSummaryPageResponse listUomSummaries(
            UomSearchableField searchableField,
            String searchText,
            Pageable pageable) {
        Page<Uom> page = fetchPage(searchableField, searchText, pageable);
        List<UomResponse> data = page.getContent().stream().map(this::mapToResponse).toList();
        return new UomSummaryPageResponse()
                .data(data)
                .pagination(new PaginationMeta()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages()));
    }

    private Page<Uom> fetchPage(UomSearchableField field, String text, Pageable pageable) {
        if (text != null && !text.trim().isEmpty() && field == UomSearchableField.NAME) {
            List<Uom> uoms = uomRepository.findActiveByNameContainingIgnoreCase(
                    text.trim(), pageable.getPageSize(), pageable.getOffset());
            long total = uomRepository.countActiveByNameContainingIgnoreCase(text.trim());
            return new PageImpl<>(uoms, pageable, total);
        }
        List<Uom> uoms = uomRepository.findAllActive(pageable.getPageSize(), pageable.getOffset());
        return new PageImpl<>(uoms, pageable, uomRepository.countAllActive());
    }

    private Uom findActiveById(Long id) {
        Uom uom = uomRepository.findById(id).orElseThrow(() -> new UomNotFoundException(id));
        if (Boolean.TRUE.equals(uom.getDeleted())) {
            throw new UomNotFoundException(id);
        }
        return uom;
    }

    private void rejectQuantityCodeChangeWhenInUse(Uom existing, UomRequest request) {
        String current = existing.getQuantityCode();
        String next = request.getQuantityCode() == null ? null : request.getQuantityCode().getValue();
        if (current != null && !current.equals(next)
                && stockItemRepository.countActiveByUomId(existing.getId()) > 0) {
            log.warn("Update blocked: UOM id {} quantity code change while in use", existing.getId());
            throw new UomInUseException(
                    existing.getId(), "Quantity code cannot be changed while stock items use this unit.");
        }
    }

    private static void validateRequest(UomRequest request) {
        if (request == null) {
            throw new LekhaiException("UOM request must not be null");
        }
        if (request.getUnitName() == null || request.getUnitName().trim().isEmpty()) {
            throw new LekhaiException("UOM unit name must not be blank");
        }
        if (request.getQuantityCode() == null) {
            throw new LekhaiException("UOM quantity code must not be null");
        }
    }

    private static Uom mapToEntity(UomRequest request, Uom uom) {
        uom.setUnitName(request.getUnitName().trim());
        uom.setQuantityCode(request.getQuantityCode().getValue());
        return uom;
    }

    private UomResponse mapToResponse(Uom uom) {
        return new UomResponse()
                .id(uom.getId())
                .unitName(uom.getUnitName())
                .quantityCode(uom.getQuantityCode() == null ? null : Uqc.fromValue(uom.getQuantityCode()));
    }
}
