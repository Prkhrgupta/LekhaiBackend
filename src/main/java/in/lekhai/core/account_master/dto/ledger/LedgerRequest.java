package in.lekhai.core.account_master.dto.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import in.lekhai.common.AccountEntryType;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record LedgerRequest(
        @JsonProperty("gst_in_number")
        String gstInNumber,
        @NotBlank String name,
        @JsonProperty("account_group")
        Long accountGroup,
        @JsonProperty("opening_balance")
        BigDecimal openingBalance,
        @JsonProperty("account_entry_type")
        AccountEntryType accountEntryType,
        @JsonProperty("legal_name")
        String legalName,
        String location,
        @JsonProperty("country_name")
        String countryName,
        @JsonProperty("state_and_code")
        String stateAndCode,
        @JsonProperty("mail_to")
        MailTo mailTo,

        @JsonProperty("area_id")
        Long areaId,
        @JsonProperty("contact_person")
        String contactPerson,
        @JsonProperty("phone_number")
        Long phoneNumber,
        String pan,
        @JsonProperty("credit_limit")
        BigDecimal creditLimit,
        @JsonProperty("transport_id")
        Long transportId,
        @JsonProperty("tan_number")
        String tanNumber,
        @JsonProperty("broker_id")
        Long brokerId,

        boolean gstInDetailsPresent,
        @JsonProperty("gst_in_details")
        GstInDetail gstInDetails,
        @JsonProperty("aadhaar_number")
        String aadhaarNumber,
        @JsonProperty("image_uploaded")
        boolean imageUploaded,
        String email,
        @JsonProperty("msme_number")
        String msmeNumber,
        int distance        // calculate
) {
    public record MailTo(
            @JsonProperty("mail_to_line_1")
            String lineOne,
            @JsonProperty("mail_to_line_2")
            String lineTwo,
            @JsonProperty("mail_to_line_3")
            String lineThree,
            @JsonProperty("mail_to_line_4")
            String lineFour
            ) {}

    public record GstInDetail(
            @JsonProperty("registration_type")
            GstInRegistrationType.RegistrationType registrationType,
            @JsonProperty("is_e_commerce_operator")
            boolean isEcommerceOperator,
            @JsonProperty("gst_in_uin")
            String gstInUin,
            @JsonProperty("party_type")
            GstInRegistrationType.PartyType partyType
    ) {}
}
