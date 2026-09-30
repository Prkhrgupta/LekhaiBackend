package in.lekhai.error.controller.uom.exception;

import in.lekhai.error.controller.LekhaiException;
import in.lekhai.shop.context.model.ShopContext;

public class UomInUseException extends LekhaiException {
    public UomInUseException(Long id) {
        super(String.format(
                "UOM id=[%s] is used by stock items for shopCode=[%s] and cannot be deleted",
                id, ShopContext.getShopCode()));
    }

    public UomInUseException(Long id, String reason) {
        super(String.format(
                "UOM id=[%s] is used by stock items for shopCode=[%s]: %s",
                id, ShopContext.getShopCode(), reason));
    }
}
