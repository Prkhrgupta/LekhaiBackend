package tenant.context.transaction.manager;

import tenant.context.model.TenantContext;
import in.lekhai.error.controller.LekhaiException;
import in.lekhai.error.controller.tenant.exception.InvalidTenantIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class TenantIdTransactionManager extends DataSourceTransactionManager {
    private final Logger log = LoggerFactory.getLogger(TenantIdTransactionManager.class);

    public TenantIdTransactionManager(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin(transaction, definition);

        Integer tenantId = TenantContext.getTenantId();
        if (Objects.isNull(tenantId)) {
            throw new InvalidTenantIdException();
        }

        ConnectionHolder connectionHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(getDataSource());
        if (Objects.isNull(connectionHolder)) {
            throw new LekhaiException(String.format("Connection null for transaction :: tenant id: [%s]", tenantId));
        }

        Connection connection = connectionHolder.getConnection();
        try {
            setTenantContext(connection, tenantId);
        } catch (SQLException exception) {
            log.error("Failed to set postgres local context for tenant [{}]", tenantId);
            throw new LekhaiException(String.format("Failed to set tenant id [%s]", tenantId), exception);
        }
    }

     // Sets tenant context on the connection
    private void setTenantContext(Connection connection, Integer tenantId) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(String.format("SET LOCAL app.tenant_id = '%s'", tenantId));

            // TODO: info >> debug
            log.info("Set tenant id [{}] for current transaction", tenantId);
        }
    }

    /**
     * Called by Spring AFTER commit/rollback but BEFORE connection is returned to pool
     * This is called whether the transaction succeeded or failed
     * Order of Spring's calls:
     * 1. doBegin()
     * 2. [business logic executes]
     * 3. doCommit() OR doRollback()  ← Spring already handled transaction here
     * 4. doCleanupAfterCompletion()  ← We are here
     * 5. Connection released to pool  ← Happens in super.doCleanupAfterCompletion()
     */
    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        ConnectionHolder connectionHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(getDataSource());
        if (Objects.nonNull(connectionHolder)) {
            connectionHolder.getConnection();
            try {
                cleanupTenantContext(connectionHolder.getConnection());
            } catch (Exception e) {
                log.warn("Failed to cleanup tenant context, connection pool will validate", e);
            }
        }
        super.doCleanupAfterCompletion(transaction);
    }

    private void cleanupTenantContext(Connection connection) throws SQLException {
        if (connection.isClosed()) {
            log.debug("Connection already closed during cleanup");
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("RESET app.tenant_id");
            log.trace("Reset tenant context session variables");
        }
    }
}
