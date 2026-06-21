package in.lekhai.error.controller.itemcategory.exception;

import in.lekhai.error.controller.LekhaiException;
import in.lekhai.shop.context.model.ShopContext;

public class ItemCategoryNotFoundException extends LekhaiException {
    public ItemCategoryNotFoundException(Long id) {
        super(String.format("ItemCategory id=[%s] not found for shopCode=[%s]", id, ShopContext.getShopCode()));
    }
}
