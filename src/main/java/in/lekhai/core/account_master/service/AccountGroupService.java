package in.lekhai.core.account_master.service;

import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.domain.AccountGroup;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
import in.lekhai.error.controller.account.exception.ParentNotFoundException;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    @ShopContextTransactional
    public AccountGroupSummaryPageResponse listAccountGroupSummaries(AccountGroupSearchableField searchableField, String query, Pageable pageable) {
        Page<AccountGroup> accountGroupsPage;
        if (query != null && !query.trim().isEmpty() && searchableField == AccountGroupSearchableField.NAME) {
            List<AccountGroup> accountGroups = accountGroupRepository.findByNameContainingIgnoreCase(query.trim(), pageable);
            long total = accountGroupRepository.countByNameContainingIgnoreCase(query.trim());
            accountGroupsPage = new PageImpl<>(accountGroups, pageable, total);
        } else {
            accountGroupsPage = accountGroupRepository.findAll(pageable);
        }

        List<AccountGroupSummaryItem> data = accountGroupsPage.getContent().stream()
                .map(group -> new AccountGroupSummaryItem()
                        .id(group.getId())
                        .name(group.getName()))
                .toList();

        return new AccountGroupSummaryPageResponse()
                .data(data)
                .pagination(new PaginationMeta()
                        .page(accountGroupsPage.getNumber())
                        .size(accountGroupsPage.getSize())
                        .totalElements(accountGroupsPage.getTotalElements())
                        .totalPages(accountGroupsPage.getTotalPages()));
    }
}
