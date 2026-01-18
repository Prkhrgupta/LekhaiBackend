package in.lekhai.authentication.service;

import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.authentication.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;
    private UserAccountRepository userAccountRepository;

    @BeforeEach
    void setUp() {
        userAccountRepository = mock(UserAccountRepository.class);
        userService = new UserService(userAccountRepository);
    }

    @Test
    void loadUserByUsername_Success() {
        // Arrange
        String username = "test-user";
        UserAccounts mockUser = new UserAccounts();
        mockUser.setUsername(username);

        when(userAccountRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // Act
        UserAccounts result = userService.loadUserByUsername(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        verify(userAccountRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        // Arrange
        String username = "non-existent-user";
        when(userAccountRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User non-existent-user does not exits"); // Note: typo in original code "exits"
    }
}
