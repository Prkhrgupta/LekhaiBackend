package in.lekhai.core.account_master.service;

import in.lekhai.core.account_master.domain.AccountGroup;
import in.lekhai.core.account_master.dto.AccountGroupRequest;
import in.lekhai.core.account_master.dto.AccountGroupResponse;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
import in.lekhai.error.controller.account.exception.ParentNotFoundException;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class AccountGroupService {

    private final AccountGroupRepository accountGroupRepository;

    public AccountGroupService(
            AccountGroupRepository accountGroupRepository
    ) {
        this.accountGroupRepository = accountGroupRepository;
    }

    @ShopContextTransactional
    public AccountGroupResponse createAccountGroup(AccountGroupRequest request) {
        // TODO: handle duplicate key exceptions
        AccountGroup accountGroup = accountGroupRepository
                .findById(request.parentId())
                .orElseThrow(() -> new ParentNotFoundException(request.parentId()));

        AccountGroup createSubGroupRequest = new AccountGroup(
                request.name(),
                accountGroup.getId(),
                accountGroup.getNature(),
                accountGroup.getBehaviour(),
                Boolean.FALSE
        );

        AccountGroup subGroup = accountGroupRepository.save(createSubGroupRequest);
        return new AccountGroupResponse(subGroup.getId(), subGroup.getName());
    }

    @ShopContextTransactional
    public List<AccountGroupResponse> listAccountGroups() {
        return StreamSupport.stream(accountGroupRepository.findAll().spliterator(), false)
                .map(g -> new AccountGroupResponse(g.getId(), g.getName()))
                .toList();
    }
}
