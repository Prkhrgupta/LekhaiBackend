package in.lekhai.core.repository.category;

import in.lekhai.core.domain.category.Categories;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriesRepo extends ListCrudRepository<Categories, Integer> {
    Optional<Categories> findByName(String name);
}
