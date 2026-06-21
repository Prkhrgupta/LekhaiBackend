package in.lekhai.error.controller.stockitem.exception;

import in.lekhai.error.controller.LekhaiException;
import in.lekhai.shop.context.model.ShopContext;

public class StockItemNotFoundException extends LekhaiException {
    public StockItemNotFoundException(Long id) {
        super(String.format("StockItem id=[%s] not found for shopCode=[%s]", id, ShopContext.getShopCode()));
    }
}
