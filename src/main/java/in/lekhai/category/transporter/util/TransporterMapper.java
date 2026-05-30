package in.lekhai.category.transporter.util;

import in.lekhai.contract.model.EwbExtendResponse;
import in.lekhai.contract.model.EwbSummary;
import in.lekhai.contract.model.VehicleDetail;
import in.lekhai.core.account_master.repository.StateRepository;
import in.lekhai.gsp.ewb.domain.entity.EwbRecord;
import in.lekhai.gsp.ewb.domain.entity.EwbVehicleDetail;
import in.lekhai.gsp.ewb.domain.model.EwbDetails;
import in.lekhai.gsp.ewb.domain.model.ExtendValidity;
import in.lekhai.gsp.ewb.domain.repository.EwbVehicleDetailRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;

@Component
public class TransporterMapper {
    private static final ZoneId IST = ZoneId.of(ZoneId.SHORT_IDS.get("IST"));
    private final StateRepository stateRepository;
    private final EwbVehicleDetailRepo ewbVehicleDetailRepo;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public TransporterMapper(StateRepository stateRepository,
                             EwbVehicleDetailRepo ewbVehicleDetailRepo) {
        this.stateRepository = stateRepository;
        this.ewbVehicleDetailRepo = ewbVehicleDetailRepo;
    }

    public in.lekhai.contract.model.EwbDetails ewbRecordToContractEwbResponse(EwbRecord ewbRecord) {
        if (ewbRecord == null) {
            return null;
        }
        in.lekhai.contract.model.EwbDetails response = new in.lekhai.contract.model.EwbDetails();
        response.setEwbNo(String.valueOf(ewbRecord.getEwbNo()));
        if (ewbRecord.getEwayBillDate() != null) {
            response.setEwbDate(ewbRecord.getEwayBillDate().atZone(IST).toOffsetDateTime());
        }
        response.setFromPlace(ewbRecord.getFromPlace());
        response.setFromState(ewbRecord.getFromStateCode());
        response.setToPlace(ewbRecord.getToPlace());
        response.setToState(ewbRecord.getToStateCode());
        response.setFromPinCode(ewbRecord.getFromPinCode());
        response.setToPinCode(ewbRecord.getToPinCode());
        response.setConsigner(ewbRecord.getFromTradeName());
        response.setConsignee(ewbRecord.getToTradeName());
        response.setActualDistance(ewbRecord.getActualDistance());
        if (!ewbRecord.getVehicleDetailSet().isEmpty()) {
            EwbVehicleDetail first = ewbRecord.getVehicleDetailSet().iterator().next();
            response.setVehicleNo(first.getVehicleNumber());
            response.setTransDocNo(first.getTransportDocumentNumber());
            if (first.getTransportDocumentDate() != null) {
                response.setTransDocDate(first.getTransportDocumentDate().atStartOfDay(IST).toOffsetDateTime());
            }
        }
        response.setVehicleDetails(
                ewbRecord.getVehicleDetailSet().stream()
                        .map(v -> {
                            VehicleDetail vd = new VehicleDetail();
                            vd.setVehicleNo(v.getVehicleNumber());
                            vd.setFromPlace(v.getFromPlace());
                            vd.setFromState(v.getFromStateCode());
                            return vd;
                        })
                        .toList()
        );
        return response;
    }
    public EwbSummary ewbRecordToSummary(EwbRecord e) {
        if (e == null) {
            return null;
        }
        EwbSummary summary = new EwbSummary();
        summary.setEwbNo(e.getEwbNo().toString());
        if (e.getEwayBillDate() != null) {
            summary.setEwbDate(e.getEwayBillDate().atZone(IST).toOffsetDateTime());
        }
        summary.setStatus(e.getStatus().getEwbSummaryStatus());
        summary.setDocNo(e.getDocumentNumber());
        if (e.getDocumentDate() != null) {
            summary.setDocDate(e.getDocumentDate().atStartOfDay(IST).toOffsetDateTime());
        }
        summary.setDestination(e.getToPlace());
        summary.setSource(e.getFromPlace());
        if (e.getValidUpTo() != null) {
            summary.setValidUpTo(e.getValidUpTo().atZone(IST).toOffsetDateTime());
        }
        summary.isDelivered(e.isDelivered());
        summary.setConsigner(e.getFromTradeName());
        summary.setConsignee(e.getToTradeName());
        summary.setActualDistance(e.getActualDistance());
        List<EwbVehicleDetail> vehicleDetails = ewbVehicleDetailRepo.findAllByEwbRecordId(e.getId());
        summary.setVehicleNo(vehicleDetails.getFirst().getVehicleNumber());

        return summary;
    }

    public EwbExtendResponse toEwbExtendResponse(ExtendValidity extendValidity) {
        EwbExtendResponse ewbExtendResponse = new EwbExtendResponse();
        ewbExtendResponse.setEwbNo(extendValidity.ewbNo());
        ewbExtendResponse.setUpdatedDate(extendValidity.updateAt().atZone(IST).toOffsetDateTime());
        ewbExtendResponse.setValidUpto(extendValidity.newValidUpTo().atZone(IST).toOffsetDateTime());
        return ewbExtendResponse;
    }

