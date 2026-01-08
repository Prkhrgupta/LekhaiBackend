//package in.lekhai.core.domain.roles;
//
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.relational.core.mapping.Table;
//
//import java.time.LocalDateTime;
//
//@Table("roles")
//public class Roles {
//    @Id
//    private Integer id;
//    private String roleName;
//    private String description;
//    private String acronym;
//    private Boolean isSystemRole = Boolean.FALSE; // Only TRUE for ADMIN right now
//    private Boolean isActive = Boolean.TRUE;
//    @CreatedDate
//    private LocalDateTime createdAt;
//    @LastModifiedDate
//    private LocalDateTime updatedAt;
//
//    public Roles() { }
//
//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//
//    public String getRoleName() {
//        return roleName;
//    }
//
//    public void setRoleName(String roleName) {
//        this.roleName = roleName;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public Boolean getSystemRole() {
//        return isSystemRole;
//    }
//
//    public void setSystemRole(Boolean systemRole) {
//        isSystemRole = systemRole;
//    }
//
//    public Boolean getActive() {
//        return isActive;
//    }
//
//    public void setActive(Boolean active) {
//        isActive = active;
//    }
//
//    public LocalDateTime getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(LocalDateTime createdAt) {
//        this.createdAt = createdAt;
//    }
//
//    public LocalDateTime getUpdatedAt() {
//        return updatedAt;
//    }
//
//    public void setUpdatedAt(LocalDateTime updatedAt) {
//        this.updatedAt = updatedAt;
//    }
//
//    public String getAcronym() {
//        return acronym;
//    }
//
//    public void setAcronym(String acronym) {
//        this.acronym = acronym;
//    }
//}
