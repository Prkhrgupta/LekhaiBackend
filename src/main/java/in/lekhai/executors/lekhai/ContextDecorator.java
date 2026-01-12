package in.lekhai.executors.lekhai;

import in.lekhai.shop.context.model.ShopContext;
import org.springframework.core.task.TaskDecorator;

public class ContextDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnableTask) {
        Integer tenantId = ShopContext.getShopCode();
        return () -> {
            try {
                ShopContext.setShopCode(tenantId);
                runnableTask.run();
            } finally {
                ShopContext.clear();
            }
        };
    }
}
