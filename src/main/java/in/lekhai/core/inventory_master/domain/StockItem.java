package in.lekhai.core.inventory_master.domain;

import in.lekhai.common.domain.ShopAwareEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("stock_item_master")
public class StockItem extends ShopAwareEntity {

    @Id
    private Long id;

    private String finishedRawMaterial;
    private Long itemCategoryId;
    private Long itemFactoryId;
    private String itemName;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private Long commodityId;
    private Long primaryUomId;
    private Long alternateUomId;
    private BigDecimal conversionFactor;

    private BigDecimal openingQty;
    private BigDecimal openingRate;
    private BigDecimal openingValue;

    private Boolean isDeleted = Boolean.FALSE;

    public StockItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFinishedRawMaterial() {
        return finishedRawMaterial;
    }

    public void setFinishedRawMaterial(String finishedRawMaterial) {
        this.finishedRawMaterial = finishedRawMaterial;
    }

    public Long getItemCategoryId() {
        return itemCategoryId;
    }

    public void setItemCategoryId(Long itemCategoryId) {
        this.itemCategoryId = itemCategoryId;
    }

    public Long getItemFactoryId() {
        return itemFactoryId;
    }

    public void setItemFactoryId(Long itemFactoryId) {
        this.itemFactoryId = itemFactoryId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public Long getCommodityId() {
        return commodityId;
    }

    public void setCommodityId(Long commodityId) {
        this.commodityId = commodityId;
    }

    public Long getPrimaryUomId() {
        return primaryUomId;
    }

    public void setPrimaryUomId(Long primaryUomId) {
        this.primaryUomId = primaryUomId;
    }

    public Long getAlternateUomId() {
        return alternateUomId;
    }

    public void setAlternateUomId(Long alternateUomId) {
        this.alternateUomId = alternateUomId;
    }

    public BigDecimal getConversionFactor() {
        return conversionFactor;
    }

    public void setConversionFactor(BigDecimal conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    public BigDecimal getOpeningQty() {
        return openingQty;
    }

    public void setOpeningQty(BigDecimal openingQty) {
        this.openingQty = openingQty;
    }

    public BigDecimal getOpeningRate() {
        return openingRate;
    }

    public void setOpeningRate(BigDecimal openingRate) {
        this.openingRate = openingRate;
    }

    public BigDecimal getOpeningValue() {
        return openingValue;
    }

    public void setOpeningValue(BigDecimal openingValue) {
        this.openingValue = openingValue;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
