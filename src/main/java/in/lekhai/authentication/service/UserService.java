package in.lekhai.authentication.service;

import in.lekhai.authentication.entity.UserCredentials;
import in.lekhai.authentication.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Collections;

public class UserService implements UserDetailsService {

    public final UserCredentialRepository userCredentialRepository;

    public UserService(
            UserCredentialRepository userCredentialRepository
    ) {
        this.userCredentialRepository = userCredentialRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserCredentials userCredentials = userCredentialRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException(String.format("User %s not found", username))
        );

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
