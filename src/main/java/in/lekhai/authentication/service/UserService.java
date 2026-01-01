package in.lekhai.authentication.service;

import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.authentication.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    public final UserAccountRepository userAccountRepository;

    public UserService(
            UserAccountRepository userAccountRepository
    ) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserAccounts loadUserByUsername(String username) {
        return userAccountRepository
                .findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User '{}' does not exist", username);
                    return new UsernameNotFoundException(String.format("User %s does not exits", username));
                });
    }
}
