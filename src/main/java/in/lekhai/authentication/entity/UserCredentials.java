package in.lekhai.authentication.entity;

import lombok.*;
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

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table("user_credentials")
public class UserCredentials implements UserDetails {

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

    @Column("is_account_active")
    @Builder.Default
    private Boolean isAccountActive = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return this.passHash;
    }
}
