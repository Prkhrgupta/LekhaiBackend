package in.lekhai.core.account_master.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("broker")
public class Broker {
    @Id
    private Long id;
    private String name;
    private String phone;
    private Integer sitswiftCode;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Broker() {
    }

    public Broker(String name, Integer sitswiftCode) {
        this.name = name;
        this.sitswiftCode = sitswiftCode;
    }

    public Broker(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public Broker(Long id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
