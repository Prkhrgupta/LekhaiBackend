package in.lekhai.core.account_master.domain;

import in.lekhai.common.domain.ShopAwareEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

/**
 * Purchase-side counterpart of {@link SaleLedgerSetting}: duties &amp; taxes
 * ledger-account configuration for a single purchase ledger. Carries TDS where
 * the sale side carries TCS.
 */
@Table("purchase_ledger_setting_master")
public class PurchaseLedgerSetting extends ShopAwareEntity {

    @Id
    private Long id;

    private Long purchaseLedgerId;
    private String purchaseType;
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
    private BigDecimal tdsPercentage;
    private Long tdsLedgerId;

    private Boolean isDeleted = Boolean.FALSE;

    public PurchaseLedgerSetting() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPurchaseLedgerId() {
        return purchaseLedgerId;
    }

    public void setPurchaseLedgerId(Long purchaseLedgerId) {
        this.purchaseLedgerId = purchaseLedgerId;
    }

    public String getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType;
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

    public BigDecimal getTdsPercentage() {
        return tdsPercentage;
    }

    public void setTdsPercentage(BigDecimal tdsPercentage) {
        this.tdsPercentage = tdsPercentage;
    }

    public Long getTdsLedgerId() {
        return tdsLedgerId;
    }

    public void setTdsLedgerId(Long tdsLedgerId) {
        this.tdsLedgerId = tdsLedgerId;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
