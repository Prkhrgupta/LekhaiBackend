package in.lekhai.core.repository;

import in.lekhai.core.entity.RoleCategoryMaster;
import in.lekhai.core.model.enums.Roles;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleCategoryMasterRepo extends ListCrudRepository<RoleCategoryMaster, Long> {
    @Query("""
            SELECT *
            FROM role_category_master
            WHERE category_id = :categoryId
            AND role = :role
            """)
    Optional<RoleCategoryMaster> findByRoleAndCategoryId(Long categoryId, Roles role);
}
