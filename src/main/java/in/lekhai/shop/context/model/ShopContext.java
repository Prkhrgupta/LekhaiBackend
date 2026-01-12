package in.lekhai.shop.context.model;

import in.lekhai.error.controller.shop.exception.InvalidShopCodeException;

import java.util.Objects;

public final class ShopContext {

    private static final ThreadLocal<Integer> shopContext = new ThreadLocal<>();

    public static void setShopCode(Integer value) {
        if(Objects.isNull(value)) {
            throw new InvalidShopCodeException();
        }
        shopContext.set(value);
    }

    public static Integer getShopCode() {
        return shopContext.get();
    }

    public static void clear() {
        shopContext.remove();
    }
}
