package in.lekhai.core.account_master.service;

import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.domain.Area;
import in.lekhai.core.account_master.repository.AreaRepository;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class AreaService {

    private final AreaRepository areaRepository;

    public AreaService(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    @ShopContextTransactional
    public AreaResponse createArea(AreaRequest request) {
        Area area = new Area(
                request.getAreaName());
        Area saved = areaRepository.save(area);
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public List<DropdownItem> listAreas() {
        return StreamSupport.stream(areaRepository.findAll().spliterator(), false)
                .map(area -> new DropdownItem().id(area.getId()).label(area.getAreaName()))
                .toList();
    }

    @ShopContextTransactional
    public AreaSummaryPageResponse listAreaSummaries(AreaSearchableField searchableField, String query, Pageable pageable) {
        Page<Area> areasPage;
        if (query != null && !query.trim().isEmpty() && searchableField == AreaSearchableField.NAME) {
            List<Area> areas = areaRepository.findByAreaNameContainingIgnoreCase(query.trim(), pageable);
            long total = areaRepository.countByAreaNameContainingIgnoreCase(query.trim());
            areasPage = new PageImpl<>(areas, pageable, total);
        } else {
            areasPage = areaRepository.findAll(pageable);
        }

        List<AreaSummaryItem> data = areasPage.getContent().stream()
                .map(area -> new AreaSummaryItem()
                        .id(area.getId())
                        .areaName(area.getAreaName()))
                .toList();

        return new AreaSummaryPageResponse()
                .data(data)
                .pagination(new PaginationMeta()
                        .page(areasPage.getNumber())
                        .size(areasPage.getSize())
                        .totalElements(areasPage.getTotalElements())
                        .totalPages(areasPage.getTotalPages()));
    }

    private AreaResponse mapToResponse(Area area) {
        return new AreaResponse()
                .id(area.getId())
                .areaName(area.getAreaName());
    }
}
