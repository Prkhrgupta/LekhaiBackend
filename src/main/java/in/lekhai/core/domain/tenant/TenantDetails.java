package in.lekhai.core.domain.tenant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("tenant_details")
public class TenantDetails {
    @Id
    private Long id;
    private String uuid;
    private Long categoryId;
    private Boolean isDefault;
    private String gstIn;
    private String firmName;
    private String registeredAddress;
    private Integer tenant;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public TenantDetails() { }

    public TenantDetails(String uuid,
                         Long categoryId,
                         Boolean isDefault,
                         String gstIn,
                         String firmName,
                         String registeredAddress,
                         Integer tenant
    ) {
        this.uuid = uuid;
        this.categoryId = categoryId;
        this.isDefault = isDefault;
        this.gstIn = gstIn;
        this.firmName = firmName;
        this.registeredAddress = registeredAddress;
        this.tenant = tenant;
    }

    public Long getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public String getGstIn() {
        return gstIn;
    }

    public String getFirmName() {
        return firmName;
    }

    public String getRegisteredAddress() {
        return registeredAddress;
    }

    public Integer getTenant() {
        return tenant;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
