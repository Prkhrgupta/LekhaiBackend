package in.lekhai.gsp.ewb.domain.entity;

import in.lekhai.gsp.ewb.domain.enums.TransportMode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table("ewb_vehicle_details")
public class EwbVehicleDetail {

    @Id
    private Long id;

    @Column("ewb_record_id")
    private Long ewbRecordId;

    @Column("update_mode")
    private String updateMode;

    @Column("vehicle_number")
    private String vehicleNumber;

    @Column("from_place")
    private String fromPlace;

    @Column("from_state_code")
    private String fromStateCode;

    @Column("trip_sheet_number")
    private Long tripSheetNumber;

    @Column("transporter_gstin")
    private String transporterGstin;

    @Column("entered_date")
    private LocalDateTime enteredDate;

    @Column("transport_mode")
    private String transportMode;

    @Column("transport_document_number")
    private String transportDocumentNumber;

    @Column("transport_document_date")
    private LocalDate transportDocumentDate;

    @Column("group_number")
    private String groupNumber;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    // ------------------------------------------------
    // ENUM GETTERS / SETTERS
    // ------------------------------------------------

    public TransportMode getTransportMode() {
        return TransportMode.fromCode(transportMode);
    }

    public void setTransportMode(TransportMode transportMode) {
        this.transportMode =
                transportMode != null ? transportMode.getCode() : null;
    }

    // ------------------------------------------------
    // NORMAL GETTERS / SETTERS
    // ------------------------------------------------

    public Long getId() {
        return id;
    }

    public Long getEwbRecordId() {
        return ewbRecordId;
    }

    public void setEwbRecordId(Long ewbRecordId) {
        this.ewbRecordId = ewbRecordId;
    }

    public String getUpdateMode() {
        return updateMode;
    }

    public void setUpdateMode(String updateMode) {
        this.updateMode = updateMode;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getFromPlace() {
        return fromPlace;
    }

    public void setFromPlace(String fromPlace) {
        this.fromPlace = fromPlace;
    }

    public String getFromStateCode() {
        return fromStateCode;
    }

    public void setFromStateCode(String fromStateCode) {
        this.fromStateCode = fromStateCode;
    }

    public Long getTripSheetNumber() {
        return tripSheetNumber;
    }

    public void setTripSheetNumber(Long tripSheetNumber) {
        this.tripSheetNumber = tripSheetNumber;
    }

    public String getTransporterGstin() {
        return transporterGstin;
    }

    public void setTransporterGstin(String transporterGstin) {
        this.transporterGstin = transporterGstin;
    }

    public LocalDateTime getEnteredDate() {
        return enteredDate;
    }

    public void setEnteredDate(LocalDateTime enteredDate) {
        this.enteredDate = enteredDate;
    }

    public String getTransportDocumentNumber() {
        return transportDocumentNumber;
    }

    public void setTransportDocumentNumber(String transportDocumentNumber) {
        this.transportDocumentNumber = transportDocumentNumber;
    }

    public LocalDate getTransportDocumentDate() {
        return transportDocumentDate;
    }

    public void setTransportDocumentDate(LocalDate transportDocumentDate) {
        this.transportDocumentDate = transportDocumentDate;
    }

    public String getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(String groupNumber) {
        this.groupNumber = groupNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}