package in.lekhai.core.domain.category;

import in.lekhai.core.enums.Roles;
import jdk.jfr.Description;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table("role_permissions")
@Description("What permission does a role have for a category" +
        "For Saree Category ADMIN has XXXXXXX" +
        "For Gold Category STAFF has YYYYYYY")
public class RolePermissions {
    @Id
    private Long id;
    private Roles role;
    private Integer categoryId; // FK for CategoryMaster
    private List<Long> permissions = new ArrayList<>();
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public RolePermissions() {
    }

    public RolePermissions(
            Roles role,
            Integer categoryId,
            List<Long> permissions
    ) {
        this.role = role;
        this.categoryId = categoryId;
        this.permissions = permissions;
    }

    public RolePermissions(
            Roles role,
            Integer categoryId
    ) {
        this.role = role;
        this.categoryId = categoryId;
    }

    public Long getId() {
        return this.id;
    }

    public Roles getRole() {
        return this.role;
    }

    public Integer getCategoryId() {
        return this.categoryId;
    }

    public List<Long> getPermissions() {
        return this.permissions;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public void setPermissions(List<Long> permissions) {
        this.permissions = permissions;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
