package in.lekhai.gsp.ewb.domain.entity;

import in.lekhai.gsp.ewb.domain.enums.EwbStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.LocalDate;

@Table("ewb_records")
public class EwbRecord {

    @Id
    private Long id;

    @Column("ewb_no")
    private String ewbNo;

    @Column("ewb_date")
    private Instant ewbDate;

    @Column("status")
    private EwbStatus status;

    @Column("generator_gstin")
    private String generatorGstin;

    @Column("doc_no")
    private String docNo;

    @Column("doc_date")
    private LocalDate docDate;

    @Column("delivery_pin_code")
    private Integer deliveryPinCode;

    @Column("delivery_state_code")
    // FK to state table
    private String deliveryStateCode;

    @Column("delivery_place")
    private String deliveryPlace;

    @Column("valid_up_to")
    private Instant validUpTo;

    @Column("extended_times")
    private Integer extendedTimes;

    @Column("reject_status")
    private Boolean rejectStatus;

    @Column("is_delivered")
    private boolean isDelivered;

    @Column("created_at")
    @CreatedDate
    private Instant createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private Instant updatedAt;

    public String getEwbNo() {
        return ewbNo;
    }

    public void setEwbNo(String ewbNo) {
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

    public String getGeneratorGstin() {
        return generatorGstin;
    }

    public void setGeneratorGstin(String generatorGstin) {
        this.generatorGstin = generatorGstin;
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

    public boolean isDelivered() {
        return isDelivered;
    }

    public void setDelivered(boolean delivered) {
        isDelivered = delivered;
    }

    public void setDeliveryPinCode(Integer deliveryPinCode) {
        this.deliveryPinCode = deliveryPinCode;
    }

    public String getDeliveryStateCode() {
        return deliveryStateCode;
    }

    public void setDeliveryStateCode(String deliveryStateCode) {
        this.deliveryStateCode = deliveryStateCode;
    }

    public String getDeliveryPlace() {
        return deliveryPlace;
    }

    public void setDeliveryPlace(String deliveryPlace) {
        this.deliveryPlace = deliveryPlace;
    }

    public Instant getValidUpTo() {
        return validUpTo;
    }

    public void setValidUpTo(Instant validUpTo) {
        this.validUpTo = validUpTo;
    }

    public Integer getExtendedTimes() {
        return extendedTimes;
    }

    public void setExtendedTimes(Integer extendedTimes) {
        this.extendedTimes = extendedTimes;
    }

    public Boolean getRejectStatus() {
        return rejectStatus;
    }

    public void setRejectStatus(Boolean rejectStatus) {
        this.rejectStatus = rejectStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "EwbRecord{" +
                "id=" + id +
                ", ewbNo=" + ewbNo +
                ", ewbDate=" + ewbDate +
                ", status=" + status +
                ", generatorGstin='" + generatorGstin + '\'' +
                ", docNo='" + docNo + '\'' +
                ", docDate=" + docDate +
                ", deliveryPinCode=" + deliveryPinCode +
                ", deliveryStateCode='" + deliveryStateCode + '\'' +
                ", deliveryPlace='" + deliveryPlace + '\'' +
                ", validUpTo=" + validUpTo +
                ", extendedTimes=" + extendedTimes +
                ", rejectStatus=" + rejectStatus +
                ", isDelivered=" + isDelivered +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}