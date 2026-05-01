package in.lekhai.core.repository.users;

import in.lekhai.core.domain.users.UserShopAccess;
import in.lekhai.core.enums.Roles;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserShopAccessRepo extends ListCrudRepository<UserShopAccess, Long> {
    List<UserShopAccess> findByUserId(Long userId);

    Optional<UserShopAccess> findByUserIdAndShopId(Long userId, Long shopId);
    Optional<UserShopAccess> findByUserIdAndShopIdAndRole(Long userId, Long shopId, Roles role);
}
