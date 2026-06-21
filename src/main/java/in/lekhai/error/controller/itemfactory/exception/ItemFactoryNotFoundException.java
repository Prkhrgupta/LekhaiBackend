package in.lekhai.error.controller.itemfactory.exception;

import in.lekhai.error.controller.LekhaiException;
import in.lekhai.shop.context.model.ShopContext;

public class ItemFactoryNotFoundException extends LekhaiException {
    public ItemFactoryNotFoundException(Long id) {
        super(String.format("ItemFactory id=[%s] not found for shopCode=[%s]", id, ShopContext.getShopCode()));
    }
}
