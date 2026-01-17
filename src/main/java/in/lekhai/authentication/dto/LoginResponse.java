package in.lekhai.authentication.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(
        List<ShopMenu> shopMenu,
        String token
) { }
