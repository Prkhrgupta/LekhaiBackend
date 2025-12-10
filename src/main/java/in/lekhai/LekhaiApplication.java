package in.lekhai;

import in.lekhai.authentication.config.RsaKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

@EnableConfigurationProperties(RsaKeyProperties.class)
@SpringBootApplication
@EnableJdbcAuditing
public class LekhaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LekhaiApplication.class, args);
    }

}
