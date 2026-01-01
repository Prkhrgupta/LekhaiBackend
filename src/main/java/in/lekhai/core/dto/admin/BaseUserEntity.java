package in.lekhai.core.dto.admin;


import in.lekhai.core.enums.Roles;

import java.util.List;

public interface BaseUserEntity {
    Long getCategoryId();
    List<Long> getPermissionBit();
    String getUuid();
    Roles getRole();
    Integer getTenant();
}