package in.lekhai.gsp.ewb.infrastructure.taxpro.client;

import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProErrorResponse;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProEwbDetailResponse;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProEwbForTransporterResponse;
import in.lekhai.gsp.ewb.infrastructure.taxpro.exceptions.TaxProUnauthorizedException;
import in.lekhai.gsp.shared.TaxProProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class EwbTaxproWebClient {

    private final WebClient webClient;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final static String UNAUTHORIZED_ERROR_CODE = "GSP102";

    public EwbTaxproWebClient(WebClient.Builder builder,
                              TaxProProperties taxProProperties) {

        this.webClient = builder
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

    public Mono<List<TaxProEwbForTransporterResponse>> getEwbsForTransporter(
            String gstIn,
            String authToken,
            Instant date) {

        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .withZone(ZoneId.of("Asia/Kolkata"));

        String formattedDate = dateFormatter.format(date);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1.03/dec/ewayapi")
                        .queryParam("action", "GetEwayBillsForTransporter")
                        .queryParam("gstin", gstIn)
                        .queryParam("authtoken", authToken)
                        .queryParam("date", formattedDate)
                        .build()
                )
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(TaxProErrorResponse.class)
                                .flatMap(errorResponse -> {
                                    if (UNAUTHORIZED_ERROR_CODE.equals(errorResponse.error().error_cd())) {
                                        return Mono.error(new TaxProUnauthorizedException("Token expired"));
                                    }
                                    return Mono.error(new RuntimeException(
                                            errorResponse.error().message()
                                    ));
                                })
                )
                .bodyToFlux(TaxProEwbForTransporterResponse.class)
                .collectList();
    }

    public Mono<TaxProEwbDetailResponse> getEwbDetailsByEwbNo(String ewbNo,
                                                              String gstin,
                                                              String authToken
    ) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1.03/dec/ewayapi")
                        .queryParam("action", "GetEwayBill")
                        .queryParam("gstin", gstin)
                        .queryParam("ewbNo", ewbNo)
                        .queryParam("authtoken", authToken)
                        .build()
                )
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(TaxProErrorResponse.class)
                                .flatMap(errorResponse -> {
                                    if (UNAUTHORIZED_ERROR_CODE.equals(errorResponse.error().error_cd())) {
                                        return Mono.error(new TaxProUnauthorizedException("Token expired"));
                                    }
                                    return Mono.error(new RuntimeException(
                                            errorResponse.error().message()
                                    ));
                                })
                )
                .bodyToMono(TaxProEwbDetailResponse.class);
    }
}