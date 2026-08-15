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

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
