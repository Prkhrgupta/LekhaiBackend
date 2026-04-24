package in.lekhai.core.account_master.service;

//import in.lekhai.accountmaster.accountgroup.dto.AccountGroupRequest;
//import in.lekhai.accountmaster.accountgroup.dto.AccountGroupResponse;

import in.lekhai.contract.model.AccountGroupRequest;
import in.lekhai.contract.model.AccountGroupResponse;
import in.lekhai.contract.model.DropdownItem;
import in.lekhai.core.account_master.domain.AccountGroup;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
import in.lekhai.error.controller.account.exception.ParentNotFoundException;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.springframework.stereotype.Service;

import java.util.List;

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
                .findById(request.getParentId())
                .orElseThrow(() -> new ParentNotFoundException(request.getParentId()));

        AccountGroup createSubGroupRequest = new AccountGroup(
                request.getName(),
                accountGroup.getId(),
                accountGroup.getNature(),
                accountGroup.getBehaviour(),
                Boolean.FALSE
        );

        AccountGroup subGroup = accountGroupRepository.save(createSubGroupRequest);
        return new AccountGroupResponse()
                .id(subGroup.getId())
                .name(subGroup.getName());
    }

    @ShopContextTransactional
    public List<DropdownItem> listAccountGroups() {
        return accountGroupRepository.findAll()
                .stream()
                .map(group -> new DropdownItem().id(group.getId()).label(group.getName()))
                .toList();
    }
}
