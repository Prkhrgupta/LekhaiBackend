package in.lekhai.error.controller.generalledgersetting.exception;

import in.lekhai.error.controller.LekhaiException;
import in.lekhai.shop.context.model.ShopContext;

public class GeneralLedgerSettingNotFoundException extends LekhaiException {
    public GeneralLedgerSettingNotFoundException() {
        super(String.format("GeneralLedgerSetting not found for shopCode=[%s]", ShopContext.getShopCode()));
    }
}
