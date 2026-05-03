package in.lekhai.gsp.ewb.domain.model;

import in.lekhai.contract.model.VehicleType;
import in.lekhai.gsp.ewb.domain.enums.EwbStatus;
import in.lekhai.gsp.ewb.domain.enums.TransportMode;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record EwbDetails(
        Long ewbNo,
        Instant ewbDate,
        Integer fromPinCode,
        String toPlace,
        Integer toState,
        Integer toPinCode,
        VehicleType vehicleType,
        Integer noOfValidDDays,
        EwbStatus status,
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
