package in.lekhai.gsp.ewb.infrastructure.taxpro.utils;

import in.lekhai.gsp.ewb.domain.entity.EwbRecord;
import in.lekhai.gsp.ewb.domain.entity.EwbVehicleDetail;
import in.lekhai.gsp.ewb.domain.enums.ExtendValidityReason;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProExtendValidityRequest;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.format.DateTimeFormatter;

public final class TaxProPojoUtils {

    private final static DateTimeFormatter ddMMyyyy = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static @NonNull TaxProExtendValidityRequest createExtendValidityRequest(
            String ewbNo, Integer remainingDistance, ExtendValidityReason extensionReason, String extensionRemark,
            EwbVehicleDetail ewbVehicleDetail, EwbRecord ewbRecord, String transMode, String consignmentStatus
    ) {
        return new TaxProExtendValidityRequest(
                Long.parseLong(ewbNo),
                ewbVehicleDetail.getVehicleNumber(),
                ewbRecord.getFromPlace(),
                Integer.valueOf(ewbRecord.getFromStateCode().trim()),
                remainingDistance,
                ewbVehicleDetail.getTransportDocumentNumber(),
                ewbVehicleDetail.getTransportDocumentDate().format(ddMMyyyy),
                transMode,
                extensionReason.getReasonCode(),
                extensionRemark,
                ewbRecord.getFromPinCode(),
                consignmentStatus,
                transitTypeFromTranMode(transMode, "R"), // TODO: change this default from "R" to userInput
                consignmentStatus.equals("T") ? ewbRecord.getFromAddressLine1() : null,
                consignmentStatus.equals("T") ? ewbRecord.getFromAddressLine2() : null,
                null
        );
    }

    private static String transitTypeFromTranMode(String transMode, String transitTypeInput) {
        if ("5".equals(transMode)) {
            if (transitTypeInput == null) return "";
            if (transitTypeInput.equals("R") || transitTypeInput.equals("W") || transitTypeInput.equals("O")) {
                return transitTypeInput;
            }
            return "";
        }
        // for transMode 1–4 → must be blank
        return "";
    }
}
