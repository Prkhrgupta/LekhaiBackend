package in.lekhai.core.domain.category;

import in.lekhai.core.enums.Roles;
import jdk.jfr.Description;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
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
@Table("role_permissions")
@Description("What permission does a role have for a category" +
        "For Saree Category ADMIN has XXXXXXX" +
        "For Gold Category STAFF has YYYYYYY")
public class RolePermissions {
    @Id
    private Long id;
    private Roles role;
    private Integer categoryId; // FK for CategoryMaster
    @Builder.Default
    private List<Long> permissions = new ArrayList<>();
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
