package in.lekhai.core.model.menu;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MenuResponse(
        @JsonProperty("Main Menu")
        List<MenuItem> mainMenuItems,
        @JsonProperty("Favourites")
        List<MenuItem> favourites
) {
}
