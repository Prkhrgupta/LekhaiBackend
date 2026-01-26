package in.lekhai.core.account_master.domain;

import in.lekhai.common.domain.ShopAwareEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("transport")
public class Transport extends ShopAwareEntity {
    @Id
    private Long id;
    private String name;
    private String phone;
    private String gstNo;

    public Transport() {
    }

    public Transport(String name, String phone, String gstNo) {
        this.name = name;
        this.phone = phone;
        this.gstNo = gstNo;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getGstNo() {
        return gstNo;
    }
}
