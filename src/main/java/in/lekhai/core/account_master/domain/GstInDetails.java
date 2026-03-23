package in.lekhai.core.account_master.domain;

import in.lekhai.contract.model.PartyType;
import in.lekhai.contract.model.RegistrationType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("gst_details")
public class GstInDetails {
    @Id
    private Long id;
    private Long ledgerId;
    private RegistrationType registrationType;
    private Boolean isEcommerceOperator;
    private String gstinOrUin;
    private PartyType partyType;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public GstInDetails(
            Long ledgerId,
            RegistrationType registrationType,
            Boolean isEcommerceOperator,
            String gstinOrUin,
            PartyType partyType) {
        this.ledgerId = ledgerId;
        this.registrationType = registrationType;
        this.isEcommerceOperator = isEcommerceOperator;
        this.gstinOrUin = gstinOrUin;
        this.partyType = partyType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(Long ledgerId) {
        this.ledgerId = ledgerId;
    }

    public RegistrationType getRegistrationType() {
        return registrationType;
    }

    public void setRegistrationType(RegistrationType registrationType) {
        this.registrationType = registrationType;
    }

    public Boolean getIsEcommerceOperator() {
        return isEcommerceOperator;
    }

    public void setIsEcommerceOperator(Boolean ecommerceOperator) {
        isEcommerceOperator = ecommerceOperator;
    }

    public String getGstinOrUin() {
        return gstinOrUin;
    }

    public void setGstinOrUin(String gstinOrUin) {
        this.gstinOrUin = gstinOrUin;
    }

    public PartyType getPartyType() {
        return partyType;
    }

    public void setPartyType(PartyType partyType) {
        this.partyType = partyType;
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
