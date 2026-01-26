package in.lekhai.core.account_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.common.Result;
import in.lekhai.core.account_master.dto.StateResponse;
import in.lekhai.core.account_master.service.StateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/state")
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class StateController {

    private final StateService stateService;

    public StateController(StateService stateService) {
        this.stateService = stateService;
    }

    @GetMapping("/list-all")
    public ResponseEntity<Result<List<StateResponse>>> listAllStates() {
        List<StateResponse> response = stateService.listStates();
        return ResponseEntity.ok(Result.success(response));
    }
}
