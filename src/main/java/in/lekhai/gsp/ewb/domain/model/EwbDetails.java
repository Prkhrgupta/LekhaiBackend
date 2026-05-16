package in.lekhai.gsp.ewb.domain.model;

import in.lekhai.gsp.ewb.domain.enums.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record EwbDetails(
        Long ewbNo,
        Instant ewbDate,
        EwbStatus status,
        String genMode,
        String generatorGstin,
        String consigner,
        String fromGstin,
        String fromPlace,
        Integer fromState,
        Integer fromPinCode,
        String addressLine1,
        String addressLine2,
        String consignee,
        String toGstin,
        String toPlace,
        Integer toState,
        Integer toPinCode,
        String toAddressLine1,
        String toAddressLine2,
        DocumentType documentType,
        String documentNumber,
        LocalDate documentDate,
        SupplyType supplyType,
        SubSupplyType subSupplyType,
        TransactionType transactionType,
        BigDecimal totalValue,
        BigDecimal totalInvoiceValue,
        BigDecimal cgstValue,
        BigDecimal sgstValue,
        BigDecimal igstValue,
        BigDecimal cessValue,
        BigDecimal otherValue,
        BigDecimal cessNonAdvolValue,
        EwbVehicleType vehicleType,
        String transporterGstin,
        String transporterName,
        Integer noOfValidDDays,
        Instant validUpto,
        Integer actualDistance,
        Integer actualFromStateCode,
        Integer actualToStateCode,
        Integer extendedTimes,
        String rejectStatus,
        List<EwbVehicleDetails> ewbVehicleDetails
){
    public record EwbVehicleDetails(
            String updateMode,
            String vehicleNo,
            String fromPlace,
            Integer fromState,
            Long tripSheetNumber,
            String transporterGstin,
            Instant enteredDate,
            TransportMode transportMode,
            String transportDocumentNo,
            LocalDate transportDocumentDate,
            String groupNumber
    ){ }
}
