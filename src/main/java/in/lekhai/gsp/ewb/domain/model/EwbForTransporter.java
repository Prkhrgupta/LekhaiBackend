package in.lekhai.gsp.ewb.domain.model;

import in.lekhai.gsp.ewb.domain.enums.EwbStatus;

import java.time.Instant;
import java.time.LocalDate;

public class EwbForTransporter {
    Long ewbNo;
    Instant ewbDate;
    EwbStatus status;
    String generatedGstIn;
    String docNo;
    LocalDate docDate;
    Integer deliveryPinCode;
    Integer deliveryStateCode;
    String deliverPlace;
    Instant validUpTo;
    Integer timesExtended;
    Boolean rejected;

    public Long getEwbNo() {
        return ewbNo;
    }

    public void setEwbNo(Long ewbNo) {
        this.ewbNo = ewbNo;
    }

    public Instant getEwbDate() {
        return ewbDate;
    }

    public void setEwbDate(Instant ewbDate) {
        this.ewbDate = ewbDate;
    }

    public EwbStatus getStatus() {
        return status;
    }

    public void setStatus(EwbStatus status) {
        this.status = status;
    }

    public String getGeneratedGstIn() {
        return generatedGstIn;
    }

    public void setGeneratedGstIn(String generatedGstIn) {
        this.generatedGstIn = generatedGstIn;
    }

    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    public LocalDate getDocDate() {
        return docDate;
    }

    public void setDocDate(LocalDate docDate) {
        this.docDate = docDate;
    }

    public Integer getDeliveryPinCode() {
        return deliveryPinCode;
    }

    public void setDeliveryPinCode(Integer deliveryPinCode) {
        this.deliveryPinCode = deliveryPinCode;
    }

    public Integer getDeliveryStateCode() {
        return deliveryStateCode;
    }

    public void setDeliveryStateCode(Integer deliveryStateCode) {
        this.deliveryStateCode = deliveryStateCode;
    }

    public String getDeliverPlace() {
        return deliverPlace;
    }

    public void setDeliverPlace(String deliverPlace) {
        this.deliverPlace = deliverPlace;
    }

    public Instant getValidUpTo() {
        return validUpTo;
    }

    public void setValidUpTo(Instant validUpTo) {
        this.validUpTo = validUpTo;
    }

    public Integer getTimesExtended() {
        return timesExtended;
    }

    public void setTimesExtended(Integer timesExtended) {
        this.timesExtended = timesExtended;
    }

    public Boolean isRejected() {
        return rejected;
    }

    public void setRejected(Boolean rejected) {
        this.rejected = rejected;
    }
}
