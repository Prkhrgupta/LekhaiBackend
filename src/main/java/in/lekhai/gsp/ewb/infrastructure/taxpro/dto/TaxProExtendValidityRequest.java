package in.lekhai.gsp.ewb.infrastructure.taxpro.dto;

public record TaxProExtendValidityRequest(
        Long ewbNo,
        String vehicleNo,
        String fromPlace,
        Integer fromState,
        Integer remainingDistance,
        String transDocNo,
        String transDocDate,
        String transMode,
        Integer extnRsnCode,
        String extnRemarks,
        Integer fromPincode,
        String consignmentStatus,
        String transitType,
        String addressLine1,
        String addressLine2,
        String addressLine3
) {}