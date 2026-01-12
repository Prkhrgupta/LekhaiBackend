package in.lekhai.shop.context.transaction.manager;

import jakarta.annotation.PostConstruct;
import org.springframework.lang.NonNull;
import in.lekhai.shop.context.model.ShopContext;
import in.lekhai.error.controller.LekhaiException;
import in.lekhai.error.controller.shop.exception.InvalidShopCodeException;
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

import static in.lekhai.common.SuperAdminConstants.SUPER_ADMIN_SHOP_CODE;

public class ShopContextTransactionManager extends DataSourceTransactionManager {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public ShopContextTransactionManager(DataSource dataSource) {
        super(dataSource);
        log.info("ShopContextTransactionManager instantiated");
    }

    @PostConstruct
    public void init() {
        log.info("ShopContextTransactionManager bean initialized");
    }

    @Override
    protected void doBegin(@NonNull Object transaction, @NonNull TransactionDefinition definition) {
        super.doBegin(transaction, definition);

        Integer shopCode = ShopContext.getShopCode();
        if (Objects.isNull(shopCode)) {
            log.error("Null shop context for transaction :: {} :: {}", transaction, definition);
            throw new InvalidShopCodeException();
        }

        ConnectionHolder connectionHolder = (ConnectionHolder) TransactionSynchronizationManager
                .getResource(Objects.requireNonNull(getDataSource()));
        if (Objects.isNull(connectionHolder)) {
            throw new LekhaiException(String.format("Connection null for transaction :: shop code : [%s]", shopCode));
        }

        Connection connection = connectionHolder.getConnection();
        try {
            setShopContext(connection, shopCode);
        } catch (SQLException exception) {
            log.error("Failed to set postgres local context for shop [{}]", shopCode);
            throw new LekhaiException(String.format("Failed to set context for shop [%s]", shopCode), exception);
        }
    }

     // Sets shop context on the connection
    private void setShopContext(Connection connection, Integer shopCode) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String bypassRLS = superAdminBypass(shopCode);
            statement.execute(String.format("SET LOCAL app.shop_code = '%s'", shopCode));
            statement.execute(String.format("SET LOCAL app.bypass_rls = '%s'", bypassRLS));
            log.info("Set shop code [{}] for current transaction", shopCode);                   // TODO: info >> debug
        }
    }

    private String superAdminBypass(Integer shopCode) {
        if(shopCode.equals(SUPER_ADMIN_SHOP_CODE)) { return "true";
        } else {
            return "false";
        }
    }

    /**
     * Called by Spring AFTER commit/rollback but BEFORE connection is returned to pool.
     * This is called whether the transaction succeeded or failed
     * Order of Spring's calls ->
     * 1. doBegin()
     * 2. [business logic executes]
     * 3. doCommit() OR doRollback()  ← Spring already handled transaction here
     * 4. doCleanupAfterCompletion()  ← We are here
     * 5. Connection released to pool  ← Happens in super.doCleanupAfterCompletion()
     */
    @Override
    protected void doCleanupAfterCompletion(@NonNull Object transaction) {
        ConnectionHolder connectionHolder = (ConnectionHolder) TransactionSynchronizationManager
                .getResource(Objects.requireNonNull(getDataSource()));
        if (Objects.nonNull(connectionHolder)) {
            connectionHolder.getConnection();
            try {
                cleanupShopContext(connectionHolder.getConnection());
            } catch (Exception exception) {
                log.error("Failed to cleanup shop context, connection pool will validate :: {}",
                        exception.getMessage(), exception);
            }
        }
        super.doCleanupAfterCompletion(transaction);
    }

    private void cleanupShopContext(Connection connection) throws SQLException {
        if (connection.isClosed()) {
            log.info("Connection already closed during cleanup");
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("RESET app.shop_code");
            log.trace("Reset shop context session variables");
        }
    }
}
