package in.lekhai.common;

import in.lekhai.common.domain.ShopAwareEntity;
import in.lekhai.shop.context.model.ShopContext;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
public class ShopAwareEntityCallback implements BeforeConvertCallback<ShopAwareEntity> {

    @Override
    public ShopAwareEntity onBeforeConvert(ShopAwareEntity entity) {
        // Auto-set shop_code
        Integer shopCode = ShopContext.getShopCode();
        if (shopCode != null) {
            entity.setShopCodeIfNull(shopCode);
        }

        return entity;
    }
}