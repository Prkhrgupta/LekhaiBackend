package in.lekhai.cucumber;

import org.springframework.boot.autoconfigure.flyway.FlywayConnectionDetails;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.PostgreSQLContainer;

// Connection details must be beans: property-based overrides apply after Tomcat's filters have already built the DataSource.
@TestConfiguration(proxyBeanMethods = false)
@Profile("!" + CucumberDbProfileResolver.LOCAL_DB_PROFILE)
public class CucumberTestcontainersConfiguration {

    @Bean
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("lekhai_test")
                .withUsername("postgres")
                .withPassword("postgres")
                .withInitScript("db/cucumber-container-init.sql");
    }

    @Bean
    JdbcConnectionDetails appConnectionDetails(PostgreSQLContainer<?> postgres) {
        return new JdbcConnectionDetails() {
            @Override
            public String getUsername() {
                return "lekhai_user";
            }

            @Override
            public String getPassword() {
                return "password";
            }

            @Override
            public String getJdbcUrl() {
                return postgres.getJdbcUrl();
            }
        };
    }

    @Bean
    FlywayConnectionDetails flywayConnectionDetails(PostgreSQLContainer<?> postgres) {
        return new FlywayConnectionDetails() {
            @Override
            public String getUsername() {
                return postgres.getUsername();
            }

            @Override
            public String getPassword() {
                return postgres.getPassword();
            }

            @Override
            public String getJdbcUrl() {
                return postgres.getJdbcUrl();
            }
        };
    }
}
