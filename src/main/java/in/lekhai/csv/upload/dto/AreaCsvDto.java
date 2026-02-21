package in.lekhai.csv.upload.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AreaCsvDto {
    @JsonProperty("code")
    private Integer areaCode;
    @JsonProperty("name")
    private String areaName;

    public AreaCsvDto() {
    }

    public AreaCsvDto(Integer areaCode, String areaName) {
        this.areaCode = areaCode;
        this.areaName = areaName;
    }

    public Integer getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(Integer areaCode) {
        this.areaCode = areaCode;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }
}
