package in.lekhai.authentication.service;

import in.lekhai.authentication.entity.UserCredentials;
import in.lekhai.authentication.repository.UserCredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    public UserCredentials loadUserByUsername(String username) {
        return userCredentialRepository
                .findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User '{}' does not exist", username);
                    return new UsernameNotFoundException(String.format("User %s does not exits", username));
                });
    }

//    private Collection<? extends GrantedAuthority> getAuthorities(UserCredentials userCredentials) {
//        return Collections.emptyList();
//    }
}
