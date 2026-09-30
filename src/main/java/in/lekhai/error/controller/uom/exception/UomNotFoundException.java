package in.lekhai.error.controller.uom.exception;

import in.lekhai.error.controller.LekhaiException;
import in.lekhai.shop.context.model.ShopContext;

public class UomNotFoundException extends LekhaiException {
    public UomNotFoundException(Long id) {
        super(String.format("UOM id=[%s] not found for shopCode=[%s]", id, ShopContext.getShopCode()));
    }
}
