package in.lekhai.core.account_master.domain;

import in.lekhai.common.domain.ShopAwareEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("account_group")
public class AccountGroup extends ShopAwareEntity {
    @Id
    private Long id;
    private String name;
    private Long parentId;
    private String nature;
    private String behaviour;
    private Boolean isPrimary;

    public AccountGroup() {
    }

    public AccountGroup(String name, Long parentId, String nature, String behaviour, Boolean isPrimary) {
        this.name = name;
        this.parentId = parentId;
        this.nature = nature;
        this.behaviour = behaviour;
        this.isPrimary = isPrimary;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getNature() {
        return nature;
    }

    public String getBehaviour() {
        return behaviour;
    }

    public Boolean getPrimary() {
        return isPrimary;
    }
}
