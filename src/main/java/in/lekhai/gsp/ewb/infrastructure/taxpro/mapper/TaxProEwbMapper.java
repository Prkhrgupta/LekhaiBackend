package in.lekhai.gsp.ewb.infrastructure.taxpro.mapper;

import in.lekhai.gsp.ewb.domain.enums.EwbStatus;
import in.lekhai.gsp.ewb.domain.enums.TransportMode;
import in.lekhai.gsp.ewb.domain.model.EwbDetails;
import in.lekhai.gsp.ewb.domain.model.EwbForTransporter;
import in.lekhai.gsp.ewb.domain.model.ExtendValidity;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProEwbDetailResponse;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProEwbForTransporterResponse;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProExtendValidityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class TaxProEwbMapper {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public EwbForTransporter toTransporterEwb(TaxProEwbForTransporterResponse response) {
        EwbForTransporter result = new EwbForTransporter();

        result.setEwbNo(response.ewbNo());
        result.setEwbDate(convertDateTimeToInstant(response.ewbDate()));
        result.setStatus(EwbStatus.valueOf(response.status()));
        result.setGeneratedGstIn(response.genGstin());
        result.setDocNo(response.docNo());
        result.setDocDate(convertDateToInstant(response.docDate()));
        result.setDeliveryPinCode(response.delPinCode());
        result.setDeliveryStateCode(response.delStateCode());
        result.setDeliverPlace(response.delPlace());
        result.setValidUpTo(convertDateTimeToInstant(response.validUpto()));
        result.setTimesExtended(response.extendedTimes());
        result.setRejected(!"N".equalsIgnoreCase(response.rejectStatus()));

        return result;
    }

    public EwbDetails toEwbDeatis(TaxProEwbDetailResponse response){
        List<EwbDetails.EwbVehicleDetails> vehicleDetails =
                response.vehicleDetails() == null ? List.of() :
                        response.vehicleDetails().stream()
                                .map(v -> new EwbDetails.EwbVehicleDetails(
                                        v.vehicleNo(),
                                        v.fromPlace(),
                                        v.fromState(),
                                        v.transDocNo(),
                                        v.transDocDate() != null
                                                ? LocalDate.parse(v.transDocDate(), DATE_FORMATTER)
                                                : null,
                                        v.transMode() != null
                                                ? TransportMode.fromCode(v.transMode())
                                                : null
                                ))
                                .toList();

        return new EwbDetails(
                response.ewbNo(),
                convertDateTimeToInstant(response.ewayBillDate()),
                response.fromPincode(),
                response.toPlace(),
                response.toStateCode(),
                response.toPincode(),
                response.vehicleType().getVehicleType(),
                response.noValidDays(),
                response.status(),
                response.addressLine1(),
                response.addressLine2(),
                vehicleDetails
        );
    }

    public ExtendValidity toExtendValidity(TaxProExtendValidityResponse response) {
        return new ExtendValidity(
                response.ewayBillNo(),
                convertDateTimeToInstant(response.updatedDate()),
                convertDateTimeToInstant(response.validUpto())
        );
    }

    private Instant convertDateTimeToInstant(String dateTime) {
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
            return localDateTime.atZone(IST).toInstant();
        } catch (DateTimeException e) {
            log.error("Failed to parse datetime: {}", dateTime, e);
            throw new RuntimeException(e);
        }
    }

    private LocalDate convertDateToInstant(String date) {
        try {
            return LocalDate.parse(date, DATE_FORMATTER);
        } catch (DateTimeException e) {
            log.error("Failed to parse date: {}", date, e);
            throw new RuntimeException(e);
        }
    }
}