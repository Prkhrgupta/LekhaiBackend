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
    private String ratePer;

    private BigDecimal openingPcs;
    private BigDecimal openingMeter;
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

    public String getRatePer() {
        return ratePer;
    }

    public void setRatePer(String ratePer) {
        this.ratePer = ratePer;
    }

    public BigDecimal getOpeningPcs() {
        return openingPcs;
    }

    public void setOpeningPcs(BigDecimal openingPcs) {
        this.openingPcs = openingPcs;
    }

    public BigDecimal getOpeningMeter() {
        return openingMeter;
    }

    public void setOpeningMeter(BigDecimal openingMeter) {
        this.openingMeter = openingMeter;
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
