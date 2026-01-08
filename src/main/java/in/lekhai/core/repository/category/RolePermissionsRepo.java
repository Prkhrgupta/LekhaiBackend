package in.lekhai.core.repository.category;

import in.lekhai.core.domain.category.RolePermissions;
import in.lekhai.core.enums.Roles;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolePermissionsRepo extends ListCrudRepository<RolePermissions, Long> {
    @Query("""
            SELECT *
            FROM role_permissions
            WHERE category_id = :categoryId
            AND role = :role
            """)
    Optional<RolePermissions> findByCategoryIdAndRoleId(Integer categoryId, Roles role);


}
