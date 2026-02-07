package in.lekhai.authentication.controller;

import in.lekhai.authentication.dto.LoginResponse;
import in.lekhai.authentication.service.JwtTokenService;
import in.lekhai.common.Result;
import in.lekhai.core.account_master.dto.ledger.LedgerSummaryResponse;
import in.lekhai.core.account_master.service.LedgerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lekhai")
public class LoginController {

    private final JwtTokenService jwtTokenService;
    private final LedgerService ledgerService;

    public LoginController(
            JwtTokenService jwtTokenService,
            LedgerService ledgerService) {
        this.jwtTokenService = jwtTokenService;
        this.ledgerService = ledgerService;
    }

    @GetMapping("/login")
    public ResponseEntity<Result<?>> generateToken(Authentication authentication) {
        LoginResponse response = jwtTokenService.generateJwtToken(authentication);
        return ResponseEntity.ok(Result.success(response));
    }

    @GetMapping("/login/withShopCode")
    public ResponseEntity<Result<?>> generateTokenWithShopCode(Authentication authentication,
            @RequestHeader("shop-code") Integer shopCode) {
        LoginResponse response = jwtTokenService.generateJwtToken(authentication, shopCode);
        return ResponseEntity.ok(Result.success(response));
    }

    @GetMapping("/ledgers/summary")
    public ResponseEntity<Result<LedgerSummaryResponse>> getLedgerSummaries() {
        LedgerSummaryResponse response = ledgerService.listLedgerSummaries();
        return ResponseEntity.ok(Result.success(response));
    }
}
