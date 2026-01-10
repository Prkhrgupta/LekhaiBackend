package in.lekhai.executors.lekhai;

import tenant.context.model.TenantContext;
import org.springframework.core.task.TaskDecorator;

public class ContextDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnableTask) {
        Integer tenantId = TenantContext.getTenantId();
        return () -> {
            try {
                TenantContext.setTenantId(tenantId);
                runnableTask.run();
            } finally {
                TenantContext.clear();
            }
        };
    }
}
