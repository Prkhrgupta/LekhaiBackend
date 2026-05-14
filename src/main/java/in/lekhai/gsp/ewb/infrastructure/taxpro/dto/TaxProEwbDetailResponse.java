package in.lekhai.gsp.ewb.infrastructure.taxpro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaxProEwbDetailResponse(

        @JsonProperty("ewbNo")
        Long ewbNo,

        @JsonProperty("ewayBillDate")
        String ewayBillDate,

        @JsonProperty("genMode")
        String genMode,

        @JsonProperty("userGstin")
        String userGstin,

        @JsonProperty("supplyType")
        String supplyType,

        @JsonProperty("subSupplyType")
        String subSupplyType,

        @JsonProperty("docType")
        String documentType,

        @JsonProperty("docNo")
        String documentNumber,

        @JsonProperty("docDate")
        String documentDate,

        @JsonProperty("fromGstin")
        String fromGstin,

        @JsonProperty("fromTrdName")
        String fromTradeName,

        @JsonProperty("fromAddr1")
        String fromAddressLine1,

        @JsonProperty("fromAddr2")
        String fromAddressLine2,

        @JsonProperty("fromPlace")
        String fromPlace,

        @JsonProperty("fromPincode")
        Integer fromPincode,

        @JsonProperty("fromStateCode")
        Integer fromStateCode,

        @JsonProperty("toGstin")
        String toGstin,

        @JsonProperty("toTrdName")
        String toTradeName,

        @JsonProperty("toAddr1")
        String toAddressLine1,

        @JsonProperty("toAddr2")
        String toAddressLine2,

        @JsonProperty("toPlace")
        String toPlace,

        @JsonProperty("toPincode")
        Integer toPincode,

        @JsonProperty("toStateCode")
        Integer toStateCode,

        @JsonProperty("totalValue")
        BigDecimal totalValue,

        @JsonProperty("totInvValue")
        BigDecimal totalInvoiceValue,

        @JsonProperty("cgstValue")
        BigDecimal cgstValue,

        @JsonProperty("sgstValue")
        BigDecimal sgstValue,

        @JsonProperty("igstValue")
        BigDecimal igstValue,

        @JsonProperty("cessValue")
        BigDecimal cessValue,

        @JsonProperty("otherValue")
        BigDecimal otherValue,

        @JsonProperty("cessNonAdvolValue")
        BigDecimal cessNonAdvolValue,

        @JsonProperty("transporterId")
        String transporterGstin,

        @JsonProperty("transporterName")
        String transporterName,

        @JsonProperty("status")
        String status,

        @JsonProperty("actualDist")
        Integer actualDistance,

        @JsonProperty("noValidDays")
        Integer validDays,

        @JsonProperty("validUpto")
        String validUpto,

        @JsonProperty("extendedTimes")
        Integer extendedTimes,

        @JsonProperty("rejectStatus")
        String rejectStatus,

        @JsonProperty("vehicleType")
        String vehicleType,

        @JsonProperty("actFromStateCode")
        Integer actualFromStateCode,

        @JsonProperty("actToStateCode")
        Integer actualToStateCode,

        @JsonProperty("transactionType")
        String transactionType,

        @JsonProperty("VehiclListDetails")
        List<VehicleDetail> vehicleDetails
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VehicleDetail(

            @JsonProperty("updMode")
            String updateMode,

            @JsonProperty("vehicleNo")
            String vehicleNumber,

            @JsonProperty("fromPlace")
            String fromPlace,

            @JsonProperty("fromState")
            Integer fromStateCode,

            @JsonProperty("tripshtNo")
            Long tripSheetNumber,

            @JsonProperty("userGSTINTransin")
            String transporterGstin,

            @JsonProperty("enteredDate")
            String enteredDate,

            @JsonProperty("transMode")
            String transportMode,

            @JsonProperty("transDocNo")
            String transportDocumentNumber,

            @JsonProperty("transDocDate")
            String transportDocumentDate,

            @JsonProperty("groupNo")
            String groupNumber
    ) {
    }
}
