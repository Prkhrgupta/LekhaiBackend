package in.lekhai.core.account_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.common.Result;
import in.lekhai.core.account_master.dto.AccountGroupRequest;
import in.lekhai.core.account_master.dto.AccountGroupResponse;
import in.lekhai.core.account_master.service.AccountGroupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account-group")
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class AccountGroupController {

    private final AccountGroupService accountGroupService;

    public AccountGroupController(AccountGroupService accountGroupService) {
        this.accountGroupService = accountGroupService;
    }

    @PostMapping("/create")
    public ResponseEntity<Result<AccountGroupResponse>> createAccountGroup(
            @RequestBody @Valid AccountGroupRequest request) {
        AccountGroupResponse response = accountGroupService.createAccountGroup(request);
        return ResponseEntity.ok(Result.success(response));
    }

    @GetMapping("/list-all")
    public ResponseEntity<Result<List<AccountGroupResponse>>> listAccountGroups() {
        List<AccountGroupResponse> response = accountGroupService.listAccountGroups();
        return ResponseEntity.ok(Result.success(response));
    }
}
