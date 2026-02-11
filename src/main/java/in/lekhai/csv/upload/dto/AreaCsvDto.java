package in.lekhai.csv.upload.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AreaCsvDto(
        @NotNull @JsonProperty("code") Long areaCode,
        @NotBlank @JsonProperty("name") String areaName
) {
}
