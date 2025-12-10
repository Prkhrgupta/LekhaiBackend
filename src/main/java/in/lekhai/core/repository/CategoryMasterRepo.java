package in.lekhai.core.repository;

import in.lekhai.core.entity.CategoryMaster;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryMasterRepo extends ListCrudRepository<CategoryMaster, Long> {
    Optional<CategoryMaster> findByCategory(String category);
}
