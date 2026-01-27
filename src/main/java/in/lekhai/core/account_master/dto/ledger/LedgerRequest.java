package in.lekhai.core.account_master.dto.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record LedgerRequest(
        @JsonProperty("gst_in_number")
        String gstInNumber,
        @NotBlank String name,
        @JsonProperty("account_group")
        long accountGroup,
        @JsonProperty("opening_balance")
        BigDecimal openingBalance,
        @JsonProperty("account_entry_type")
        AccountEntryType accountEntryType,
        @JsonProperty("legal_name")
        String legalName,
        String Location,
        @JsonProperty("country_name")
        String countryName,
        @JsonProperty("state_and_code")
        String stateAndCode,
        @JsonProperty("mail_to")
        MailTo mailTo,

        long area,
        @JsonProperty("contact_person")
        String contactPerson,
        @JsonProperty("phone_number")
        Integer phoneNumber,
        String pan,
        @JsonProperty("credit_limit")
        BigDecimal creditLimit,
        long transport,
        @JsonProperty("tan_number")
        String tanNumber,
        long broker,

        boolean gstInDetailsPresent,
        @JsonProperty("gst_in_details")
        GstInDetail gstInDetails,
        @JsonProperty("aadhaar_number")
        Integer aadhaarNumber,
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
            Integer gstInUin,
            @JsonProperty("party_type")
            GstInRegistrationType.PartyType partyType
    ) {}
}
