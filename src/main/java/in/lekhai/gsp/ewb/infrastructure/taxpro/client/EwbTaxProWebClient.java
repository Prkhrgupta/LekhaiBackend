package in.lekhai.gsp.ewb.infrastructure.taxpro.client;

import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.*;
import in.lekhai.gsp.ewb.infrastructure.taxpro.exceptions.TaxProUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class EwbTaxProWebClient {

    private final WebClient webClient;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final static String UNAUTHORIZED_ERROR_CODE = "GSP102";

    public EwbTaxProWebClient(@Qualifier("taxProWebClient") WebClient taxProWebClient) {
        this.webClient = taxProWebClient;
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
                                    if (UNAUTHORIZED_ERROR_CODE.equals(errorResponse.error().errorCd())) {
                                        return Mono.error(new TaxProUnauthorizedException("Token expired"));
                                    }
                                    log.error("Failed to fetch all ewb for transporter for gst : [{}] and date : [{}]", gstIn, date);
                                    return Mono.error(new RuntimeException(
                                            errorResponse.error().message()
                                    ));
                                })
                )
                .bodyToFlux(TaxProEwbForTransporterResponse.class)
                .collectList()
                .doOnNext((res) ->
                        log.info("Successfully fetched {} ewbs for gst : [{}] on date : [{}]", res.size(), gstIn, date));
    }

    public Mono<TaxProEwbDetailResponse> getEwbDetailsByEwbNo(
            Long ewbNo,
            String gstin,
            String authToken
    ) {
        log.info("Calling TaxPro ewbDetail API for ewbNo : [{}]", ewbNo);

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
                .bodyToMono(TaxProEwbDetailResponse.class)

                .retryWhen(
                        Retry.backoff(3, Duration.ofSeconds(2))
                                .filter(this::isTooManyRequests)
                                .doBeforeRetry(retrySignal ->
                                        log.warn(
                                                "Retrying TaxPro GetEwayBill for ewbNo : [{}], attempt : [{}]",
                                                ewbNo,
                                                retrySignal.totalRetries() + 1
                                        )
                                )
                )

                .doOnNext(res ->
                        log.info(
                                "Successfully fetched ewb details for ewbNo : [{}]",
                                res.ewbNo()
                        )
                );
    }

    private boolean isTooManyRequests(Throwable throwable) {
        return throwable instanceof WebClientResponseException ex
                && ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
    }
    public Mono<TaxProExtendValidityResponse> extendEwbValidity(TaxProExtendValidityRequest request,
                                                                String gstin,
                                                                String authToken) {
        log.info("Calling TaxPro extend validity API for ewbNo : [{}] with request : {}", request.ewbNo(), request);
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1.03/dec/ewayapi")
                        .queryParam("action", "EXTENDVALIDITY")
                        .queryParam("gstin", gstin)
                        .queryParam("authtoken", authToken)
                        .build()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(TaxProErrorResponse.class)
                                .flatMap(errorResponse -> {
                                    if (UNAUTHORIZED_ERROR_CODE.equals(errorResponse.error().errorCd())) {
                                        return Mono.error(new TaxProUnauthorizedException("Token expired"));
                                    }
                                    log.error("Failed to extend EwbNo {}, error : {}", request.ewbNo(), errorResponse);
                                    return Mono.error(new LekhaiClientException(
                                            errorResponse.error().message(), response.statusCode()
                                    ));
                                })
                )
                .bodyToMono(TaxProExtendValidityResponse.class)
                .doOnNext(res ->
                        log.info("Successfully extended validity for ewbNo : [{}] till : [{}]", res.ewayBillNo(), res.validUpto()));
    }
}