package in.lekhai.gsp.ewb.infrastructure.taxpro.config;

import in.lekhai.gsp.shared.TaxProProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Configuration
public class TaxProConfiguration {
    @Bean("taxProWebClient")
    public WebClient taxProWebClient(WebClient.Builder defaultWebClient,
                                     TaxProProperties taxProProperties)  {
        return defaultWebClient
                .baseUrl(taxProProperties.baseUrl())
                .filter((request, next) -> {
                    URI newUri = UriComponentsBuilder.fromUri(request.url())
                            .queryParam("aspid", taxProProperties.credentials().aspId())
                            .queryParam("password", taxProProperties.credentials().aspPassword())
                            .build(true)
                            .toUri();

                    ClientRequest newRequest = ClientRequest.from(request)
                            .url(newUri)
                            .build();

                    return next.exchange(newRequest);
                })
                .build();
    }
}
