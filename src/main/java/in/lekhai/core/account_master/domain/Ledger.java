package in.lekhai.core.account_master.domain;

import in.lekhai.common.AccountEntryType;
import in.lekhai.common.domain.ShopAwareEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("ledger")
public class Ledger {
    @Id
    private Long id;
    private String name;
    private String legalName;
    private Long accountGroupId;
    private BigDecimal openingBalance;
    private AccountEntryType openingBalanceType;
    private BigDecimal creditLimit;
    private Long defaultAreaId;
    private Long defaultBrokerId;
    private Long defaultTransportId;
    private String pan;
    private String aadhaar;
    private String tan;
    private String email;
    private String msme;
    private String contactPerson;
    private Long phoneNumber;
    private String gstInNumber;
    private String location;
    private Boolean isActive;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Ledger(
            String name,
            String legalName,
            Long accountGroupId,
            BigDecimal openingBalance,
            AccountEntryType openingBalanceType,
            BigDecimal creditLimit,
            Long defaultAreaId,
            Long defaultBrokerId,
            Long defaultTransportId,
            String pan,
            String aadhaar,
            String tan,
            String email,
            String msme,
            String contactPerson,
            Long phoneNumber,
            String gstInNumber,
            String location) {
        this.name = name;
        this.legalName = legalName;
        this.accountGroupId = accountGroupId;
        this.openingBalance = openingBalance;
        this.openingBalanceType = openingBalanceType;
        this.creditLimit = creditLimit;
        this.defaultAreaId = defaultAreaId;
        this.defaultBrokerId = defaultBrokerId;
        this.defaultTransportId = defaultTransportId;
        this.pan = pan;
        this.aadhaar = aadhaar;
        this.tan = tan;
        this.email = email;
        this.msme = msme;
        this.contactPerson = contactPerson;
        this.phoneNumber = phoneNumber;
        this.gstInNumber = gstInNumber;
        this.location = location;
        this.isActive = Boolean.TRUE;
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

    public AccountEntryType getOpeningBalanceType() {
        return openingBalanceType;
    }

    public void setOpeningBalanceType(AccountEntryType openingBalanceType) {
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

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGstInNumber() {
        return gstInNumber;
    }

    public void setGstInNumber(String gstInNumber) {
        this.gstInNumber = gstInNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
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
