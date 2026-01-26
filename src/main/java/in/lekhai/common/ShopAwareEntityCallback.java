package in.lekhai.common;

import in.lekhai.common.domain.ShopAwareEntity;
import in.lekhai.shop.context.model.ShopContext;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ShopAwareEntityCallback implements BeforeConvertCallback<ShopAwareEntity> {

    @NonNull
    @Override
    public ShopAwareEntity onBeforeConvert(@NonNull ShopAwareEntity entity) {
        // Auto-set shop_code
        Integer shopCode = ShopContext.getShopCode();
        if (shopCode != null) {
            entity.setShopCodeIfNull(shopCode);
        }

        return entity;
    }
}