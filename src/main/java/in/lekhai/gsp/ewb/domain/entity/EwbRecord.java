package in.lekhai.gsp.ewb.domain.entity;

import in.lekhai.gsp.ewb.domain.enums.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Table("ewb_records")
public class EwbRecord {

    @Id
    private Long id;

    @Column("ewb_no")
    private Long ewbNo;

    @Column("eway_bill_date")
    private Instant ewayBillDate;

    @Column("valid_upto")
    private Instant validUpTo;

    @Column("status")
    private String status;

    @Column("reject_status")
    private Boolean rejectStatus;

    @Column("gen_mode")
    private String genMode;

    @Column("supply_type")
    private String supplyType;

    @Column("sub_supply_type")
    private String subSupplyType;

    @Column("document_type")
    private String documentType;

    @Column("document_number")
    private String documentNumber;

    @Column("document_date")
    private LocalDate documentDate;

    @Column("generator_gstin")
    private String generatorGstin;

    @Column("from_gstin")
    private String fromGstin;

    @Column("from_trade_name")
    private String fromTradeName;

    @Column("from_address_line_1")
    private String fromAddressLine1;

    @Column("from_address_line_2")
    private String fromAddressLine2;

    @Column("from_place")
    private String fromPlace;

    @Column("from_pincode")
    private Integer fromPinCode;

    @Column("from_state_code")
    private String fromStateCode;

    @Column("to_gstin")
    private String toGstin;

    @Column("to_trade_name")
    private String toTradeName;

    @Column("to_address_line_1")
    private String toAddressLine1;

    @Column("to_address_line_2")
    private String toAddressLine2;

    @Column("to_place")
    private String toPlace;

    @Column("to_pincode")
    private Integer toPinCode;

    @Column("to_state_code")
    private String toStateCode;

    @Column("transporter_gstin")
    private String transporterGstin;

    @Column("transporter_name")
    private String transporterName;

    @Column("total_value")
    private BigDecimal totalValue;

    @Column("total_invoice_value")
    private BigDecimal totalInvoiceValue;

    @Column("cgst_value")
    private BigDecimal cgstValue;

    @Column("sgst_value")
    private BigDecimal sgstValue;

    @Column("igst_value")
    private BigDecimal igstValue;

    @Column("cess_value")
    private BigDecimal cessValue;

    @Column("other_value")
    private BigDecimal otherValue;

    @Column("cess_non_advol_value")
    private BigDecimal cessNonAdvolValue;

    @Column("actual_distance")
    private Integer actualDistance;

    @Column("valid_days")
    private Integer validDays;

    @Column("extended_times")
    private Integer extendedTimes;

    @Column("vehicle_type")
    private String vehicleType;

    @Column("transaction_type")
    private Integer transactionType;

    @Column("is_delivered")
    private boolean delivered;

    @MappedCollection(idColumn = "ewb_record_id")
    Set<EwbVehicleDetail> vehicleDetailSet = new HashSet<>();

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ------------------------------------------------
    // ENUM GETTERS / SETTERS
    // ------------------------------------------------

    public EwbStatus getStatus() {
        return EwbStatus.fromCode(status);
    }

    public void setStatus(EwbStatus status) {
        this.status = status != null ? status.getCode() : null;
    }

    public SupplyType getSupplyType() {
        return SupplyType.fromCode(supplyType);
    }

    public void setSupplyType(SupplyType supplyType) {
        this.supplyType = supplyType != null ? supplyType.getCode() : null;
    }

    public SubSupplyType getSubSupplyType() {
        return SubSupplyType.fromCode(subSupplyType);
    }

    public void setSubSupplyType(SubSupplyType subSupplyType) {
        this.subSupplyType =
                subSupplyType != null ? subSupplyType.getCode() : null;
    }

    public DocumentType getDocumentType() {
        return DocumentType.fromCode(documentType);
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType =
                documentType != null ? documentType.getCode() : null;
    }

    public EwbVehicleType getVehicleType() {
        return EwbVehicleType.fromCode(vehicleType);
    }

    public void setVehicleType(EwbVehicleType vehicleType) {
        this.vehicleType =
                vehicleType != null ? vehicleType.getCode() : null;
    }

    public TransactionType getTransactionType() {
        return TransactionType.fromCode(transactionType);
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType =
                transactionType != null ? transactionType.getCode() : null;
    }

    // ------------------------------------------------
    // NORMAL GETTERS / SETTERS
    // ------------------------------------------------

    public Long getId() {
        return id;
    }

    public Long getEwbNo() {
        return ewbNo;
    }

    public void setEwbNo(Long ewbNo) {
        this.ewbNo = ewbNo;
    }

    public Instant getEwayBillDate() {
        return ewayBillDate;
    }

    public void setEwayBillDate(Instant ewayBillDate) {
        this.ewayBillDate = ewayBillDate;
    }

    public Instant getValidUpTo() {
        return validUpTo;
    }

    public void setValidUpTo(Instant validUpTo) {
        this.validUpTo = validUpTo;
    }

    public Boolean getRejectStatus() {
        return rejectStatus;
    }

    public void setRejectStatus(Boolean rejectStatus) {
        this.rejectStatus = rejectStatus;
    }

