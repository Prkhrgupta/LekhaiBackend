package in.lekhai.core.inventory_master.domain;

import in.lekhai.common.domain.ShopAwareEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("uom_master")
public class Uom extends ShopAwareEntity {

    @Id
    private Long id;
    private String unitName;
    private String quantityCode;
    private Boolean isDeleted = Boolean.FALSE;

    public Uom() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getQuantityCode() {
        return quantityCode;
    }

    public void setQuantityCode(String quantityCode) {
        this.quantityCode = quantityCode;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
