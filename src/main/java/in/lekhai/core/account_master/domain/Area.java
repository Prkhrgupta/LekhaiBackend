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
    private Integer sitswiftCode;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Area() {
    }

    public Area(String areaName, Integer sitswiftCode) {
        this.areaName = areaName;
        this.sitswiftCode = sitswiftCode;
    }

    public Area(String areaName) {
        this.areaName = areaName;
    }

    public Area(Long id, String areaName) {
        this.id = id;
        this.areaName = areaName;
    }

    public Long getId() {
        return id;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
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

    public Integer getSitswiftCode() {
        return sitswiftCode;
    }

    public void setSitswiftCode(Integer sitswiftCode) {
        this.sitswiftCode = sitswiftCode;
    }
}
