package in.lekhai.gsp.ewb.domain.model;

import in.lekhai.gsp.ewb.domain.enums.TransportMode;

import java.time.LocalDate;
import java.util.List;

public record EwbDetails(
        String ewbNo,
        Integer fromPinCode,
        String addressLine1,
        String addressLine2,
        List<EwbVehicleDetails> ewbVehicleDetails
){
    public record EwbVehicleDetails(
            String vehicleNo,
            String fromPlace,
            Integer fromState,
            String transportDocumentNo,
            LocalDate transportDocumentDate,
            TransportMode transportMode
    ){ }
}
