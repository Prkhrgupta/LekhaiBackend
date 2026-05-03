package in.lekhai.gsp.ewb.infrastructure.taxpro.client;

import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProAuthResponse;
import in.lekhai.gsp.shared.TaxProProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class AuthTaxProClient {

    private final WebClient webClient;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public AuthTaxProClient(WebClient.Builder webClientBuilder,
                            TaxProProperties taxProProperties) {

        this.webClient = webClientBuilder
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

    public Mono<TaxProAuthResponse> getAccessToken(String userName,
                                                   String userPassword,
                                                   String gstIn) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1.03/dec/auth")
                        .queryParam("action", "ACCESSTOKEN")
                        .queryParam("gstin", gstIn)
                        .queryParam("username", userName)
                        .queryParam("ewbpwd", userPassword)
                        .build()
                )
                .retrieve()
                .bodyToMono(TaxProAuthResponse.class);
    }
}