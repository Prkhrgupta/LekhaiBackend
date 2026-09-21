package in.lekhai.core.account_master.domain;

import in.lekhai.common.domain.ShopAwareEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

/**
 * Purchase-side counterpart of {@link SaleLedgerSetting}: duties &amp; taxes
 * ledger-account configuration for a single purchase ledger.
 */
@Table("purchase_ledger_setting_master")
public class PurchaseLedgerSetting extends ShopAwareEntity {

    @Id
    private Long id;

    private Long purchaseLedgerId;
    private String purchaseType;
    private BigDecimal gstRate;

    private Long cgstLedgerId;
    private Long sgstLedgerId;
    private Long igstLedgerId;

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

    public Long getCgstLedgerId() {
        return cgstLedgerId;
    }

    public void setCgstLedgerId(Long cgstLedgerId) {
        this.cgstLedgerId = cgstLedgerId;
    }

    public Long getSgstLedgerId() {
        return sgstLedgerId;
    }

    public void setSgstLedgerId(Long sgstLedgerId) {
        this.sgstLedgerId = sgstLedgerId;
    }

    public Long getIgstLedgerId() {
        return igstLedgerId;
    }

    public void setIgstLedgerId(Long igstLedgerId) {
        this.igstLedgerId = igstLedgerId;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
