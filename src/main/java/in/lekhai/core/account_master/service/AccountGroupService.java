package in.lekhai.core.account_master.service;

import in.lekhai.core.account_master.domain.AccountGroup;
import in.lekhai.core.account_master.dto.AccountGroupRequest;
import in.lekhai.core.account_master.dto.AccountGroupResponse;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class AccountGroupService {

    private final AccountGroupRepository accountGroupRepository;

    public AccountGroupService(AccountGroupRepository accountGroupRepository) {
        this.accountGroupRepository = accountGroupRepository;
    }

    public AccountGroupResponse createAccountGroup(AccountGroupRequest request) {
        AccountGroup accountGroup = new AccountGroup(
                request.name(),
                request.parentId(),
                request.nature(),
                request.behaviour(),
                request.isPrimary());
        AccountGroup saved = accountGroupRepository.save(accountGroup);
        return mapToResponse(saved);
    }

    public List<AccountGroupResponse> listAccountGroups() {
        return StreamSupport.stream(accountGroupRepository.findAll().spliterator(), false)
                .map(this::mapToResponse)
                .toList();
    }

    private AccountGroupResponse mapToResponse(AccountGroup accountGroup) {
        return new AccountGroupResponse(
                accountGroup.getId(),
                accountGroup.getName(),
                accountGroup.getParentId(),
                accountGroup.getNature(),
                accountGroup.getBehaviour(),
                accountGroup.getPrimary(),
                accountGroup.getShopCode(),
                accountGroup.getCreatedAt());
    }
}
