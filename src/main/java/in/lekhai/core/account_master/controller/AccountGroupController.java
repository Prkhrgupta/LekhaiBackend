package in.lekhai.core.account_master.controller;

import in.lekhai.accountmaster.accountgroup.api.AccountGroupApi;
import in.lekhai.accountmaster.accountgroup.dto.AccountGroupRequest;
import in.lekhai.accountmaster.accountgroup.dto.AccountGroupResponse;
import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.core.account_master.service.AccountGroupService;
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
public class AccountGroupController implements AccountGroupApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final AccountGroupService accountGroupService;

    public AccountGroupController(
            AccountGroupService accountGroupService
    ) {
        this.accountGroupService = accountGroupService;
    }

    @Override
    public ResponseEntity<AccountGroupResponse> createAccountGroup(@Valid AccountGroupRequest request) {
        log.info("Got a request to create account group {} :: {}", ShopContext.getShopCode(), request.toString());
        AccountGroupResponse response = accountGroupService.createAccountGroup(request);
        log.info("Successfully created account group {} :: {}", ShopContext.getShopCode(), response.toString());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<AccountGroupResponse>> listAccountGroups() {
        log.info("Got a request to list all account groups {}", ShopContext.getShopCode());
        List<AccountGroupResponse> response = accountGroupService.listAccountGroups();
        log.info("Successfully listed all account groups {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }
}
