package in.lekhai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

@Slf4j
@SpringBootApplication
@EnableJdbcAuditing
public class LekhaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LekhaiApplication.class, args);
    }

}
