package in.lekhai.core.domain.users;

import in.lekhai.core.enums.Roles;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table("users")
public class Users {
    @Id
    private Long id;
    private String uuid;
    private String fullName;
    private Roles role;
    private Integer categoryId;
    private Integer shopCode; // This is also the default shop this user will log in with
    private Boolean isSuperAdmin = Boolean.FALSE;
    private Boolean isActive = Boolean.TRUE;
    private List<Long> permissions = new ArrayList<>();
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Users() { }

    public static Users createDefaultUsers(String uuid,
                                           String fullName,
                                           Roles role,
                                           Integer shopCode
    ) {
        Users users = new Users();
        users.setUuid(uuid);
        users.setFullName(fullName);
        users.setRole(role);
        users.setShopCode(shopCode);
        return users;
    }

    public static Users createUserWithPermissions(String uuid,
                                                  String fullName,
                                                  Roles role,
                                                  Integer shopCode,
                                                  List<Long> permissions
    ) {
        Users users = Users.createDefaultUsers(uuid, fullName, role, shopCode);
        users.setPermissions(permissions);
        return users;
    }

    public static Users createUserWithCategory(String uuid,
                                                  String fullName,
                                                  Roles role,
                                                  Integer shopCode,
                                                  Integer categoryId
    ) {
        Users users = Users.createDefaultUsers(uuid, fullName, role, shopCode);
        users.setCategoryId(categoryId);
        return users;
    }

    public static Users createSuperAdminUser(String uuid,
                                             String fullName,
                                             Roles role,
                                             Integer shopCode
    ) {
        Users users = Users.createDefaultUsers(uuid, fullName, role, shopCode);
        users.setSuperAdmin(Boolean.TRUE);
        return users;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Boolean getSuperAdmin() {
        return isSuperAdmin;
    }

    public void setSuperAdmin(Boolean superAdmin) {
        isSuperAdmin = superAdmin;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getShopCode() {
        return shopCode;
    }

    public void setShopCode(Integer shopCode) {
        this.shopCode = shopCode;
    }

    public List<Long> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Long> permissions) {
        this.permissions = permissions;
    }
}
