package in.lekhai.gsp.ewb.infrastructure.taxpro.mapper;

import in.lekhai.gsp.ewb.domain.enums.*;
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

        result.setEwbNo(Long.valueOf(response.ewbNo()));
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

    public EwbDetails toEwbDetails(TaxProEwbDetailResponse response){
        List<EwbDetails.EwbVehicleDetails> vehicleDetails =
                response.vehicleDetails() == null ? List.of() :
                        response.vehicleDetails().stream()
                                .map(v -> new EwbDetails.EwbVehicleDetails(
                                        v.updateMode(),
                                        v.vehicleNumber(),
                                        v.fromPlace(),
                                        v.fromStateCode(),
                                        v.tripSheetNumber(),
                                        v.transporterGstin(),
                                        safeParseDateTime(v.enteredDate()),
                                        safeTransportMode(v.transportMode()),
                                        v.transportDocumentNumber(),
                                        safeParseDate(v.transportDocumentDate()),
                                        v.groupNumber()
                                ))
                                .toList();

        return new EwbDetails(
                response.ewbNo(),
                convertDateTimeToInstant(response.ewayBillDate()),
                safeStatus(response.status()),
                response.genMode(),
                response.userGstin(),
                response.fromTradeName(),
                response.fromGstin(),
                response.fromPlace(),
                response.fromStateCode(),
                response.fromPincode(),
                response.fromAddressLine1(),
                response.fromAddressLine2(),
                response.toTradeName(),
                response.toGstin(),
                response.toPlace(),
                response.toStateCode(),
                response.toPincode(),
                response.toAddressLine1(),
                response.toAddressLine2(),
                safeDocumentType(response.documentType()),
                response.documentNumber(),
                safeParseDate(response.documentDate()),
                safeSupplyType(response.supplyType()),
                safeSubSupplyType(response.subSupplyType()),
                safeTransactionType(response.transactionType()),
                response.totalValue(),
                response.totalInvoiceValue(),
                response.cgstValue(),
                response.sgstValue(),
                response.igstValue(),
                response.cessValue(),
                response.otherValue(),
                response.cessNonAdvolValue(),
                safeVehicleType(response.vehicleType()),
                response.transporterGstin(),
                response.transporterName(),
                response.validDays(),
                safeParseDateTime(response.validUpto()),
                response.actualDistance(),
                response.actualFromStateCode(),
                response.actualToStateCode(),
                response.extendedTimes(),
                response.rejectStatus(),
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

    private EwbStatus safeStatus(String value) {
        if (value == null) return null;
        try {
            return EwbStatus.fromCode(value);
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert status: {}", value, e);
            return null;
        }
    }

    private EwbVehicleType safeVehicleType(String value) {
        if (value == null) return null;
        try {
            return EwbVehicleType.fromCode(value);
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert vehicleType: {}", value, e);
            return null;
        }
    }

    private SupplyType safeSupplyType(String value) {
        if (value == null) return null;
        try {
            return SupplyType.fromCode(value);
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert supplyType: {}", value, e);
            return null;
        }
    }

    private SubSupplyType safeSubSupplyType(String value) {
        if (value == null) return null;
        try {
            return SubSupplyType.fromCode(value);
        } catch (RuntimeException e) {
            log.error("Failed to convert subSupplyType: {}", value, e);
            return null;
        }
    }

    private DocumentType safeDocumentType(String value) {
        if (value == null) return null;
        try {
            return DocumentType.fromCode(value);
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert documentType: {}", value, e);
            return null;
        }
    }

    private TransactionType safeTransactionType(String value) {
        if (value == null) return null;
        try {
            return TransactionType.fromCode(Integer.valueOf(value));
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert transactionType: {}", value, e);
            return null;
        }
    }

    private TransportMode safeTransportMode(String value) {
        if (value == null) return null;
        try {
            return TransportMode.fromCode(value);
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert transportMode: {}", value, e);
            return null;
        }
    }

    private Instant safeParseDateTime(String value) {
        if (value == null) return null;
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(value, DATE_TIME_FORMATTER);
            return localDateTime.atZone(IST).toInstant();
        } catch (DateTimeException e) {
            log.error("Failed to parse datetime: {}", value, e);
            return null;
        }
    }

    private LocalDate safeParseDate(String value) {
        if (value == null) return null;
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (DateTimeException e) {
            log.error("Failed to parse date: {}", value, e);
            return null;
        }
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
