package in.lekhai.gsp.ewb.infrastructure.taxpro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaxProEwbDetailResponse(

        String ewbNo,

        Integer fromPincode,

        @JsonProperty("fromAddr1")
        String addressLine1,

        @JsonProperty("fromAddr2")
        String addressLine2,

        @JsonProperty("VehiclListDetails")
        List<VehicleDetail> vehicleDetails
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VehicleDetail(

            @JsonProperty("vehicleNo")
            String vehicleNo,

            String fromPlace,
            Integer fromState,

            @JsonProperty("transDocNo")
            String transDocNo,

            @JsonProperty("transDocDate")
            String transDocDate,

            @JsonProperty("transMode")
            String transMode
    ) {}
}