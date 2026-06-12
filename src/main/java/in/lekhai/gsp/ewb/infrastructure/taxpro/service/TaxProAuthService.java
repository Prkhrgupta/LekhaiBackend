package in.lekhai.gsp.ewb.infrastructure.taxpro.service;

import in.lekhai.core.domain.shop.Shops;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.gsp.ewb.domain.entity.GspUserCredentials;
import in.lekhai.gsp.ewb.infrastructure.taxpro.client.AuthTaxProClient;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProAuthResponse;
import in.lekhai.gsp.ewb.repository.GspUserCredentialsRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
            log.error("No GSP credential present for shopCode=[{}]", shopCode);
            throw new LekhaiClientException("No GSP credentials found. Create credentials", HttpStatus.NOT_FOUND);
        }
        Shops shopOptional = shopsRepo.findByShopCode(shopCode)
                .orElseThrow(() -> new IllegalStateException(String.format("Invalid shopCode=%s in Ewb auth process", shopCode)));
        String gstNumber = shopOptional.getGstNumber();
        TaxProAuthResponse taxProAuthResponse = authTaxProClient.getAccessToken(
                        gspUserCredentialsOptional.get().getUserName(),
                        gspUserCredentialsOptional.get().getPassword(),
                        gstNumber)
                .block();

        if(taxProAuthResponse == null || taxProAuthResponse.status() == 0) {
            // TODO : TaxProException
            throw new RuntimeException("TaxPro API failed");
        }

        return taxProAuthResponse.data().authToken();
    }
}
