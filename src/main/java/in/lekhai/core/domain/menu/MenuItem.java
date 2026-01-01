package in.lekhai.core.domain.menu;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record MenuItem(
        String id,
        String title,
        String icon,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String route, // only for leaf node
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<MenuItem> children
) {
}
