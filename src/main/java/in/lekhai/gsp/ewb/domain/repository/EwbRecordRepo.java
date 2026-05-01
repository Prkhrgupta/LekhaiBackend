package in.lekhai.gsp.ewb.domain.repository;

import in.lekhai.gsp.ewb.domain.entity.EwbRecord;
import org.springframework.data.repository.ListCrudRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EwbRecordRepo extends ListCrudRepository<EwbRecord, Long> {
    List<EwbRecord> findByEwbDateBetween(Instant start, Instant end);
    List<EwbRecord> findByValidUpToLessThanEqualAndIsDeliveredFalse(Instant date);
    Optional<EwbRecord> findByEwbNo(String ewbNo);
}
