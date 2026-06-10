package in.lekhai.gsp.ewb.repository;

import in.lekhai.gsp.ewb.domain.entity.EwbVehicleDetail;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EwbVehicleDetailRepo extends ListCrudRepository<EwbVehicleDetail, Long> {
    List<EwbVehicleDetail> findAllByEwbRecordId(Long ewbRecordId);
}
