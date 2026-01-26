package in.lekhai.core.account_master.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("ledger")
public class Ledger {
    @Id
    private Long id;
    private String name;
    private String legalName;
    private Long accountGroupId;
    private BigDecimal openingBalance = BigDecimal.ZERO;
    private OpeningBalanceType openingBalanceType;
    private BigDecimal creditLimit;
    private Long defaultAreaId;
    private Long defaultBrokerId;
    private Long defaultTransportId;
    private String pan;
    private String aadhaar;
    private String tan;
    private String email;
    private String msme;
    private Boolean isActive = true;
    private Integer shopCode;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Ledger() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public Long getAccountGroupId() {
        return accountGroupId;
    }

    public void setAccountGroupId(Long accountGroupId) {
        this.accountGroupId = accountGroupId;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public OpeningBalanceType getOpeningBalanceType() {
        return openingBalanceType;
    }

    public void setOpeningBalanceType(OpeningBalanceType openingBalanceType) {
        this.openingBalanceType = openingBalanceType;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public Long getDefaultAreaId() {
        return defaultAreaId;
    }

    public void setDefaultAreaId(Long defaultAreaId) {
        this.defaultAreaId = defaultAreaId;
    }

    public Long getDefaultBrokerId() {
        return defaultBrokerId;
    }

    public void setDefaultBrokerId(Long defaultBrokerId) {
        this.defaultBrokerId = defaultBrokerId;
    }

    public Long getDefaultTransportId() {
        return defaultTransportId;
    }

    public void setDefaultTransportId(Long defaultTransportId) {
        this.defaultTransportId = defaultTransportId;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getAadhaar() {
        return aadhaar;
    }

    public void setAadhaar(String aadhaar) {
        this.aadhaar = aadhaar;
    }

    public String getTan() {
        return tan;
    }

    public void setTan(String tan) {
        this.tan = tan;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMsme() {
        return msme;
    }

    public void setMsme(String msme) {
        this.msme = msme;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Integer getShopCode() {
        return shopCode;
    }

    public void setShopCode(Integer shopCode) {
        this.shopCode = shopCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public enum OpeningBalanceType {
        DR, CR
    }
}
