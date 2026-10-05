package in.lekhai.licensing;

import in.lekhai.licensing.domain.LicensePayload;
import in.lekhai.licensing.domain.ShopLicenseProfile;
import in.lekhai.licensing.service.LicenseCryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LicenseCryptoServiceTest {

    private KeyPair keyPair;
    private LicenseCryptoService cryptoService;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        this.keyPair = kpg.generateKeyPair();
        String pubBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        this.cryptoService = new LicenseCryptoService(pubBase64);
    }

    @Test
    void shouldVerifyValidSignature() throws Exception {
        LicensePayload payload = new LicensePayload(
                "LIC-TEST-01",
                "Test Client",
                "admin@test.in",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30),
                List.of(new ShopLicenseProfile(101, "Test Shop", "GST123", "Delhi", "SAREE", 5)),
                Map.of("ewb", true)
        );

        String json = cryptoService.serializePayload(payload);

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(keyPair.getPrivate());
        sig.update(json.getBytes(StandardCharsets.UTF_8));
        String signatureBase64 = Base64.getEncoder().encodeToString(sig.sign());

        assertTrue(cryptoService.verifySignature(json, signatureBase64));
    }

    @Test
    void shouldRejectTamperedSignature() throws Exception {
        LicensePayload payload = new LicensePayload(
                "LIC-TEST-01",
                "Test Client",
                "admin@test.in",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30),
                List.of(new ShopLicenseProfile(101, "Test Shop", "GST123", "Delhi", "SAREE", 5)),
                Map.of("ewb", true)
        );

        String json = cryptoService.serializePayload(payload);

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(keyPair.getPrivate());
        sig.update(json.getBytes(StandardCharsets.UTF_8));
        String signatureBase64 = Base64.getEncoder().encodeToString(sig.sign());

        // Tamper with payload (e.g. increase users from 5 to 50)
        String tamperedJson = json.replace("\"maxUsers\":5", "\"maxUsers\":50");

        assertFalse(cryptoService.verifySignature(tamperedJson, signatureBase64));
    }
}
