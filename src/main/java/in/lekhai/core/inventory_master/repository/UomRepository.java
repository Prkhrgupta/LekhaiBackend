package in.lekhai.core.inventory_master.repository;

import in.lekhai.core.inventory_master.domain.Uom;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UomRepository extends CrudRepository<Uom, Long> {

    @Query("SELECT * FROM uom_master WHERE (is_deleted IS NULL OR is_deleted = FALSE) "
            + "ORDER BY unit_name, id LIMIT :limit OFFSET :offset")
    List<Uom> findAllActive(@Param("limit") int limit, @Param("offset") long offset);

    @Query("SELECT COUNT(*) FROM uom_master WHERE (is_deleted IS NULL OR is_deleted = FALSE)")
    long countAllActive();

    @Query("SELECT * FROM uom_master WHERE (is_deleted IS NULL OR is_deleted = FALSE) AND unit_name ILIKE '%' || :query || '%' "
            + "ORDER BY unit_name, id LIMIT :limit OFFSET :offset")
    List<Uom> findActiveByNameContainingIgnoreCase(@Param("query") String query,
                                                   @Param("limit") int limit,
                                                   @Param("offset") long offset);

    @Query("SELECT COUNT(*) FROM uom_master WHERE (is_deleted IS NULL OR is_deleted = FALSE) AND unit_name ILIKE '%' || :query || '%'")
    long countActiveByNameContainingIgnoreCase(@Param("query") String query);
}
