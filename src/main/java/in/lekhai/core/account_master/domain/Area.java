package in.lekhai.core.account_master.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("area")
public class Area {
    @Id
    private Long id;
    private String areaName;
    private String stateCode;
//    private Integer shopCode;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Area() {
    }

    public Area(String areaName, String stateCode) {
        this.areaName = areaName;
        this.stateCode = stateCode;
    }

    public Long getId() {
        return id;
    }

    public String getAreaName() {
        return areaName;
    }

    public String getStateCode() {
        return stateCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

//    public Integer getShopCode() {
//        return shopCode;
//    }
}