    public EwbRecord convertEwbDetailToEwbRecord(EwbDetails ewbDetails) {
        if (ewbDetails == null) {
            return null;
        }
        EwbRecord record = new EwbRecord();
        record.setEwbNo(ewbDetails.ewbNo());
        record.setGeneratorGstin(ewbDetails.generatorGstin());
        if (ewbDetails.ewbDate() != null) {
            record.setEwayBillDate(ewbDetails.ewbDate());
        }
        record.setFromPinCode(ewbDetails.fromPinCode());
        record.setFromTradeName(ewbDetails.consigner());
        record.setFromAddressLine1(ewbDetails.addressLine1());
        record.setFromAddressLine2(ewbDetails.addressLine2());
        record.setToPlace(ewbDetails.toPlace());
        record.setToStateCode(ewbDetails.toState().toString());
        record.setToPinCode(ewbDetails.toPinCode());
        record.setToTradeName(ewbDetails.consignee());
        record.setVehicleType(ewbDetails.vehicleType());
        record.setValidDays(ewbDetails.noOfValidDDays());
        record.setActualDistance(ewbDetails.actualDistance());
        record.setStatus(ewbDetails.status());
        record.setGenMode(ewbDetails.genMode());
        record.setSupplyType(ewbDetails.supplyType());
        record.setSubSupplyType(ewbDetails.subSupplyType());
        record.setDocumentType(ewbDetails.documentType());
        record.setDocumentNumber(ewbDetails.documentNumber());
        record.setDocumentDate(ewbDetails.documentDate());
        record.setFromGstin(ewbDetails.fromGstin());
        record.setFromPlace(ewbDetails.fromPlace());
        record.setFromStateCode(ewbDetails.fromState().toString());
        record.setToGstin(ewbDetails.toGstin());
        record.setToAddressLine1(ewbDetails.toAddressLine1());
        record.setToAddressLine2(ewbDetails.toAddressLine2());
        record.setTransporterGstin(ewbDetails.transporterGstin());
        record.setTransporterName(ewbDetails.transporterName());
        record.setTotalValue(ewbDetails.totalValue());
        record.setTotalInvoiceValue(ewbDetails.totalInvoiceValue());
        record.setCgstValue(ewbDetails.cgstValue());
        record.setSgstValue(ewbDetails.sgstValue());
        record.setIgstValue(ewbDetails.igstValue());
        record.setCessValue(ewbDetails.cessValue());
        record.setOtherValue(ewbDetails.otherValue());
        record.setCessNonAdvolValue(ewbDetails.cessNonAdvolValue());
        record.setExtendedTimes(ewbDetails.extendedTimes());
        record.setTransactionType(ewbDetails.transactionType());
        if (ewbDetails.validUpto() != null) {
            record.setValidUpTo(ewbDetails.validUpto());
        }
        record.setRejectStatus(ewbDetails.rejectStatus() != null && !"N".equalsIgnoreCase(ewbDetails.rejectStatus()));
        return record;
    }

    public EwbVehicleDetail convertEwbDetailToEwbVehicle(EwbDetails.EwbVehicleDetails ewbVehicleDetails) {
        if (ewbVehicleDetails == null) {
            return null;
        }
        EwbVehicleDetail vehicle = new EwbVehicleDetail();
        vehicle.setVehicleNumber(ewbVehicleDetails.vehicleNo());
        vehicle.setFromPlace(ewbVehicleDetails.fromPlace());
        vehicle.setFromStateCode(ewbVehicleDetails.fromState().toString());
        vehicle.setTransportDocumentNumber(ewbVehicleDetails.transportDocumentNo());
        vehicle.setTransportDocumentDate(ewbVehicleDetails.transportDocumentDate());
        vehicle.setTransportMode(ewbVehicleDetails.transportMode());
        vehicle.setUpdateMode(ewbVehicleDetails.updateMode());
        vehicle.setTripSheetNumber(ewbVehicleDetails.tripSheetNumber());
        vehicle.setTransporterGstin(ewbVehicleDetails.transporterGstin());
        if (ewbVehicleDetails.enteredDate() != null) {
            vehicle.setEnteredDate(ewbVehicleDetails.enteredDate().atZone(IST).toLocalDateTime());
        }
        vehicle.setGroupNumber(ewbVehicleDetails.groupNumber());
        return vehicle;
    }

//    private String convertGstCodeToStateCode(Integer gstCode) {
//        //TODO : convert gstCode to integer in the DB
//        String gstCodeString = gstCode == null
//                ? null
//                : String.format("%02d", gstCode);
//        Optional<State> state = stateRepository.findByGstCode(gstCodeString);
//        if(state.isEmpty()) {
//            log.error("Invalid GST Code {}", gstCode);
//            return null;
//        }
//        return state.get().getStateCode();
//    }
}
