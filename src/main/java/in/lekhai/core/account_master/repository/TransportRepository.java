package in.lekhai.core.account_master.repository;

import in.lekhai.core.account_master.domain.Transport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportRepository extends CrudRepository<Transport, Long> {
    Optional<Transport> findBySitswiftCode(Integer sitswiftCode);

    Page<Transport> findAll(Pageable pageable);

    @Query("SELECT * FROM transport WHERE name ILIKE '%' || :query || '%'")
    List<Transport> findByNameContainingIgnoreCase(@Param("query") String query, Pageable pageable);

    @Query("SELECT COUNT(*) FROM transport WHERE name ILIKE '%' || :query || '%'")
    long countByNameContainingIgnoreCase(@Param("query") String query);

    @Query("SELECT * FROM transport WHERE gst_no ILIKE '%' || :query || '%'")
    List<Transport> findByGstNoContainingIgnoreCase(@Param("query") String query, Pageable pageable);

    @Query("SELECT COUNT(*) FROM transport WHERE gst_no ILIKE '%' || :query || '%'")
    long countByGstNoContainingIgnoreCase(@Param("query") String query);
}
