package in.lekhai.csv.upload.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AreaCsvDto {
    @JsonProperty("code")
    private Long areaCode;
    @JsonProperty("name")
    private String areaName;

    public AreaCsvDto() {
    }

    public AreaCsvDto(Long areaCode, String areaName) {
        this.areaCode = areaCode;
        this.areaName = areaName;
    }

    public Long getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(Long areaCode) {
        this.areaCode = areaCode;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }
}
