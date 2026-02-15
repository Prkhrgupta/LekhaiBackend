package in.lekhai.core.account_master.controller;

import in.lekhai.accountmaster.transport.api.TransportApi;
import in.lekhai.accountmaster.transport.dto.TransportRequest;
import in.lekhai.accountmaster.transport.dto.TransportResponse;
import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.common.Result;
import in.lekhai.core.account_master.service.TransportService;
import in.lekhai.shop.context.model.ShopContext;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class TransportController implements TransportApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final TransportService transportService;

    public TransportController(
            TransportService transportService
    ) {
        this.transportService = transportService;
    }

    @Override
    public ResponseEntity<TransportResponse> createTransport(@Valid TransportRequest request) {
        log.info("Got a request to create a new transport {} :: {}", ShopContext.getShopCode(), request.toString());
        TransportResponse response = transportService.createTransport(request);
        log.info("Successfully added transport details {} :: {}", ShopContext.getShopCode(), response.toString());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<TransportResponse>> listTransports() {
        log.info("Got a request to list all transport detail {}", ShopContext.getShopCode());
        List<TransportResponse> response = transportService.listTransports();
        log.info("Successfully listed all transport details {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }
}
