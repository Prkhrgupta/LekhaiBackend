package in.lekhai.core.account_master.seeders;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

@Component
public class StatesSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StatesSeeder.class);

    private final DataSource dataSource;
    private final ResourceLoader resourceLoader;
    private final JdbcTemplate jdbcTemplate;

    public StatesSeeder(DataSource dataSource, ResourceLoader resourceLoader, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.resourceLoader = resourceLoader;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedStates();
    }

    private void seedStates() {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM state", Integer.class);
        if (count != null && count > 0) {
            log.info("States already seeded. Skipping.");
            return;
        }

        log.info("Seeding states via COPY...");
        try (Connection connection = dataSource.getConnection();
                Reader reader = new InputStreamReader(
                        resourceLoader.getResource("classpath:seeds/states.csv").getInputStream(),
                        StandardCharsets.UTF_8)) {

            BaseConnection pgConnection = connection.unwrap(BaseConnection.class);
            CopyManager copyManager = new CopyManager(pgConnection);

            // CSV columns: state_code,state_name,gst_code,type
            copyManager.copyIn("COPY state (state_code, state_name, gst_code, type) FROM STDIN WITH CSV HEADER",
                    reader);

            log.info("Seeding states completed.");

        } catch (Exception e) {
            log.error("Failed to seed states", e);
        }
    }
}
