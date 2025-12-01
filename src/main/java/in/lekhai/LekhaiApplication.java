package in.lekhai;

import in.lekhai.authentication.repository.UserCredentialRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication
public class LekhaiApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext run = SpringApplication.run(LekhaiApplication.class, args);
		UserCredentialRepository userCredentialRepository = run.getBean(UserCredentialRepository.class);
		userCredentialRepository.findByUsername("abcd").get();
		log.error("herere");

	}

}
