package in.lekhai.authentication.dto;

import in.lekhai.core.enums.Roles;

public record ShopMenu(
        String name,
        Integer shopCode, // Not for display
        Roles role
) { }
