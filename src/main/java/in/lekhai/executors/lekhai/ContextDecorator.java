package in.lekhai.executors.lekhai;

import in.lekhai.authentication.model.TenantContext;
import org.springframework.core.task.TaskDecorator;

import java.net.StandardSocketOptions;

public class ContextDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnableTask) {
        String tenantId = TenantContext.getTenantId();
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
