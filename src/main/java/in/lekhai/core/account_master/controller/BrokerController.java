package in.lekhai.core.account_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.BrokerApi;
import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.service.BrokerService;
import in.lekhai.shop.context.model.ShopContext;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<BrokerResponse> getBroker(Long id) {
        log.info("Got a request to fetch broker {} :: broker id {}", ShopContext.getShopCode(), id);
        BrokerResponse response = brokerService.getBrokerById(id);
        log.info("Successfully fetched broker {} :: {}", ShopContext.getShopCode(), response.toString());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<DropdownItem>> getBrokerDropdownOptions() {
        log.info("Got a request to list all brokers {}", ShopContext.getShopCode());
        List<DropdownItem> response = brokerService.listBrokers();
        log.info("Successfully listed all brokers {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BrokerResponse> updateBroker(Long id, BrokerRequest request) {
        log.info("Got a request to update broker {} :: {}", ShopContext.getShopCode(), request.toString());
        BrokerResponse response = brokerService.updateBroker(id, request);
        log.info("Successfully updated broker {} :: {}", ShopContext.getShopCode(), response.toString());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BrokerSummaryPageResponse> getBrokerSummaries(@Valid BrokerSearchableField brokerSearchableField,
                                                                        @Valid String query,
                                                                        Pageable pageable) {
        log.info("Got a request to fetch broker summary {}", ShopContext.getShopCode());
        BrokerSummaryPageResponse response = brokerService.listBrokerSummaries(brokerSearchableField, query, pageable);
        log.info("Successfully fetched broker summary {} :: {}", ShopContext.getShopCode(), response.toString());
        return ResponseEntity.ok(response);
    }
}
