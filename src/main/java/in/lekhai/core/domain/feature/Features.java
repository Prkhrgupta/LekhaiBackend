package in.lekhai.core.domain.feature;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "features")
public class Features {
    @Id
    Long id;

    String featureKey;

    @Column(value = "parent_id")
    Long parentFeatureId;

    String title;

    String icon;

    String route;

    Integer bitPosition;

    Long displayOrder;

    Boolean isActive = Boolean.TRUE;

    Boolean isDeleted = Boolean.FALSE;

    @CreatedDate
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;

    public Features() {
    }

    public Features(Long id, String featureKey, Long parentFeatureId, String title, String icon, String route,
            Integer bitPosition, Long displayOrder, Boolean isActive, Boolean isDeleted, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.featureKey = featureKey;
        this.parentFeatureId = parentFeatureId;
        this.title = title;
        this.icon = icon;
        this.route = route;
        this.bitPosition = bitPosition;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static FeaturesBuilder builder() {
        return new FeaturesBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getFeatureKey() {
        return this.featureKey;
    }

    public Long getParentFeatureId() {
        return this.parentFeatureId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getIcon() {
        return this.icon;
    }

    public String getRoute() {
        return this.route;
    }

    public Integer getBitPosition() {
        return this.bitPosition;
    }

    public Long getDisplayOrder() {
        return this.displayOrder;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public Boolean getIsDeleted() {
        return this.isDeleted;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFeatureKey(String featureKey) {
        this.featureKey = featureKey;
    }

    public void setParentFeatureId(Long parentFeatureId) {
        this.parentFeatureId = parentFeatureId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setBitPosition(Integer bitPosition) {
        this.bitPosition = bitPosition;
    }

    public void setDisplayOrder(Long displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String toString() {
        return "Features(id=" + this.getId() + ", featureKey=" + this.getFeatureKey() + ", parentFeatureId="
                + this.getParentFeatureId() + ", title=" + this.getTitle() + ", icon=" + this.getIcon() + ", route="
                + this.getRoute() + ", bitPosition=" + this.getBitPosition() + ", displayOrder="
                + this.getDisplayOrder() + ", isActive=" + this.getIsActive() + ", isDeleted=" + this.getIsDeleted()
                + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
    }

    public static class FeaturesBuilder {
        private Long id;
        private String featureKey;
        private Long parentFeatureId;
        private String title;
        private String icon;
        private String route;
        private Integer bitPosition;
        private Long displayOrder;
        private Boolean isActive = Boolean.TRUE;
        private Boolean isDeleted = Boolean.FALSE;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        FeaturesBuilder() {
        }

        public FeaturesBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public FeaturesBuilder featureKey(String featureKey) {
            this.featureKey = featureKey;
            return this;
        }

        public FeaturesBuilder parentFeatureId(Long parentFeatureId) {
            this.parentFeatureId = parentFeatureId;
            return this;
        }

        public FeaturesBuilder title(String title) {
            this.title = title;
            return this;
        }

        public FeaturesBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public FeaturesBuilder route(String route) {
            this.route = route;
            return this;
        }

        public FeaturesBuilder bitPosition(Integer bitPosition) {
            this.bitPosition = bitPosition;
            return this;
        }

        public FeaturesBuilder displayOrder(Long displayOrder) {
            this.displayOrder = displayOrder;
            return this;
        }

        public FeaturesBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public FeaturesBuilder isDeleted(Boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public FeaturesBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public FeaturesBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Features build() {
            return new Features(id, featureKey, parentFeatureId, title, icon, route, bitPosition, displayOrder,
                    isActive, isDeleted, createdAt, updatedAt);
        }

        public String toString() {
            return "Features.FeaturesBuilder(id=" + this.id + ", featureKey=" + this.featureKey + ", parentFeatureId="
                    + this.parentFeatureId + ", title=" + this.title + ", icon=" + this.icon + ", route=" + this.route
                    + ", bitPosition=" + this.bitPosition + ", displayOrder=" + this.displayOrder + ", isActive="
                    + this.isActive + ", isDeleted=" + this.isDeleted + ", createdAt=" + this.createdAt + ", updatedAt="
                    + this.updatedAt + ")";
        }
    }
}
