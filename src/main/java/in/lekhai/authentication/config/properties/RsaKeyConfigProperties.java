package in.lekhai.authentication.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rsa")
public record RsaKeyConfigProperties(String publicKey, String privateKey) {
}
