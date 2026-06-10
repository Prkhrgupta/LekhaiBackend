package in.lekhai.gsp.ewb.infrastructure.taxpro;

import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.gsp.ewb.domain.entity.EwbRecord;
import in.lekhai.gsp.ewb.domain.entity.EwbVehicleDetail;
import in.lekhai.gsp.ewb.domain.enums.ExtendValidityReason;
import in.lekhai.gsp.ewb.domain.model.EwbDetails;
import in.lekhai.gsp.ewb.domain.model.EwbForTransporter;
import in.lekhai.gsp.ewb.domain.model.ExtendValidity;
import in.lekhai.gsp.ewb.domain.port.EwbProvider;
import in.lekhai.gsp.ewb.infrastructure.taxpro.client.EwbTaxProWebClient;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProEwbDetailResponse;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProEwbForTransporterResponse;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProExtendValidityRequest;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProExtendValidityResponse;
import in.lekhai.gsp.ewb.infrastructure.taxpro.mapper.TaxProEwbMapper;
import in.lekhai.gsp.ewb.infrastructure.taxpro.service.TaxProAuthService;
import in.lekhai.gsp.ewb.repository.service.EwbRecordRepoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

import static in.lekhai.gsp.ewb.infrastructure.taxpro.utils.TaxProPojoUtils.createExtendValidityRequest;

@Component
public class TaxProEwbProvider implements EwbProvider {
    private final TaxProAuthService taxProAuthService;
    private final EwbTaxProWebClient ewbTaxproWebClient;
    private final TaxProEwbMapper taxProEwbMapper;

    private final EwbRecordRepoService ewbRecordRepoService;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public TaxProEwbProvider(
            TaxProAuthService taxProAuthService,
            EwbTaxProWebClient ewbTaxproWebClient,
            TaxProEwbMapper taxProEwbMapper,
            EwbRecordRepoService ewbRecordRepoService
    ) {
        this.taxProAuthService = taxProAuthService;
        this.ewbTaxproWebClient = ewbTaxproWebClient;
        this.taxProEwbMapper = taxProEwbMapper;
        this.ewbRecordRepoService = ewbRecordRepoService;
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
    public EwbDetails getEwbDetails(Long ewbNo, String gstIn, Integer shopCode) {
        String ewbAuthToken = taxProAuthService.getEwbAuthToken(shopCode);
        log.info("Starting to fetch details for ewbNo : [{}] for shopCode : [{}]", ewbNo, shopCode);
        TaxProEwbDetailResponse taxProEwbDetailResponse = ewbTaxproWebClient.getEwbDetailsByEwbNo(ewbNo, gstIn, ewbAuthToken)
                .blockOptional()
                .orElseThrow(() -> new RuntimeException(String.format("Failed to fetch ewb details for ewb %s", ewbNo)));

        return taxProEwbMapper.toEwbDetails(taxProEwbDetailResponse);
    }

    @Override
    public ExtendValidity extendValidity(
            String ewbNo,
            Integer remainingDistance,
            ExtendValidityReason extensionReason,
            String extensionRemark,
            String gstIn,
            Integer shopCode
    ) {
        String ewbAuthToken = taxProAuthService.getEwbAuthToken(shopCode);
        EwbRecord ewbRecord = ewbRecordRepoService.getEwbRecord(ewbNo);
        EwbVehicleDetail ewbVehicleDetail = ewbRecord.getVehicleDetailSet().stream().findFirst()
                .orElseThrow(() -> new LekhaiClientException(String.format("No Vehicle details found for EwbNo=[%s]", ewbNo)));

        String transMode = ewbVehicleDetail.getTransportMode().getCode();
        String consignmentStatus = consignmentStatusFromTranMode(transMode);
        TaxProExtendValidityRequest extendValidityRequest = createExtendValidityRequest(ewbNo, remainingDistance,
                extensionReason, extensionRemark, ewbVehicleDetail, ewbRecord, transMode, consignmentStatus);

        TaxProExtendValidityResponse taxProExtendValidityResponse = ewbTaxproWebClient
                .extendEwbValidity(extendValidityRequest, gstIn, ewbAuthToken)
                .blockOptional()
                .orElseThrow(() -> new RuntimeException(String.format("Something went wrong while extending " +
                        "validity for ewbNo : %s", ewbNo)));

        return taxProEwbMapper.toExtendValidity(taxProExtendValidityResponse);
    }

    private String consignmentStatusFromTranMode(String transMode) {
        if ("5".equals(transMode)) {
            return "T"; // IN_TRANSIT
        }
        // for transMode 1–4
        return "M"; // IN_MOVEMENT
    }
}
