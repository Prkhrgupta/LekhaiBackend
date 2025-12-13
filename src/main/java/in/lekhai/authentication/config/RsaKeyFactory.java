package in.lekhai.authentication.config;

import in.lekhai.authentication.config.properties.RsaKeyConfigProperties;
import in.lekhai.authentication.config.properties.RsaKeyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class RsaKeyFactory {

    private final RsaKeyConfigProperties config;

    public RsaKeyFactory(
            RsaKeyConfigProperties  rsaKeyConfigProperties
    ) {
        this.config = rsaKeyConfigProperties;
    }

    @Bean
    public RsaKeyProperties rsaKeyProperties() throws Exception {
        return new RsaKeyProperties(parsePublicKey(config.publicKey()), parsePrivateKey(config.privateKey()));
    }

    private RSAPublicKey parsePublicKey(String key) throws Exception {
        byte[] encoded = Base64.getDecoder().decode(key);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    private RSAPrivateKey parsePrivateKey(String key) throws Exception {
        byte[] encoded = Base64.getDecoder().decode(key);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }
}
