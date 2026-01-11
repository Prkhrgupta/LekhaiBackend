package in.lekhai.authentication.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Table("user_accounts")
public class UserAccounts implements UserDetails {

    @Id
    @Column("id")
    private Long Id;

    @Column("pass_hash")
    private String passHash;

    @Column("username")
    private String username;

    @Column("uuid")
    private String uuid;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Boolean isActive = Boolean.TRUE;

    public UserAccounts() {
    }

    public UserAccounts(Long Id, String passHash, String username, String uuid, LocalDateTime createdAt,
            LocalDateTime updatedAt, Boolean isActive) {
        this.Id = Id;
        this.passHash = passHash;
        this.username = username;
        this.uuid = uuid;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isActive = isActive;
    }

    public static UserAccountsBuilder builder() {
        return new UserAccountsBuilder();
    }

    public Long getId() {
        return this.Id;
    }

    public String getPassHash() {
        return this.passHash;
    }

    public String getUsername() {
        return this.username;
    }

    public String getUuid() {
        return this.uuid;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public void setId(Long Id) {
        this.Id = Id;
    }

    public void setPassHash(String passHash) {
        this.passHash = passHash;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return this.passHash;
    }

    @Override
    public String toString() {
        return "UserAccounts(Id=" + this.getId() + ", passHash=" + this.getPassHash() + ", username="
                + this.getUsername() + ", uuid=" + this.getUuid() + ", createdAt=" + this.getCreatedAt()
                + ", updatedAt=" + this.getUpdatedAt() + ", isActive=" + this.getIsActive() + ")";
    }

    public static class UserAccountsBuilder {
        private Long Id;
        private String passHash;
        private String username;
        private String uuid;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Boolean isActive = Boolean.TRUE;

        UserAccountsBuilder() {
        }

        public UserAccountsBuilder Id(Long Id) {
            this.Id = Id;
            return this;
        }

        public UserAccountsBuilder passHash(String passHash) {
            this.passHash = passHash;
            return this;
        }

        public UserAccountsBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserAccountsBuilder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public UserAccountsBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserAccountsBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserAccountsBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public UserAccounts build() {
            return new UserAccounts(Id, passHash, username, uuid, createdAt, updatedAt, isActive);
        }

        public String toString() {
            return "UserAccounts.UserAccountsBuilder(Id=" + this.Id + ", passHash=" + this.passHash + ", username="
                    + this.username + ", uuid=" + this.uuid + ", createdAt=" + this.createdAt + ", updatedAt="
                    + this.updatedAt + ", isActive=" + this.isActive + ")";
        }
    }
}
