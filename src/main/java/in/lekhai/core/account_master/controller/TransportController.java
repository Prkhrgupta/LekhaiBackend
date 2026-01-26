package in.lekhai.core.account_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.common.Result;
import in.lekhai.core.account_master.dto.TransportRequest;
import in.lekhai.core.account_master.dto.TransportResponse;
import in.lekhai.core.account_master.service.TransportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transport")
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class TransportController {

    private final TransportService transportService;

    public TransportController(TransportService transportService) {
        this.transportService = transportService;
    }

    @PostMapping("/create")
    public ResponseEntity<Result<TransportResponse>> createTransport(@RequestBody @Valid TransportRequest request) {
        TransportResponse response = transportService.createTransport(request);
        return ResponseEntity.ok(Result.success(response));
    }

    @GetMapping("/list-all")
    public ResponseEntity<Result<List<TransportResponse>>> listTransports() {
        List<TransportResponse> response = transportService.listTransports();
        return ResponseEntity.ok(Result.success(response));
    }
}
