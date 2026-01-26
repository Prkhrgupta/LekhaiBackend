package in.lekhai.core.account_master.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("transport")
public class Transport {
    @Id
    private Long id;
    private String name;
    private String phone;
    private String gstNo;
    private Integer shopCode;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Transport() {
    }

    public Transport(String name, String phone, String gstNo) {
        this.name = name;
        this.phone = phone;
        this.gstNo = gstNo;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getGstNo() {
        return gstNo;
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
