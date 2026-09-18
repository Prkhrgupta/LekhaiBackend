package in.lekhai.error.controller.commodity.exception;

import in.lekhai.error.controller.LekhaiException;
import in.lekhai.shop.context.model.ShopContext;

public class CommodityNotFoundException extends LekhaiException {
    public CommodityNotFoundException(Long id) {
        super(String.format("Commodity id=[%s] not found for shopCode=[%s]", id, ShopContext.getShopCode()));
    }
}
