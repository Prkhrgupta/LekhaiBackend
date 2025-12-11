package in.lekhai.authentication.service;

import in.lekhai.authentication.entity.UserCredentials;
import in.lekhai.authentication.exception.UserDoesNotExistException;
import in.lekhai.authentication.repository.UserCredentialRepository;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class UserService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    public final UserCredentialRepository userCredentialRepository;

    public UserService(
            UserCredentialRepository userCredentialRepository
    ) {
        this.userCredentialRepository = userCredentialRepository;
    }

    @Override
    @SneakyThrows
    public UserDetails loadUserByUsername(String username) {
        UserCredentials userCredentials = userCredentialRepository
                .findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User '{}' does not exist", username);
                    return new UserDoesNotExistException(username);
                });

        return User.builder()
                .username(userCredentials.getUsername())
                .password(userCredentials.getPassHash())
                .authorities(getAuthorities(userCredentials))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(UserCredentials userCredentials) {
        return Collections.emptyList();
    }
}
