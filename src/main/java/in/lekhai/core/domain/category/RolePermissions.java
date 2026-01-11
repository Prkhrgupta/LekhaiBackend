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

    public RolePermissions(Long id, Roles role, Integer categoryId, List<Long> permissions, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.role = role;
        this.categoryId = categoryId;
        this.permissions = permissions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RolePermissionsBuilder builder() {
        return new RolePermissionsBuilder();
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

    public static class RolePermissionsBuilder {
        private Long id;
        private Roles role;
        private Integer categoryId;
        private List<Long> permissions = new ArrayList<>();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        RolePermissionsBuilder() {
        }

        public RolePermissionsBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RolePermissionsBuilder role(Roles role) {
            this.role = role;
            return this;
        }

        public RolePermissionsBuilder categoryId(Integer categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public RolePermissionsBuilder permissions(List<Long> permissions) {
            this.permissions = permissions;
            return this;
        }

        public RolePermissionsBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public RolePermissionsBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public RolePermissions build() {
            return new RolePermissions(id, role, categoryId, permissions, createdAt, updatedAt);
        }

        public String toString() {
            return "RolePermissions.RolePermissionsBuilder(id=" + this.id + ", role=" + this.role + ", categoryId="
                    + this.categoryId + ", permissions=" + this.permissions + ", createdAt=" + this.createdAt
                    + ", updatedAt=" + this.updatedAt + ")";
        }
    }
}
