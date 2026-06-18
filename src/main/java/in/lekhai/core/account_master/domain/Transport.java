package in.lekhai.core.account_master.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("transport")
public class Transport {
    @Id
    private Long id;
    private String name;
    private String phone;
    private String gstNo;
    private Integer sitswiftCode;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Transport() {
    }

    public Transport(String name, String gstNo, Integer sitswiftCode) {
        this.name = name;
        this.gstNo = gstNo;
        this.sitswiftCode = sitswiftCode;
    }

    public Transport(String name, String phone, String gstNo) {
        this.name = name;
        this.phone = phone;
        this.gstNo = gstNo;
    }

    public Transport(Long id, String name, String phone, String gstNo) {
        this.id = id;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGstNo() {
        return gstNo;
    }

    public void setGstNo(String gstNo) {
        this.gstNo = gstNo;
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
}
