package in.lekhai.gsp.ewb.config;

import in.lekhai.gsp.ewb.domain.port.EwbProvider;
import in.lekhai.gsp.ewb.infrastructure.taxpro.TaxProEwbProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EwbConfig {
    @Bean
    public EwbProvider ewbProvider(@Value("${provider.ewb}") String provider,
                                   TaxProEwbProvider taxProEwbProvider) {

        if ("tax-pro".equalsIgnoreCase(provider)) {
            return taxProEwbProvider;
        }

        throw new IllegalArgumentException(
                "Unsupported EWB provider: " + provider);
    }
}