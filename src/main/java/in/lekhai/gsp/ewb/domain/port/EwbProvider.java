package in.lekhai.gsp.ewb.domain.port;

import in.lekhai.gsp.ewb.domain.model.EwbDetails;
import in.lekhai.gsp.ewb.domain.model.EwbForTransporter;

import java.time.Instant;
import java.util.List;

public interface EwbProvider {
    List<EwbForTransporter> getEwbListForTransporter(String gstIn, Instant date, Integer shopCode);
    EwbDetails getEwbDetails(String ewbNo, String gstIn, Integer shopCode);
}
