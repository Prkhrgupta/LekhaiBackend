package in.lekhai.core.account_master.service;

import in.lekhai.accountmaster.area.dto.AreaRequest;
import in.lekhai.accountmaster.area.dto.AreaResponse;
import in.lekhai.core.account_master.domain.Area;
import in.lekhai.core.account_master.repository.AreaRepository;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
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
                request.getAreaName(),
                request.getStateCode());
        Area saved = areaRepository.save(area);
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public List<AreaResponse> listAreas() {
        return StreamSupport.stream(areaRepository.findAll().spliterator(), false)
                .map(this::mapToResponse)
                .toList();
    }

    private AreaResponse mapToResponse(Area area) {
        return new AreaResponse()
                .id(area.getId())
                .areaName(area.getAreaName())
                .stateCode(area.getStateCode())
                .createdAt(OffsetDateTime.ofInstant(area.getCreatedAt(), ZoneId.of(ZoneId.SHORT_IDS.get("IST"))));
    }
}
