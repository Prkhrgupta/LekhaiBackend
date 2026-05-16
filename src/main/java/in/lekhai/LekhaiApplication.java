package in.lekhai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJdbcAuditing
@ConfigurationPropertiesScan
@EnableScheduling
@EnableCaching
public class LekhaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LekhaiApplication.class, args);
    }

}
