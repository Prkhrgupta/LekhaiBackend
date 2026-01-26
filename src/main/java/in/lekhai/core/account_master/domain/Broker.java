package in.lekhai.core.account_master.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("broker")
public class Broker {
    @Id
    private Long id;
    private String name;
    private String phone;
    private Integer shopCode;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Broker() {
    }

    public Broker(String name, String phone, Integer shopCode) {
        this.name = name;
        this.phone = phone;
        this.shopCode = shopCode;
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
