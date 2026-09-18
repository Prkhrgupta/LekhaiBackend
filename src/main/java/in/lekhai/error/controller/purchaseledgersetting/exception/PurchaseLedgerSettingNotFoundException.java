package in.lekhai.error.controller.purchaseledgersetting.exception;

import in.lekhai.error.controller.LekhaiException;
import in.lekhai.shop.context.model.ShopContext;

public class PurchaseLedgerSettingNotFoundException extends LekhaiException {
    public PurchaseLedgerSettingNotFoundException(Long id) {
        super(String.format("PurchaseLedgerSetting id=[%s] not found for shopCode=[%s]", id, ShopContext.getShopCode()));
    }
}
