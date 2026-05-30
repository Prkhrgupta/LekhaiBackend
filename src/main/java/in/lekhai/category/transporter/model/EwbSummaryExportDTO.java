package in.lekhai.category.transporter.model;

import in.lekhai.common.excel.ExcelColumn;
import in.lekhai.contract.model.EwbSummary;

import java.time.format.DateTimeFormatter;

public class EwbSummaryExportDTO {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @ExcelColumn(name = "EWB No", order = 1, width = 4000)
    private String ewbNo;

    @ExcelColumn(name = "EWB Date", order = 2)
    private String ewbDate;

    @ExcelColumn(name = "Status", order = 3, width = 3000)
    private String status;

    @ExcelColumn(name = "Doc No", order = 4, width = 4000)
    private String docNo;

    @ExcelColumn(name = "Doc Date", order = 5)
    private String docDate;

    @ExcelColumn(name = "Destination", order = 6)
    private String destination;

    @ExcelColumn(name = "Source", order = 7)
    private String source;

    @ExcelColumn(name = "Valid Up To", order = 8)
    private String validUpTo;

    @ExcelColumn(name = "Delivered", order = 9, width = 3000)
    private Boolean isDelivered;

    @ExcelColumn(name = "Consigner", order = 10)
    private String consigner;

    @ExcelColumn(name = "Consignee", order = 11)
    private String consignee;

    @ExcelColumn(name = "Distance (km)", order = 12, width = 4000)
    private Integer actualDistance;

    @ExcelColumn(name = "Vehicle No", order = 13, width = 4000)
    private String vehicleNo;

    public EwbSummaryExportDTO() {
    }

    public EwbSummaryExportDTO(EwbSummary s) {
        this.ewbNo = s.getEwbNo();
        this.ewbDate = s.getEwbDate() != null ? s.getEwbDate().format(FORMATTER) : null;
        this.status = s.getStatus() != null ? s.getStatus().toString() : null;
        this.docNo = s.getDocNo();
        this.docDate = s.getDocDate() != null ? s.getDocDate().format(FORMATTER) : null;
        this.destination = s.getDestination();
        this.source = s.getSource();
        this.validUpTo = s.getValidUpTo() != null ? s.getValidUpTo().format(FORMATTER) : null;
        this.isDelivered = s.getIsDelivered();
        this.consigner = s.getConsigner();
        this.consignee = s.getConsignee();
        this.actualDistance = s.getActualDistance();
        this.vehicleNo = s.getVehicleNo();
    }

    public String getEwbNo() {
        return ewbNo;
    }

    public void setEwbNo(String ewbNo) {
        this.ewbNo = ewbNo;
    }

    public String getEwbDate() {
        return ewbDate;
    }

    public void setEwbDate(String ewbDate) {
        this.ewbDate = ewbDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    public String getDocDate() {
        return docDate;
    }

    public void setDocDate(String docDate) {
        this.docDate = docDate;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getValidUpTo() {
        return validUpTo;
    }

    public void setValidUpTo(String validUpTo) {
        this.validUpTo = validUpTo;
    }

    public Boolean getIsDelivered() {
        return isDelivered;
    }

    public void setIsDelivered(Boolean isDelivered) {
        this.isDelivered = isDelivered;
    }

    public String getConsigner() {
        return consigner;
    }

    public void setConsigner(String consigner) {
        this.consigner = consigner;
    }

    public String getConsignee() {
        return consignee;
    }

    public void setConsignee(String consignee) {
        this.consignee = consignee;
    }

    public Integer getActualDistance() {
        return actualDistance;
    }

    public void setActualDistance(Integer actualDistance) {
        this.actualDistance = actualDistance;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }
}
