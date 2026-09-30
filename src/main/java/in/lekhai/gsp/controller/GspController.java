package in.lekhai.gsp.controller;

import in.lekhai.contract.api.GspCredentialsApi;
import in.lekhai.contract.model.GspCredentialsRequest;
import in.lekhai.contract.model.GspCredentialsResponse;
import in.lekhai.gsp.ewb.service.GspCredentialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GspController implements GspCredentialsApi {
    private final GspCredentialService gspCredentialService;

    public GspController(GspCredentialService gspCredentialService) {
        this.gspCredentialService = gspCredentialService;
    }

    @Override
    public ResponseEntity<GspCredentialsResponse> upsertGspCredentials(GspCredentialsRequest gspCredentialsRequest) {
        GspCredentialsResponse response = gspCredentialService.upsertGspEwbCredentials(gspCredentialsRequest);
        return ResponseEntity.ok(response);
    }
}
