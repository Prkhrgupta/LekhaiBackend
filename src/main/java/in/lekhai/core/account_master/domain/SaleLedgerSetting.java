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

    private Long cgstLedgerId;
    private Long sgstLedgerId;
    private Long igstLedgerId;

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
