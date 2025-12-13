package in.lekhai.authentication.controller;

import in.lekhai.authentication.service.JwtTokenService;
import in.lekhai.common.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lekhai")
public class LoginController {

    private final JwtTokenService jwtTokenService;

    public LoginController(
            JwtTokenService jwtTokenService
    ) {
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<Result<?>> generateToken(Authentication authentication) {
        String token = jwtTokenService.generateJwtToken(authentication);
        return ResponseEntity.ok(Result.success(token));
    }
}
