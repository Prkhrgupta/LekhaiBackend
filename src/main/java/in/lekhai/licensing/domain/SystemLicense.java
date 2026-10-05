package in.lekhai.licensing.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("system_license")
public class SystemLicense {

    @Id
    private Long id;
    private String licenseId;
    private String licenseeName;
    private String adminUsername;
    private String rawPayload;
    private String signature;
    private Boolean isActive;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public SystemLicense() {
    }

    public SystemLicense(String licenseId, String licenseeName, String adminUsername, String rawPayload, String signature, Boolean isActive, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        this.licenseId = licenseId;
        this.licenseeName = licenseeName;
        this.adminUsername = adminUsername;
        this.rawPayload = rawPayload;
        this.signature = signature;
        this.isActive = isActive;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(String licenseId) {
        this.licenseId = licenseId;
    }

    public String getLicenseeName() {
        return licenseeName;
    }

    public void setLicenseeName(String licenseeName) {
        this.licenseeName = licenseeName;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
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
}
