package in.lekhai.licensing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.lekhai.licensing.domain.LicensePayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class LicenseCryptoService {

    private final PublicKey publicKey;
    private final ObjectMapper objectMapper;

    public LicenseCryptoService(@Value("${licensing.public-key}") String publicKeyBase64) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64.replaceAll("\\s", ""));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            this.publicKey = kf.generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize licensing public key: " + e.getMessage(), e);
        }
    }

    public boolean verifySignature(String payloadJson, String signatureBase64) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(this.publicKey);
            signature.update(payloadJson.trim().getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64.replaceAll("\\s", ""));
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }

    public LicensePayload parsePayload(String payloadJson) {
        try {
            return objectMapper.readValue(payloadJson, LicensePayload.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid license JSON payload: " + e.getMessage(), e);
        }
    }

    public String serializePayload(LicensePayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize license payload", e);
        }
    }
}
