package in.lekhai.core.account_master.domain;

import in.lekhai.common.domain.ShopAwareEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

/**
 * Duties &amp; taxes ledger-account configuration for a single sale ledger, e.g.
 * "SALE IN-UP @12%" → CGST @ 6% / SGST @ 6%. Previously modelled per commodity;
 * it belongs to the sale ledger account instead, since many commodities share
 * one configuration.
 */
@Table("sale_ledger_setting_master")
public class SaleLedgerSetting extends ShopAwareEntity {

    @Id
    private Long id;

    private Long saleLedgerId;
    private String saleType;
    private BigDecimal gstRate;

    private BigDecimal cgstPercentage;
    private Long cgstLedgerId;
    private BigDecimal sgstPercentage;
    private Long sgstLedgerId;
    private BigDecimal igstPercentage;
    private Long igstLedgerId;
    private BigDecimal cessPercentage;
    private Long cessLedgerId;

    private Long freightPackingLedgerId;
    private Long roundOffLedgerId;
    private BigDecimal tcsPercentage;
    private Long tcsLedgerId;

    private Boolean isDeleted = Boolean.FALSE;

    public SaleLedgerSetting() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSaleLedgerId() {
        return saleLedgerId;
    }

    public void setSaleLedgerId(Long saleLedgerId) {
        this.saleLedgerId = saleLedgerId;
    }

    public String getSaleType() {
        return saleType;
    }

    public void setSaleType(String saleType) {
        this.saleType = saleType;
    }

    public BigDecimal getGstRate() {
        return gstRate;
    }

    public void setGstRate(BigDecimal gstRate) {
        this.gstRate = gstRate;
    }

    public BigDecimal getCgstPercentage() {
        return cgstPercentage;
    }

    public void setCgstPercentage(BigDecimal cgstPercentage) {
        this.cgstPercentage = cgstPercentage;
    }

    public Long getCgstLedgerId() {
        return cgstLedgerId;
    }

    public void setCgstLedgerId(Long cgstLedgerId) {
        this.cgstLedgerId = cgstLedgerId;
    }

    public BigDecimal getSgstPercentage() {
        return sgstPercentage;
    }

    public void setSgstPercentage(BigDecimal sgstPercentage) {
        this.sgstPercentage = sgstPercentage;
    }

    public Long getSgstLedgerId() {
        return sgstLedgerId;
    }

    public void setSgstLedgerId(Long sgstLedgerId) {
        this.sgstLedgerId = sgstLedgerId;
    }

    public BigDecimal getIgstPercentage() {
        return igstPercentage;
    }

    public void setIgstPercentage(BigDecimal igstPercentage) {
        this.igstPercentage = igstPercentage;
    }

    public Long getIgstLedgerId() {
        return igstLedgerId;
    }

    public void setIgstLedgerId(Long igstLedgerId) {
        this.igstLedgerId = igstLedgerId;
    }

    public BigDecimal getCessPercentage() {
        return cessPercentage;
    }

    public void setCessPercentage(BigDecimal cessPercentage) {
        this.cessPercentage = cessPercentage;
    }

    public Long getCessLedgerId() {
        return cessLedgerId;
    }

    public void setCessLedgerId(Long cessLedgerId) {
        this.cessLedgerId = cessLedgerId;
    }

    public Long getFreightPackingLedgerId() {
        return freightPackingLedgerId;
    }

    public void setFreightPackingLedgerId(Long freightPackingLedgerId) {
        this.freightPackingLedgerId = freightPackingLedgerId;
    }

    public Long getRoundOffLedgerId() {
        return roundOffLedgerId;
    }

    public void setRoundOffLedgerId(Long roundOffLedgerId) {
        this.roundOffLedgerId = roundOffLedgerId;
    }

    public BigDecimal getTcsPercentage() {
        return tcsPercentage;
    }

    public void setTcsPercentage(BigDecimal tcsPercentage) {
        this.tcsPercentage = tcsPercentage;
    }

    public Long getTcsLedgerId() {
        return tcsLedgerId;
    }

    public void setTcsLedgerId(Long tcsLedgerId) {
        this.tcsLedgerId = tcsLedgerId;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
