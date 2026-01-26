package in.lekhai.core.account_master.domain;

import in.lekhai.common.domain.ShopAwareEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("area")
public class Area extends ShopAwareEntity {
    @Id
    private Long id;
    private String areaName;
    private String stateCode;

    public Area() {
    }

    public Area(String areaName, String stateCode) {
        this.areaName = areaName;
        this.stateCode = stateCode;
    }

    public Long getId() {
        return id;
    }

    public String getAreaName() {
        return areaName;
    }

    public String getStateCode() {
        return stateCode;
    }
}
