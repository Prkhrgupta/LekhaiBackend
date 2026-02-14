package in.lekhai.core.account_master.controller;

import in.lekhai.accountmaster.area.api.AreaApi;
import in.lekhai.accountmaster.area.dto.AreaRequest;
import in.lekhai.accountmaster.area.dto.AreaResponse;
import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.core.account_master.service.AreaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class AreaController implements AreaApi {

    private final AreaService areaService;

    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }

    @Override
    public ResponseEntity<AreaResponse> createArea(@Valid AreaRequest request) {
        AreaResponse response = areaService.createArea(request);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<AreaResponse>> listAreas() {
        List<AreaResponse> response = areaService.listAreas();
        return ResponseEntity.ok(response);
    }
}
