package in.lekhai.gsp.ewb.infrastructure.taxpro.service;

import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.gsp.ewb.domain.entity.GspUserCredentials;
import in.lekhai.gsp.ewb.domain.repository.GspUserCredentialsRepo;
import in.lekhai.gsp.ewb.infrastructure.taxpro.client.AuthTaxProClient;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProAuthResponse;
import in.lekhai.gsp.ewb.infrastructure.taxpro.exceptions.TaxProUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;

@Service
public class TaxProAuthService {
    private final AuthTaxProClient authTaxProClient;
    private final GspUserCredentialsRepo gspUserCredentialsRepo;
    private final ShopsRepo shopsRepo;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public TaxProAuthService(AuthTaxProClient authTaxProClient,
                             GspUserCredentialsRepo gspUserCredentialsRepo,
                             ShopsRepo shopsRepo) {
        this.authTaxProClient = authTaxProClient;
        this.gspUserCredentialsRepo = gspUserCredentialsRepo;
        this.shopsRepo = shopsRepo;
    }

    @Cacheable(value = "ewb", key = "#shopCode")
    public String getEwbAuthToken(Integer shopCode) {
        Optional<GspUserCredentials> gspUserCredentialsOptional = gspUserCredentialsRepo.findByShopCode(shopCode);
        if(gspUserCredentialsOptional.isEmpty()) {
            // TODO : create a new TaxProException
            throw new RuntimeException(String.format("No GSP credentials found for shop : %s, Create credentials", shopCode));
        }
        String gstNumber = shopsRepo.findByShopCode(shopCode).get().getGstNumber();
        log.info("Starting to generate access token for gstIn : [{}]", gstNumber);

        TaxProAuthResponse taxProAuthResponse = authTaxProClient.getAccessToken(
                        gspUserCredentialsOptional.get().getUserName(),
                        gspUserCredentialsOptional.get().getPassword(),
                        gstNumber)
                .block();

        if(taxProAuthResponse == null || taxProAuthResponse.status() == 0) {
            // TODO : TaxProException
            throw new RuntimeException("TaxPro API failed");
        }

        return taxProAuthResponse.authToken();
    }

    @CacheEvict(value = "ewb", key = "#shopCode")
    public void evictToken(Integer shopCode) {
        log.info("Evicting token for shopCode={}", shopCode);
    }

    //TODO : Implement this while calling all the TaxPro apis
    public <T> Mono<T> executeWithTokenRetry(
            Integer shopCode,
            Function<String, Mono<T>> apiCall
    ) {

        return Mono.defer(() -> {

            String token = getEwbAuthToken(shopCode);

            return apiCall.apply(token)
                    .onErrorResume(TaxProUnauthorizedException.class, ex -> {

                        log.warn("Token expired for shopCode={}, refreshing...", shopCode);

                        // evict old token
                        evictToken(shopCode);

                        String newToken = getEwbAuthToken(shopCode);

                        // retry once
                        return apiCall.apply(newToken)
                                .onErrorResume(TaxProUnauthorizedException.class,
                                        e -> Mono.error(new RuntimeException("Token refresh failed")));
                    });
        });
    }

}
