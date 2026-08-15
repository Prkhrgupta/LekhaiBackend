package in.lekhai.error.controller.saleledgersetting.exception;

import in.lekhai.error.controller.LekhaiException;
import in.lekhai.shop.context.model.ShopContext;

public class SaleLedgerSettingNotFoundException extends LekhaiException {
    public SaleLedgerSettingNotFoundException(Long id) {
        super(String.format("SaleLedgerSetting id=[%s] not found for shopCode=[%s]", id, ShopContext.getShopCode()));
    }
}
