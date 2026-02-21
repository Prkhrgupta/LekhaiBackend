package in.lekhai.core.account_master.controller;

import in.lekhai.accountmaster.broker.api.BrokerApi;
import in.lekhai.accountmaster.broker.dto.BrokerRequest;
import in.lekhai.accountmaster.broker.dto.BrokerResponse;
import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.core.account_master.service.BrokerService;
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
public class BrokerController implements BrokerApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final BrokerService brokerService;

    public BrokerController(
            BrokerService brokerService
    ) {
        this.brokerService = brokerService;
    }

    @Override
    public ResponseEntity<BrokerResponse> createBroker(@Valid BrokerRequest request) {
        log.info("Got a request to create a broker {} :: {}", ShopContext.getShopCode(), request.toString());
        BrokerResponse response = brokerService.createBroker(request);
        log.info("Successfully create broker {} :: {}", ShopContext.getShopCode(), response.toString());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<BrokerResponse>> listBrokers() {
        log.info("Got a request to list all brokers {}", ShopContext.getShopCode());
        List<BrokerResponse> response = brokerService.listBrokers();
        log.info("Successfully listed all brokers {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }
}
