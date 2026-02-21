package in.lekhai.csv.upload.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BrokerDto {

    @JsonProperty("code")
    private Integer brokerCode;

    @JsonProperty("name")
    private String brokerName;

    public BrokerDto() {
    }

    public BrokerDto(String brokerName, Integer brokerCode) {
        this.brokerName = brokerName;
        this.brokerCode = brokerCode;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public Integer getBrokerCode() {
        return brokerCode;
    }

    public void setBrokerCode(Integer brokerCode) {
        this.brokerCode = brokerCode;
    }
}
