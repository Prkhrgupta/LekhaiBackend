package in.lekhai.gsp.ewb.domain.port;

import in.lekhai.gsp.ewb.domain.enums.ExtendValidityReason;
import in.lekhai.gsp.ewb.domain.model.EwbDetails;
import in.lekhai.gsp.ewb.domain.model.EwbForTransporter;
import in.lekhai.gsp.ewb.domain.model.ExtendValidity;
import org.springframework.cache.annotation.Cacheable;

import java.time.Instant;
import java.util.List;

public interface EwbProvider {
    List<EwbForTransporter> getEwbListForTransporter(String gstIn, Instant date, Integer shopCode);
    @Cacheable(value = "ewb", key = "#ewbNo")
    EwbDetails getEwbDetails(String ewbNo, String gstIn, Integer shopCode);
    ExtendValidity extendValidity(String ewbNo,
                                  Integer remainingDistance,
                                  ExtendValidityReason extensionReason,
                                  String extensionRemark,
                                  String gstIn,
                                  Integer shopCode);
}
