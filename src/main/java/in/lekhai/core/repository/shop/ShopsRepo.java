package in.lekhai.core.repository.shop;

import in.lekhai.core.domain.shop.Shops;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopsRepo extends ListCrudRepository<Shops, Long> { }
