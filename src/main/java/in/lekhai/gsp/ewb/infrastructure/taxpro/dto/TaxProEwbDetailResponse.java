package in.lekhai.gsp.ewb.infrastructure.taxpro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import in.lekhai.gsp.ewb.domain.enums.EwbStatus;
import in.lekhai.gsp.ewb.domain.enums.EwbVehicleType;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaxProEwbDetailResponse(

        Long ewbNo,

        Integer fromPincode,

        String ewayBillDate,

        Integer fromStateCode,

        Integer toPincode,

        Integer toStateCode,

        Integer noValidDays,

        String toPlace,

        EwbStatus status,

        EwbVehicleType vehicleType,

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