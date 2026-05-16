package in.lekhai.gsp.shared;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "tax-pro")
public record TaxProProperties(
        String baseUrl,
        EwbTaxPro ewb,
        Credentials credentials
) {

    public record EwbTaxPro(
            Timeouts timeouts
    ) { }

    public record Credentials(
            String aspId,
            String aspPassword
    ) { }

    public record Timeouts(
            Duration connect,
            Duration read
    ) { }

}
