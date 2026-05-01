package in.lekhai.gsp.ewb.infrastructure.taxpro;

import in.lekhai.gsp.ewb.domain.model.EwbDetails;
import in.lekhai.gsp.ewb.domain.model.EwbForTransporter;
import in.lekhai.gsp.ewb.domain.port.EwbProvider;
import in.lekhai.gsp.ewb.infrastructure.taxpro.client.EwbTaxproWebClient;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProEwbDetailResponse;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProEwbForTransporterResponse;
import in.lekhai.gsp.ewb.infrastructure.taxpro.mapper.TaxProEwbMapper;
import in.lekhai.gsp.ewb.infrastructure.taxpro.service.TaxProAuthService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class TaxProEwbProvider implements EwbProvider {
    private final TaxProAuthService taxProAuthService;
    private final EwbTaxproWebClient ewbTaxproWebClient;
    private final TaxProEwbMapper taxProEwbMapper;

    public TaxProEwbProvider(TaxProAuthService taxProAuthService,
                             EwbTaxproWebClient ewbTaxproWebClient,
                             TaxProEwbMapper taxProEwbMapper) {
        this.taxProAuthService = taxProAuthService;
        this.ewbTaxproWebClient = ewbTaxproWebClient;
        this.taxProEwbMapper = taxProEwbMapper;
    }

    @Override
    public List<EwbForTransporter> getEwbListForTransporter(String gstIn, Instant date, Integer shopCode) {
        String ewbAuthToken = taxProAuthService.getEwbAuthToken(shopCode);

        List<TaxProEwbForTransporterResponse> listOfEwbs = ewbTaxproWebClient.getEwbsForTransporter(gstIn, ewbAuthToken, date)
                .blockOptional()
                .orElse(List.of()); // TODO : handle this failing

        return listOfEwbs.stream()
                .map(taxProEwbMapper::toTransporterEwb)
                .toList();
    }

    @Override
    public EwbDetails getEwbDetails(String ewbNo, String gstIn, Integer shopCode) {
        String ewbAuthToken = taxProAuthService.getEwbAuthToken(shopCode);

        TaxProEwbDetailResponse taxProEwbDetailResponse = ewbTaxproWebClient.getEwbDetailsByEwbNo(ewbNo, gstIn, ewbAuthToken)
                .blockOptional()
                .orElseThrow(() -> new RuntimeException(String.format("Failed to fetch ewb details for ewb %s", ewbNo)));

        return taxProEwbMapper.toEwbDeatis(taxProEwbDetailResponse);
    }
}
