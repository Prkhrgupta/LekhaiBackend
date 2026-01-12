package in.lekhai.shop.context.transaction.manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import in.lekhai.shop.context.transaction.manager.ShopContextTransactionManager;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
public class ShopContextTransactionManagerConfig {

    @Bean
    @Primary
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean("shop-context-transaction-manager")
    public ShopContextTransactionManager shopContextTransactionManager(DataSource dataSource) {
        return new ShopContextTransactionManager(dataSource);
    }
}
