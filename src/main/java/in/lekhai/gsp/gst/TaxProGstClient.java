package in.lekhai.gsp.gst;

import in.lekhai.gsp.ewb.infrastructure.taxpro.service.TaxProAuthService;
import in.lekhai.gsp.gst.dto.GstVerificationResponseDto;
import in.lekhai.shop.context.model.ShopContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TaxProGstClient {
    private final WebClient webClient;
    private final TaxProAuthService taxProAuthService;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public TaxProGstClient(
            @Qualifier("taxProWebClient") WebClient taxProWebClient,
            TaxProAuthService taxProAuthService
    ) {

        this.webClient = taxProWebClient;
        this.taxProAuthService = taxProAuthService;
    }

    public Mono<GstVerificationResponseDto> getGstDetails(String gstIn) {
        String taxProAuthToken = taxProAuthService.getEwbAuthToken(ShopContext.getShopCode());
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/eivital/dec/v1.04/Master/gstin")
                        .queryParam("action", "GetEwayBill")
                        .queryParam("gstin", gstIn)
                        .queryParam("authtoken", taxProAuthToken)
                        .build()
                )
                .retrieve()
                .bodyToMono(GstVerificationResponseDto.class);
    }
}
