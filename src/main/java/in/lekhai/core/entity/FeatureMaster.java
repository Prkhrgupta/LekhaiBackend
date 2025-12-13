package in.lekhai.core.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Table(name = "features")
public class FeatureMaster {
    @Id
    Long id;

    String featureKey;

    @Column(value = "parent_id")
    Long parentFeatureId;

    String title;

    String icon;

    Long bitPosition;

    Long sortOrder;

    @Builder.Default
    Boolean isActive = Boolean.TRUE;

    @Builder.Default
    Boolean isDeleted = Boolean.FALSE;

    @CreatedDate
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;
}
