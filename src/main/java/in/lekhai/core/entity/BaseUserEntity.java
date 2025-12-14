package in.lekhai.core.entity;


import in.lekhai.core.model.enums.Roles;
import java.util.List;

public interface BaseUserEntity {
    Long getCategoryId();
    List<Long> getPermissionBit();
    String getUuid();
    Roles getRole();
    Integer getTenant();
}