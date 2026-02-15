package in.lekhai.core.account_master.controller;

import in.lekhai.accountmaster.state.api.StateApi;
import in.lekhai.accountmaster.state.dto.StateResponse;
import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.common.Result;
import in.lekhai.core.account_master.service.StateService;
import in.lekhai.shop.context.model.ShopContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class StateController implements StateApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final StateService stateService;

    public StateController(
            StateService stateService
    ) {
        this.stateService = stateService;
    }

    @Override
    public ResponseEntity<List<StateResponse>> listAllStates() {
        log.info("Got a request to list all states {}", ShopContext.getShopCode());
        List<StateResponse> response = stateService.listStates();
        log.info("Successfully listed all states {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }
}
