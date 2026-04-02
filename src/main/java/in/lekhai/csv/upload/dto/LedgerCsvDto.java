package in.lekhai.csv.upload.dto;

import com.opencsv.bean.CsvBindByName;

public class LedgerCsvDto {

    @CsvBindByName(column = "code")
    private Integer ledgerCode;

    @CsvBindByName(column = "name")
    private String name;

    @CsvBindByName(column = "location")
    private String location;

    @CsvBindByName(column = "legal")
    private String legalName;

    @CsvBindByName(column = "add1")
    private String add1;

    @CsvBindByName(column = "add2")
    private String add2;

    @CsvBindByName(column = "add3")
    private String add3;

    @CsvBindByName(column = "add4")
    private String add4;

    @CsvBindByName(column = "add5")
    private String add5;

    @CsvBindByName(column = "group_id")
    private String accountGroupName;

    @CsvBindByName(column = "state_id")
    private Integer stateId;

    @CsvBindByName(column = "gst_no")
    private String gstNo;

    @CsvBindByName(column = "pan_no")
    private String panNo;

    @CsvBindByName(column = "phone_no")
    private String phoneNo;

    @CsvBindByName(column = "obal")
    private Double openingBalance;

    @CsvBindByName(column = "aadhar_no")
    private String aadharNo;

    @CsvBindByName(column = "email")
    private String email;

    @CsvBindByName(column = "tpn_id")
    private Integer transportCsvId;

    @CsvBindByName(column = "brok_id")
    private Integer brokerCsvId;

    @CsvBindByName(column = "area_id")
    private Integer areaCsvId;

    public Integer getLedgerCode() {
        return ledgerCode;
    }

    public void setLedgerCode(Integer ledgerCode) {
        this.ledgerCode = ledgerCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getAdd1() {
        return add1;
    }

    public void setAdd1(String add1) {
        this.add1 = add1;
    }

    public String getAdd2() {
        return add2;
    }

    public void setAdd2(String add2) {
        this.add2 = add2;
    }

    public String getAdd3() {
        return add3;
    }

    public void setAdd3(String add3) {
        this.add3 = add3;
    }

    public String getAdd4() {
        return add4;
    }

    public void setAdd4(String add4) {
        this.add4 = add4;
    }

    public String getAdd5() {
        return add5;
    }

    public void setAdd5(String add5) {
        this.add5 = add5;
    }

    public String getAccountGroupName() {
        return accountGroupName;
    }

    public void setAccountGroupName(String accountGroupName) {
        this.accountGroupName = accountGroupName;
    }

    public Integer getStateId() {
        return stateId;
    }

    public void setStateId(Integer stateId) {
        this.stateId = stateId;
    }

    public String getGstNo() {
        return gstNo;
    }

    public void setGstNo(String gstNo) {
        this.gstNo = gstNo;
    }

    public String getPanNo() {
        return panNo;
    }

    public void setPanNo(String panNo) {
        this.panNo = panNo;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public Double getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(Double openingBalance) {
        this.openingBalance = openingBalance;
    }

    public String getAadharNo() {
        return aadharNo;
    }

    public void setAadharNo(String aadharNo) {
        this.aadharNo = aadharNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getTransportCsvId() {
        return transportCsvId;
    }

    public void setTransportCsvId(Integer transportCsvId) {
        this.transportCsvId = transportCsvId;
    }

    public Integer getBrokerCsvId() {
        return brokerCsvId;
    }

    public void setBrokerCsvId(Integer brokerCsvId) {
        this.brokerCsvId = brokerCsvId;
    }

    public Integer getAreaCsvId() {
        return areaCsvId;
    }

    public void setAreaCsvId(Integer areaCsvId) {
        this.areaCsvId = areaCsvId;
    }
}