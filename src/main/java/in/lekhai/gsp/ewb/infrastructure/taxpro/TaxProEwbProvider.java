package in.lekhai.gsp.ewb.infrastructure.taxpro;

import in.lekhai.gsp.ewb.domain.enums.ExtendValidityReason;
import in.lekhai.gsp.ewb.domain.model.EwbDetails;
import in.lekhai.gsp.ewb.domain.model.EwbForTransporter;
import in.lekhai.gsp.ewb.domain.model.ExtendValidity;
import in.lekhai.gsp.ewb.domain.port.EwbProvider;
import in.lekhai.gsp.ewb.infrastructure.taxpro.client.EwbTaxproWebClient;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProEwbDetailResponse;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProEwbForTransporterResponse;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProExtendValidityRequest;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProExtendValidityResponse;
import in.lekhai.gsp.ewb.infrastructure.taxpro.mapper.TaxProEwbMapper;
import in.lekhai.gsp.ewb.infrastructure.taxpro.service.TaxProAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class TaxProEwbProvider implements EwbProvider {
    private final TaxProAuthService taxProAuthService;
    private final EwbTaxproWebClient ewbTaxproWebClient;
    private final TaxProEwbMapper taxProEwbMapper;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final static DateTimeFormatter ddMMyyyy = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
        log.info("Starting to fetch details for ewbNo : [{}] for shopCode : [{}]", ewbNo, shopCode);
        TaxProEwbDetailResponse taxProEwbDetailResponse = ewbTaxproWebClient.getEwbDetailsByEwbNo(ewbNo, gstIn, ewbAuthToken)
                .blockOptional()
                .orElseThrow(() -> new RuntimeException(String.format("Failed to fetch ewb details for ewb %s", ewbNo)));

        return taxProEwbMapper.toEwbDetails(taxProEwbDetailResponse);
    }

    @Override
    public ExtendValidity extendValidity(String ewbNo,
                                         Integer remainingDistance,
                                         ExtendValidityReason extensionReason,
                                         String extensionRemark,
                                         String gstIn,
                                         Integer shopCode) {
        String ewbAuthToken = taxProAuthService.getEwbAuthToken(shopCode);
        EwbDetails ewbDetail = getEwbDetails(ewbNo, gstIn, shopCode);
        EwbDetails.EwbVehicleDetails vehicleDetail = ewbDetail.ewbVehicleDetails().stream().findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("No vehicle details available for ewbNo : [%s]", ewbNo)));


        String transMode = vehicleDetail.transportMode().getCode();
        String consignmentStatus = consignmentStatusFromTranMode(transMode);
        TaxProExtendValidityRequest extendValidityRequest = new TaxProExtendValidityRequest(
                Long.parseLong(ewbNo),
                vehicleDetail.vehicleNo(),
                vehicleDetail.fromPlace(),
                vehicleDetail.fromState(),
                remainingDistance,
                vehicleDetail.transportDocumentNo(),
                vehicleDetail.transportDocumentDate().format(ddMMyyyy),
                transMode,
                extensionReason.getReasonCode(),
                extensionRemark,
                ewbDetail.fromPinCode(),
                consignmentStatus,
                transitTypeFromTranMode(transMode, "R"), // TODO: change this default from "R" to userInput
                consignmentStatus.equals("T") ? ewbDetail.addressLine1() : null,
                consignmentStatus.equals("T") ? ewbDetail.addressLine2() : null,
                null
        );

        TaxProExtendValidityResponse taxProExtendValidityResponse =
                ewbTaxproWebClient.extendEwbValidity(extendValidityRequest, gstIn, ewbAuthToken)
                .blockOptional()
                .orElseThrow(() -> new RuntimeException(String.format("Something went wrong while extending validity for ewbNo : %s", ewbNo)));

        return taxProEwbMapper.toExtendValidity(taxProExtendValidityResponse);
    }

    private String transitTypeFromTranMode(String transMode, String transitTypeInput) {
        if ("5".equals(transMode)) {
            if (transitTypeInput == null) return "";
            if (transitTypeInput.equals("R") || transitTypeInput.equals("W") || transitTypeInput.equals("O")) {
                return transitTypeInput;
            }
            return "";
        }
        // for transMode 1–4 → must be blank
        return "";
    }

    private String consignmentStatusFromTranMode(String transMode) {
        if ("5".equals(transMode)) {
            return "T"; // IN_TRANSIT
        }
        // for transMode 1–4
        return "M"; // IN_MOVEMENT
    }
}
