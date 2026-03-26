package in.lekhai.authentication.controller;

import in.lekhai.authentication.service.JwtTokenService;
import in.lekhai.contract.api.AuthApi;
import in.lekhai.contract.model.LoginResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController implements AuthApi {

    private final JwtTokenService jwtTokenService;

    public LoginController(
            JwtTokenService jwtTokenService
    ) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public ResponseEntity<LoginResponse> login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginResponse response = jwtTokenService.generateShopJwtToken(authentication);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<LoginResponse> generateTokenForShopCode(@NotNull Integer shopCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginResponse response = jwtTokenService.generateShopJwtToken(authentication, shopCode);
        return ResponseEntity.ok(response);
    }
}
