package in.lekhai.gsp.gst.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GstVerificationResponseDto(
        @JsonProperty("Status")
        String status,

        @JsonProperty("Data")
        GstDetailsDto data,

        @JsonProperty("ErrorDetails")
        Object errorDetails,

        @JsonProperty("InfoDtls")
        Object infoDtls

) {
}