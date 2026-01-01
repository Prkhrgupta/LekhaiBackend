package in.lekhai.core.domain.category;

import in.lekhai.core.enums.Roles;
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
@Table("role_category_master")
public class RoleCategoryMaster {
    @Id
    private Long id;
    private Roles role;
    private Long categoryId; // FK for CategoryMaster
    @Builder.Default
    private List<Long> permission = new ArrayList<>();
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
