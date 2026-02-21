package in.lekhai.csv.upload.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransportDto {
    @JsonProperty("code")
    private Integer transportCode;

    @JsonProperty("name")
    private String transportName;

    @JsonProperty("add")
    private String transportAddress;

    @JsonProperty("gst_no")
    private String gstNumber;

    public TransportDto() {
    }

    public TransportDto(Integer transportCode, String transportName, String transportAddress, String gstNumber) {
        this.transportCode = transportCode;
        this.transportName = transportName;
        this.transportAddress = transportAddress;
        this.gstNumber = gstNumber;
    }

    public Integer getTransportCode() {
        return transportCode;
    }

    public void setTransportCode(Integer transportCode) {
        this.transportCode = transportCode;
    }

    public String getTransportName() {
        return transportName;
    }

    public void setTransportName(String transportName) {
        this.transportName = transportName;
    }

    public String getTransportAddress() {
        return transportAddress;
    }

    public void setTransportAddress(String transportAddress) {
        this.transportAddress = transportAddress;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }
}