    public String getGenMode() {
        return genMode;
    }

    public void setGenMode(String genMode) {
        this.genMode = genMode;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public LocalDate getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(LocalDate documentDate) {
        this.documentDate = documentDate;
    }

    public String getGeneratorGstin() {
        return generatorGstin;
    }

    public void setGeneratorGstin(String generatorGstin) {
        this.generatorGstin = generatorGstin;
    }

    public String getFromGstin() {
        return fromGstin;
    }

    public void setFromGstin(String fromGstin) {
        this.fromGstin = fromGstin;
    }

    public String getFromTradeName() {
        return fromTradeName;
    }

    public void setFromTradeName(String fromTradeName) {
        this.fromTradeName = fromTradeName;
    }

    public String getFromAddressLine1() {
        return fromAddressLine1;
    }

    public void setFromAddressLine1(String fromAddressLine1) {
        this.fromAddressLine1 = fromAddressLine1;
    }

    public String getFromAddressLine2() {
        return fromAddressLine2;
    }

    public void setFromAddressLine2(String fromAddressLine2) {
        this.fromAddressLine2 = fromAddressLine2;
    }

    public String getFromPlace() {
        return fromPlace;
    }

    public void setFromPlace(String fromPlace) {
        this.fromPlace = fromPlace;
    }

    public Integer getFromPinCode() {
        return fromPinCode;
    }

    public void setFromPinCode(Integer fromPinCode) {
        this.fromPinCode = fromPinCode;
    }

    public String getFromStateCode() {
        return fromStateCode;
    }

    public void setFromStateCode(String fromStateCode) {
        this.fromStateCode = fromStateCode;
    }

    public String getToGstin() {
        return toGstin;
    }

    public void setToGstin(String toGstin) {
        this.toGstin = toGstin;
    }

    public String getToTradeName() {
        return toTradeName;
    }

    public void setToTradeName(String toTradeName) {
        this.toTradeName = toTradeName;
    }

    public String getToAddressLine1() {
        return toAddressLine1;
    }

    public void setToAddressLine1(String toAddressLine1) {
        this.toAddressLine1 = toAddressLine1;
    }

    public String getToAddressLine2() {
        return toAddressLine2;
    }

    public void setToAddressLine2(String toAddressLine2) {
        this.toAddressLine2 = toAddressLine2;
    }

    public String getToPlace() {
        return toPlace;
    }

    public void setToPlace(String toPlace) {
        this.toPlace = toPlace;
    }

    public Integer getToPinCode() {
        return toPinCode;
    }

    public void setToPinCode(Integer toPinCode) {
        this.toPinCode = toPinCode;
    }

    public String getToStateCode() {
        return toStateCode;
    }

    public void setToStateCode(String toStateCode) {
        this.toStateCode = toStateCode;
    }

    public String getTransporterGstin() {
        return transporterGstin;
    }

    public void setTransporterGstin(String transporterGstin) {
        this.transporterGstin = transporterGstin;
    }

    public String getTransporterName() {
        return transporterName;
    }

    public void setTransporterName(String transporterName) {
        this.transporterName = transporterName;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public BigDecimal getTotalInvoiceValue() {
        return totalInvoiceValue;
    }

    public void setTotalInvoiceValue(BigDecimal totalInvoiceValue) {
        this.totalInvoiceValue = totalInvoiceValue;
    }

    public BigDecimal getCgstValue() {
        return cgstValue;
    }

    public void setCgstValue(BigDecimal cgstValue) {
        this.cgstValue = cgstValue;
    }

    public BigDecimal getSgstValue() {
        return sgstValue;
    }

    public void setSgstValue(BigDecimal sgstValue) {
        this.sgstValue = sgstValue;
    }

    public BigDecimal getIgstValue() {
        return igstValue;
    }

    public void setIgstValue(BigDecimal igstValue) {
        this.igstValue = igstValue;
    }

    public BigDecimal getCessValue() {
        return cessValue;
    }

    public void setCessValue(BigDecimal cessValue) {
        this.cessValue = cessValue;
    }

    public BigDecimal getOtherValue() {
        return otherValue;
    }

    public void setOtherValue(BigDecimal otherValue) {
        this.otherValue = otherValue;
    }

    public BigDecimal getCessNonAdvolValue() {
        return cessNonAdvolValue;
    }

    public void setCessNonAdvolValue(BigDecimal cessNonAdvolValue) {
        this.cessNonAdvolValue = cessNonAdvolValue;
    }

    public Integer getActualDistance() {
        return actualDistance;
    }

    public void setActualDistance(Integer actualDistance) {
        this.actualDistance = actualDistance;
    }

    public Integer getValidDays() {
        return validDays;
    }

    public void setValidDays(Integer validDays) {
        this.validDays = validDays;
    }

    public Integer getExtendedTimes() {
        return extendedTimes;
    }

    public void setExtendedTimes(Integer extendedTimes) {
        this.extendedTimes = extendedTimes;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Set<EwbVehicleDetail> getVehicleDetailSet() {
        return vehicleDetailSet;
    }

    public void setVehicleDetailSet(Set<EwbVehicleDetail> vehicleDetailSet) {
        this.vehicleDetailSet = vehicleDetailSet;
    }
}