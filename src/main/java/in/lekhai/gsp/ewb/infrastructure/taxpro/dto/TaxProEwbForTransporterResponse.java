package in.lekhai.gsp.ewb.infrastructure.taxpro.dto;

public record TaxProEwbForTransporterResponse(
        String ewbNo,
        String ewbDate,
        String status,
        String genGstin,
        String docNo,
        String docDate,
        Integer delPinCode,
        Integer delStateCode,
        String delPlace,
        String validUpto,
        Integer extendedTimes,
        String rejectStatus
){ }
