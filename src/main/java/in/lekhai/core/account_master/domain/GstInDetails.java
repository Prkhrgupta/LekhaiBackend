package in.lekhai.core.account_master.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("gst_details")
public class GstInDetails {
    @Id
    private Long id;
    private Long ledgerId;
    private RegistrationType registrationType;
    private Boolean isEcommerceOperator;
    private String gstInOrUin;
    private PartyType partyType;

    public GstInDetails(
            Long ledgerId,
            RegistrationType registrationType,
            Boolean isEcommerceOperator,
            String gstInOrUin,
            PartyType partyType
    ) {
        this.ledgerId = ledgerId;
        this.registrationType = registrationType;
        this.isEcommerceOperator = isEcommerceOperator;
        this.gstInOrUin = gstInOrUin;
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

    public String getGstInOrUin() {
        return gstInOrUin;
    }

    public void setGstInOrUin(String gstInOrUin) {
        this.gstInOrUin = gstInOrUin;
    }

    public PartyType getPartyType() {
        return partyType;
    }

    public void setPartyType(PartyType partyType) {
        this.partyType = partyType;
    }

    public enum RegistrationType {
        REGULAR,
        COMPOSITION,
        UNREGISTERED,
        UIN
    }

    public enum PartyType {
        SEZ,
        NOT_APPLICABLE,
        DEEMED_EXPORT,
        GOVERNMENT_ENTITY
    }
}
