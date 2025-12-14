package in.lekhai.core.entity;


import in.lekhai.core.model.enums.Roles;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("user_details")
public class UserDetails {
    @Id
    private Long id;
    private String uuid;
    private Roles role;
    private Long category_id;
    @Builder.Default
    private List<Long> permissionBit = new ArrayList<>();
    private Integer tenant;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
