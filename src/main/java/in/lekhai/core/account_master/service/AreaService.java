package in.lekhai.core.account_master.service;

import in.lekhai.core.account_master.domain.Area;
import in.lekhai.core.account_master.dto.AreaRequest;
import in.lekhai.core.account_master.dto.AreaResponse;
import in.lekhai.core.account_master.repository.AreaRepository;
import in.lekhai.shop.context.transaction.manager.annotation.ShopTransactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class AreaService {

    private final AreaRepository areaRepository;

    public AreaService(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    @ShopTransactional
    public AreaResponse createArea(AreaRequest request) {
        Area area = new Area(
                request.areaName(),
                request.stateCode());
        Area saved = areaRepository.save(area);
        return mapToResponse(saved);
    }

    @ShopTransactional
    public List<AreaResponse> listAreas() {
        return StreamSupport.stream(areaRepository.findAll().spliterator(), false)
                .map(this::mapToResponse)
                .toList();
    }

    private AreaResponse mapToResponse(Area area) {
        return new AreaResponse(
                area.getId(),
                area.getAreaName(),
                area.getStateCode(),
                area.getShopCode(),
                area.getCreatedAt());
    }
}
