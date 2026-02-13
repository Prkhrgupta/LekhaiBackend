package in.lekhai.csv.upload.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BrokerDto {

    @JsonProperty("code")
    private Long brokerCode;

    @JsonProperty("name")
    private String brokerName;

    public BrokerDto() {
    }

    public BrokerDto(String brokerName, Long brokerCode) {
        this.brokerName = brokerName;
        this.brokerCode = brokerCode;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public Long getBrokerCode() {
        return brokerCode;
    }

    public void setBrokerCode(Long brokerCode) {
        this.brokerCode = brokerCode;
    }
}
