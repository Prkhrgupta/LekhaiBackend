package in.lekhai.core.domain.users;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("users")
public class Users {
    @Id
    private Long id;
    private String uuid;
    private String fullName;
    private Integer categoryId;
    private Boolean isSuperAdmin = Boolean.FALSE;
    private Boolean isActive = Boolean.TRUE;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Users() {
    }

    public static Users createDefaultUsers(String uuid,
            String fullName) {
        Users users = new Users();
        users.setUuid(uuid);
        users.setFullName(fullName);
        return users;
    }

    public static Users createUserWithCategory(String uuid,
            String fullName,
            Integer categoryId) {
        Users users = Users.createDefaultUsers(uuid, fullName);
        users.setCategoryId(categoryId);
        return users;
    }

    public static Users createSuperAdminUser(String uuid,
            String fullName) {
        Users users = Users.createDefaultUsers(uuid, fullName);
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
}
