package in.lekhai.core.account_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.common.Result;
import in.lekhai.core.account_master.dto.AreaRequest;
import in.lekhai.core.account_master.dto.AreaResponse;
import in.lekhai.core.account_master.service.AreaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/area")
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class AreaController {

    private final AreaService areaService;

    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }

    @PostMapping("/create")
    public ResponseEntity<Result<AreaResponse>> createArea(@RequestBody @Valid AreaRequest request) {
        AreaResponse response = areaService.createArea(request);
        return ResponseEntity.ok(Result.success(response));
    }

    @GetMapping("/list-all")
    public ResponseEntity<Result<List<AreaResponse>>> listAreas() {
        List<AreaResponse> response = areaService.listAreas();
        return ResponseEntity.ok(Result.success(response));
    }
}
