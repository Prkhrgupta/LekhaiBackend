package in.lekhai.authentication.controller;

import in.lekhai.authentication.entity.UserCredentials;
import in.lekhai.authentication.model.LoginRequest;
import in.lekhai.authentication.repository.UserCredentialRepository;
import in.lekhai.authentication.service.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/lekhai")
public class LoginController {

    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    public LoginController(
            JwtTokenService jwtTokenService,
            AuthenticationManager authenticationManager
    ) {
        this.jwtTokenService = jwtTokenService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public String generateToken(Authentication authentication) {
        return jwtTokenService.generateJwtToken(authentication);
    }

}
