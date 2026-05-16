package in.lekhai.gsp.ewb.service;

import in.lekhai.contract.model.GspCredentialsRequest;
import in.lekhai.contract.model.GspCredentialsResponse;
import in.lekhai.core.domain.shop.Shops;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.gsp.ewb.domain.entity.GspUserCredentials;
import in.lekhai.gsp.ewb.domain.repository.GspUserCredentialsRepo;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GspCredentialService {
    private final GspUserCredentialsRepo gspUserCredentialsRepo;
    private final ShopsRepo shopsRepo;

    public GspCredentialService(GspUserCredentialsRepo gspUserCredentialsRepo,
                                ShopsRepo shopsRepo) {
        this.gspUserCredentialsRepo = gspUserCredentialsRepo;
        this.shopsRepo = shopsRepo;
    }

    @ShopContextTransactional
    public GspCredentialsResponse upsertGspEwbCredentials(GspCredentialsRequest request) {
        Integer shopCode = JwtUtil.extractJwtClaim().shopCode();
        Optional<Shops> shop = shopsRepo.findByShopCode(shopCode);
        if(shop.isEmpty()) {
            throw new RuntimeException("Something went wrong, shopCode not registered but accessed");
        }
        GspUserCredentials gspUserCredentials = new GspUserCredentials();

        gspUserCredentials.setUserName(request.getUsername());
        gspUserCredentials.setShopCode(shopCode);
        gspUserCredentials.setPassword(request.getPassword());
        gspUserCredentialsRepo.save(gspUserCredentials);

        GspCredentialsResponse response = new GspCredentialsResponse();
        response.setGstin(shop.get().getGstNumber());
        response.setMessage(String.format("Successfully saved/updated credentials for gstIn : %s", request.getGstin()));
        return response;
    }

}
