package in.lekhai.core.domain.shop;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("shops")
public class Shops {
    @Id
    private Long id;
    private Integer categoryId;
    private Boolean isActive;
    private String gstNumber;
    private String firmName;
    private String registeredAddress;
    private Integer shopCode;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Shops() { }

    public Shops(Integer categoryId,
                 Boolean isActive,
                 String gstNumber,
                 String firmName,
                 String registeredAddress,
                 Integer shopCode
    ) {
        this.categoryId = categoryId;
        this.isActive = isActive;
        this.gstNumber = gstNumber;
        this.firmName = firmName;
        this.registeredAddress = registeredAddress;
        this.shopCode = shopCode;
    }

    public Long getId() {
        return id;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public Boolean getDefault() {
        return isActive;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public String getFirmName() {
        return firmName;
    }

    public String getRegisteredAddress() {
        return registeredAddress;
    }

    public Integer getShopCode() {
        return shopCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
