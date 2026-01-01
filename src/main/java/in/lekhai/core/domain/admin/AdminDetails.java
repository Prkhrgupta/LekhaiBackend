package in.lekhai.core.domain.admin;

import in.lekhai.core.dto.admin.BaseUserEntity;
import in.lekhai.core.enums.Roles;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;

@Table("admin_details")
public class AdminDetails implements BaseUserEntity {
    @Id
    private Long id;
    @NotNull
    private String uuid;
    private String name;
    private Long categoryId;
    private Integer tenant;
    private List<Long> permissionBit = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setTenant(Integer tenant) {
        this.tenant = tenant;
    }

    public void setPermissionBit(List<Long> permissionBit) {
        this.permissionBit = permissionBit;
    }

    @Override
    public Long getCategoryId() {
        return this.categoryId;
    }

    @Override
    public List<Long> getPermissionBit() {
        return this.permissionBit;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    @Override
    public Roles getRole() {
        return Roles.ADMIN;
    }

    @Override
    public Integer getTenant() {
        return this.tenant;
    }
}
