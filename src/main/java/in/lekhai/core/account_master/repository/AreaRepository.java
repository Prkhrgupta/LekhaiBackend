package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.Area;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AreaRepository extends CrudRepository<Area, Long> {
    Optional<Area> findBySitswiftCode(Integer sitswiftCode);

    Page<Area> findAll(Pageable pageable);

    @Query("SELECT * FROM area WHERE area_name ILIKE '%' || :query || '%'")
    List<Area> findByAreaNameContainingIgnoreCase(@Param("query") String query, Pageable pageable);

    @Query("SELECT COUNT(*) FROM area WHERE area_name ILIKE '%' || :query || '%'")
    long countByAreaNameContainingIgnoreCase(@Param("query") String query);
}
