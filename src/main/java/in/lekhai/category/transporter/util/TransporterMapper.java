package in.lekhai.category.transporter.util;

import in.lekhai.contract.model.EwbExtendResponse;
import in.lekhai.contract.model.EwbSummary;
import in.lekhai.core.account_master.domain.State;
import in.lekhai.core.account_master.repository.StateRepository;
import in.lekhai.gsp.ewb.domain.entity.EwbRecord;
import in.lekhai.gsp.ewb.domain.model.EwbDetails;
import in.lekhai.gsp.ewb.domain.model.EwbForTransporter;
import in.lekhai.gsp.ewb.domain.model.ExtendValidity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

@Component
public class TransporterMapper {
    private static final ZoneId IST = ZoneId.of(ZoneId.SHORT_IDS.get("IST"));
    private final StateRepository stateRepository;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public TransporterMapper(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    public static EwbSummary forTransporterToSummary(EwbForTransporter ewbForTransporter) {
        EwbSummary ewbSummary = new EwbSummary();
        ewbSummary.setEwbNo(ewbForTransporter.getEwbNo());
        ewbSummary.setEwbDate(ewbForTransporter.getEwbDate().atZone(IST).toOffsetDateTime());
        ewbSummary.setStatus(ewbForTransporter.getStatus().getEwbSummaryStatus());
        ewbSummary.setDocNo(ewbForTransporter.getDocNo());
        ewbSummary.setDelPlace(ewbForTransporter.getDeliverPlace());
        ewbSummary.setDelState(ewbForTransporter.getDeliveryStateCode().toString()); // TODO: change to state name
        ewbSummary.setValidUpTo(ewbForTransporter.getValidUpTo().atZone(IST).toOffsetDateTime());
        ewbSummary.setExtendedTimes(ewbForTransporter.getTimesExtended());
        return ewbSummary;
    }

    public in.lekhai.contract.model.EwbDetails ewbDetailsToContractEwbResponse(EwbDetails ewbDetails) {
        if (ewbDetails == null) {
            return null;
        }
        in.lekhai.contract.model.EwbDetails response = new in.lekhai.contract.model.EwbDetails();
        // Direct mappings
        response.setEwbNo(ewbDetails.ewbNo().toString());
        response.setFromPinCode(ewbDetails.fromPinCode());
        // Mapping from vehicle details (taking first vehicle if present)
        if (ewbDetails.ewbVehicleDetails() != null && !ewbDetails.ewbVehicleDetails().isEmpty()) {
            EwbDetails.EwbVehicleDetails vehicle = ewbDetails.ewbVehicleDetails().get(0);
            response.setFromPlace(vehicle.fromPlace());
            response.setFromState(
                    vehicle.fromState() != null ? String.valueOf(vehicle.fromState()) : null
            );
            response.setTransDocNo(vehicle.transportDocumentNo());
            response.setTransDocDate(
                    vehicle.transportDocumentDate() != null
                            ? vehicle.transportDocumentDate().atStartOfDay().atOffset(ZoneOffset.UTC)
                            : null
            );
            response.setVehicleNo(vehicle.vehicleNo());
        }

        // Not present in source → leaving as comments
         response.setEwbDate(ewbDetails.ewbDate().atZone(IST).toOffsetDateTime());
         response.setToPlace(ewbDetails.toPlace());
         response.setToState(convertGstCodeToStateCode(ewbDetails.toState()));
         response.setToPinCode(ewbDetails.toPinCode());
         response.setConsignee(ewbDetails.consignee());
         response.setConsigner(ewbDetails.consigner());
         response.setActualDistance(ewbDetails.actualDistance());

        return response;
    }
    public EwbSummary ewbRecordToSummary(EwbRecord e) {
        if (e == null) {
            return null;
        }
        EwbSummary summary = new EwbSummary();
        summary.setEwbNo(e.getEwbNo());
        if (e.getEwbDate() != null) {
            summary.setEwbDate(e.getEwbDate().atZone(IST).toOffsetDateTime());
        }
        summary.setStatus(e.getStatus().getEwbSummaryStatus());
        summary.setDocNo(e.getDocNo());
        summary.setDelPlace(e.getDeliveryPlace());
        summary.setDelState(e.getDeliveryStateCode());
        if (e.getValidUpTo() != null) {
            summary.setValidUpTo(e.getValidUpTo().atZone(IST).toOffsetDateTime());
        }
        summary.setExtendedTimes(e.getExtendedTimes());
        summary.isDelivered(e.isDelivered());

        return summary;
    }

    public EwbRecord convertEwbForTransporterToEwbRecord(EwbForTransporter e) {
        if (e == null) {
            return null;
        }
        //TODO: convert the gstCode to Integer in state table
        EwbRecord record = new EwbRecord();

        record.setDeliveryStateCode(convertGstCodeToStateCode(e.getDeliveryStateCode()));

        record.setEwbNo(e.getEwbNo());
        record.setEwbDate(e.getEwbDate());
        record.setStatus(e.getStatus());
        record.setGeneratorGstin(e.getGeneratedGstIn());
        record.setDocNo(e.getDocNo());
        record.setDocDate(e.getDocDate());
        record.setDeliveryPinCode(e.getDeliveryPinCode());
        record.setDeliveryPlace(e.getDeliverPlace());
        record.setValidUpTo(e.getValidUpTo());
        record.setExtendedTimes(e.getTimesExtended());
        record.setRejectStatus(e.isRejected());

        return record;
    }

    public EwbExtendResponse toEwbExtendResponse(ExtendValidity extendValidity) {
        EwbExtendResponse ewbExtendResponse = new EwbExtendResponse();
        ewbExtendResponse.setEwbNo(extendValidity.ewbNo());
        ewbExtendResponse.setUpdatedDate(extendValidity.updateAt().atZone(IST).toOffsetDateTime());
        ewbExtendResponse.setValidUpto(extendValidity.newValidUpTo().atZone(IST).toOffsetDateTime());
        return ewbExtendResponse;
    }

    private String convertGstCodeToStateCode(Integer gstCode) {
        //TODO : convert gstCode to integer in the DB
        String gstCodeString = gstCode == null
                ? null
                : String.format("%02d", gstCode);
        Optional<State> state = stateRepository.findByGstCode(gstCodeString);
        if(state.isEmpty()) {
            log.error("Invalid GST Code {}", gstCode);
            return null;
        }
        return state.get().getStateCode();
    }
}
