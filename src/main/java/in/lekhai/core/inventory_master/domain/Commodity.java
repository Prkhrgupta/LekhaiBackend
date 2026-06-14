package in.lekhai.core.inventory_master.domain;

import in.lekhai.common.domain.ShopAwareEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("commodity_master")
public class Commodity extends ShopAwareEntity {

    @Id
    private Long itemId;
    private String itemName;
    private String hsnSacCode;
    private String description;
    private String uom;

    private BigDecimal gstRateSale;
    private BigDecimal gstRatePurchase;

    private Boolean isSalePurchaseActive;

    // Sale Ledger Config
    private Long saleAcInStateId;
    private BigDecimal saleCgstPercent;
    private BigDecimal saleSgstPercent;
    private BigDecimal saleCessPercent;
    private Long roundOffAcId;
    private Long saleAcOutStateId;
    private BigDecimal saleIgstPercent;
    private BigDecimal saleCessOutPercent;

    // Purchase Ledger Config
    private Long purchaseAcInStateId;
    private BigDecimal purchaseCgstPercent;
    private BigDecimal purchaseSgstPercent;
    private BigDecimal purchaseCessPercent;
    private Long purchaseAcOutStateId;
    private BigDecimal purchaseIgstPercent;
    private BigDecimal purchaseCessOutPercent;

    private Boolean isDeleted = Boolean.FALSE;

    public Commodity() {
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getHsnSacCode() {
        return hsnSacCode;
    }

    public void setHsnSacCode(String hsnSacCode) {
        this.hsnSacCode = hsnSacCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public BigDecimal getGstRateSale() {
        return gstRateSale;
    }

    public void setGstRateSale(BigDecimal gstRateSale) {
        this.gstRateSale = gstRateSale;
    }

    public BigDecimal getGstRatePurchase() {
        return gstRatePurchase;
    }

    public void setGstRatePurchase(BigDecimal gstRatePurchase) {
        this.gstRatePurchase = gstRatePurchase;
    }

    public Boolean getIsSalePurchaseActive() {
        return isSalePurchaseActive;
    }

    public void setIsSalePurchaseActive(Boolean salePurchaseActive) {
        isSalePurchaseActive = salePurchaseActive;
    }

    public Long getSaleAcInStateId() {
        return saleAcInStateId;
    }

    public void setSaleAcInStateId(Long saleAcInStateId) {
        this.saleAcInStateId = saleAcInStateId;
    }

    public BigDecimal getSaleCgstPercent() {
        return saleCgstPercent;
    }

    public void setSaleCgstPercent(BigDecimal saleCgstPercent) {
        this.saleCgstPercent = saleCgstPercent;
    }

    public BigDecimal getSaleSgstPercent() {
        return saleSgstPercent;
    }

    public void setSaleSgstPercent(BigDecimal saleSgstPercent) {
        this.saleSgstPercent = saleSgstPercent;
    }

    public BigDecimal getSaleCessPercent() {
        return saleCessPercent;
    }

    public void setSaleCessPercent(BigDecimal saleCessPercent) {
        this.saleCessPercent = saleCessPercent;
    }

    public Long getRoundOffAcId() {
        return roundOffAcId;
    }

    public void setRoundOffAcId(Long roundOffAcId) {
        this.roundOffAcId = roundOffAcId;
    }

    public Long getSaleAcOutStateId() {
        return saleAcOutStateId;
    }

    public void setSaleAcOutStateId(Long saleAcOutStateId) {
        this.saleAcOutStateId = saleAcOutStateId;
    }

    public BigDecimal getSaleIgstPercent() {
        return saleIgstPercent;
    }

    public void setSaleIgstPercent(BigDecimal saleIgstPercent) {
        this.saleIgstPercent = saleIgstPercent;
    }

    public BigDecimal getSaleCessOutPercent() {
        return saleCessOutPercent;
    }

    public void setSaleCessOutPercent(BigDecimal saleCessOutPercent) {
        this.saleCessOutPercent = saleCessOutPercent;
    }

    public Long getPurchaseAcInStateId() {
        return purchaseAcInStateId;
    }

    public void setPurchaseAcInStateId(Long purchaseAcInStateId) {
        this.purchaseAcInStateId = purchaseAcInStateId;
    }

    public BigDecimal getPurchaseCgstPercent() {
        return purchaseCgstPercent;
    }

    public void setPurchaseCgstPercent(BigDecimal purchaseCgstPercent) {
        this.purchaseCgstPercent = purchaseCgstPercent;
    }

    public BigDecimal getPurchaseSgstPercent() {
        return purchaseSgstPercent;
    }

    public void setPurchaseSgstPercent(BigDecimal purchaseSgstPercent) {
        this.purchaseSgstPercent = purchaseSgstPercent;
    }

    public BigDecimal getPurchaseCessPercent() {
        return purchaseCessPercent;
    }

    public void setPurchaseCessPercent(BigDecimal purchaseCessPercent) {
        this.purchaseCessPercent = purchaseCessPercent;
    }

    public Long getPurchaseAcOutStateId() {
        return purchaseAcOutStateId;
    }

    public void setPurchaseAcOutStateId(Long purchaseAcOutStateId) {
        this.purchaseAcOutStateId = purchaseAcOutStateId;
    }

    public BigDecimal getPurchaseIgstPercent() {
        return purchaseIgstPercent;
    }

    public void setPurchaseIgstPercent(BigDecimal purchaseIgstPercent) {
        this.purchaseIgstPercent = purchaseIgstPercent;
    }

    public BigDecimal getPurchaseCessOutPercent() {
        return purchaseCessOutPercent;
    }

    public void setPurchaseCessOutPercent(BigDecimal purchaseCessOutPercent) {
        this.purchaseCessOutPercent = purchaseCessOutPercent;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
