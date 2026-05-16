package in.lekhai.gsp.ewb.infrastructure.taxpro.client;

import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthTaxProClient {

    private final WebClient webClient;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public AuthTaxProClient(@Qualifier("taxProWebClient") WebClient taxProWebClient) {

        this.webClient = taxProWebClient;
    }

    public Mono<TaxProAuthResponse> getAccessToken(String userName,
                                                   String userPassword,
                                                   String gstIn) {
        log.info("Access token request to TaxPro for user=[{}] and gstin=[{}]", userName, gstIn);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/eivital/dec/v1.04/auth")
                        .queryParam("action", "AUTH")
                        .queryParam("gstin", gstIn)
                        .queryParam("user_name", userName)
                        .queryParam("eInvPwd", userPassword)
                        .build()
                )
                .retrieve()
                .bodyToMono(TaxProAuthResponse.class);
    }
}