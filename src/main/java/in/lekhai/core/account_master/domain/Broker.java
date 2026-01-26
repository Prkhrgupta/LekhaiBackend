package in.lekhai.core.account_master.domain;

import in.lekhai.common.domain.ShopAwareEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("broker")
public class Broker extends ShopAwareEntity {
    @Id
    private Long id;
    private String name;
    private String phone;
    public Broker() {
    }

    public Broker(String name, String phone) {
        this.name = name;
        this.phone = phone;
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
}
