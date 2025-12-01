package in.lekhai.authentication.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table("user_credentials")
public class UserCredentials {

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
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("is_account_active")
    private boolean isAccountActive;

}
