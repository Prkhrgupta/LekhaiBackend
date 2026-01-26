package in.lekhai.core.account_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.common.Result;
import in.lekhai.core.account_master.dto.BrokerRequest;
import in.lekhai.core.account_master.dto.BrokerResponse;
import in.lekhai.core.account_master.service.BrokerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/broker")
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class BrokerController {

    private final BrokerService brokerService;

    public BrokerController(BrokerService brokerService) {
        this.brokerService = brokerService;
    }

    @PostMapping("/create")
    public ResponseEntity<Result<BrokerResponse>> createBroker(@RequestBody @Valid BrokerRequest request) {
        BrokerResponse response = brokerService.createBroker(request);
        return ResponseEntity.ok(Result.success(response));
    }

    @GetMapping("/list-all")
    public ResponseEntity<Result<List<BrokerResponse>>> listBrokers() {
        List<BrokerResponse> response = brokerService.listBrokers();
        return ResponseEntity.ok(Result.success(response));
    }
}
