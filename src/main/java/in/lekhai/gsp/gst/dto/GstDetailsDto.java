package in.lekhai.gsp.gst.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GstDetailsDto(
        @JsonProperty("Gstin")
        String gstin,

        @JsonProperty("TradeName")
        String tradeName,

        @JsonProperty("LegalName")
        String legalName,

        @JsonProperty("AddrBnm")
        String buildingName,

        @JsonProperty("AddrBno")
        String buildingNumber,

        @JsonProperty("AddrFlno")
        String floorNumber,

        @JsonProperty("AddrSt")
        String street,

        @JsonProperty("AddrLoc")
        String location,

        @JsonProperty("StateCode")
        Integer stateCode,

        @JsonProperty("AddrPncd")
        Integer pincode,

        @JsonProperty("TxpType")
        String taxpayerType,

        @JsonProperty("Status")
        String registrationStatus,

        @JsonProperty("BlkStatus")
        String blockStatus,

        @JsonProperty("DtReg")
        String registrationDate,

        @JsonProperty("DtDReg")
        String deregistrationDate

) {
}