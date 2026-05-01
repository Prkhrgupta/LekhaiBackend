package in.lekhai.gsp.ewb.infrastructure.taxpro.service;

import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.gsp.ewb.domain.entity.GspUserCredentials;
import in.lekhai.gsp.ewb.domain.repository.GspUserCredentialsRepo;
import in.lekhai.gsp.ewb.infrastructure.taxpro.client.AuthTaxProClient;
import in.lekhai.gsp.ewb.infrastructure.taxpro.dto.TaxProAuthResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TaxProAuthService {
    private final AuthTaxProClient authTaxProClient;
    private final GspUserCredentialsRepo gspUserCredentialsRepo;
    private final ShopsRepo shopsRepo;

    public TaxProAuthService(AuthTaxProClient authTaxProClient,
                             GspUserCredentialsRepo gspUserCredentialsRepo,
                             ShopsRepo shopsRepo) {
        this.authTaxProClient = authTaxProClient;
        this.gspUserCredentialsRepo = gspUserCredentialsRepo;
        this.shopsRepo = shopsRepo;
    }

    public String getEwbAuthToken(Integer shopCode) {
        Optional<GspUserCredentials> gspUserCredentialsOptional = gspUserCredentialsRepo.findByShopCode(shopCode);
        if(gspUserCredentialsOptional.isEmpty()) {
            // TODO : create a new TaxProException
            throw new RuntimeException(String.format("No GSP credentials found for shop : %s, Create credentials", shopCode));
        }
        String gstNumber = shopsRepo.findByShopCode(shopCode).get().getGstNumber();

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
}
