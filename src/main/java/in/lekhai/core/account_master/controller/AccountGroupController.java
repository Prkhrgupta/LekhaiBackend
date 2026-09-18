package in.lekhai.core.account_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.AccountGroupApi;
import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.service.AccountGroupService;
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
    public ResponseEntity<List<DropdownItem>> getAccountGroupDropdownOptions() {
        log.info("Got a request to list all account groups {}", ShopContext.getShopCode());
        List<DropdownItem> response = accountGroupService.listAccountGroups();
        log.info("Successfully listed all account groups {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AccountGroupSummaryPageResponse> getAccountGroupSummaries(@Valid AccountGroupSearchableField accountGroupSearchableField,
                                                                                    @Valid String query,
                                                                                    Pageable pageable) {
        log.info("Got a request to fetch account group summary {}", ShopContext.getShopCode());
        AccountGroupSummaryPageResponse response = accountGroupService.listAccountGroupSummaries(accountGroupSearchableField, query, pageable);
        log.info("Successfully fetched account group summary {} :: {}", ShopContext.getShopCode(), response.toString());
        return ResponseEntity.ok(response);
    }
}
