package in.lekhai.core.entity;

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
@Table("tenant_details")
public class TenantDetails {
    @Id
    private Long id;
    private String uuid;
    private String role; // FIXME: Think if we want it here or not
    private Long category_id;
    @Builder.Default
    private List<Long> permissionBit = new ArrayList<>();
    @Builder.Default
    private List<Long> specialFeatureBits = new ArrayList<>();
    private String gstIn;
    private String firmName;
    private String registeredAddress;
    private Integer tenant;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
