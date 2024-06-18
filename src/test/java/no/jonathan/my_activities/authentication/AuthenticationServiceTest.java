package no.jonathan.my_activities.authentication;

import jakarta.mail.MessagingException;
import net.datafaker.Faker;
import no.jonathan.my_activities.email.EmailService;
import no.jonathan.my_activities.exception.EmailAlreadyTakenException;
import no.jonathan.my_activities.exception.TokenNotFoundException;
import no.jonathan.my_activities.exception.UserAlreadyEnabledException;
import no.jonathan.my_activities.security.JwtService;
import no.jonathan.my_activities.token.Token;
import no.jonathan.my_activities.token.TokenRepository;
import no.jonathan.my_activities.user.User;
import no.jonathan.my_activities.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quickperf.junit5.QuickPerfTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@QuickPerfTest
class AuthenticationServiceTest {

    private final Faker faker = new Faker();

    private final String commonEmail = faker.internet().emailAddress();

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private AuthenticationService underTest;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .password(faker.internet().password())
                .enabled(false)
                .build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("It should register a user")
    void canRegister() throws MessagingException {
        // Given
        var registerRequest = new RegistrationRequest(
                faker.name().firstName(),
                faker.name().lastName(),
                commonEmail,
                faker.internet().password());

        // When
        when(userRepository.save(any()))
                .thenReturn(user);

        // Then
        underTest.register(registerRequest);

        verify(userRepository, times(1)).existsByEmail(registerRequest.email());
        verify(userRepository, times(1)).save(any());
        verify(tokenRepository, times(1)).save(any());
        verify(emailService, times(1)).sendEmail(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("It should not register a user if email is taken")
    void itWillThrowWhenRegisteringWithExistingEmail() throws MessagingException {
        // Given
        var registerRequest = new RegistrationRequest(
                faker.name().firstName(),
                faker.name().lastName(),
                commonEmail,
                faker.internet().password());

        // When
        when(userRepository.existsByEmail(registerRequest.email()))
                .thenReturn(true);

        // Then
        assertThatThrownBy(() -> underTest.register(registerRequest))
                .isInstanceOf(EmailAlreadyTakenException.class)
                .hasMessage(String.format("Email %s is already taken", registerRequest.email()));

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("It should try to authenticate")
    void canAuthenticate() {
        // Given
        var authRequest = new AuthenticationRequest(user.getEmail(), user.getPassword());

        var mockAuthentication = mock(Authentication.class);

        // When
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())))
                .thenReturn(mockAuthentication);

        when(mockAuthentication.getPrincipal())
                .thenReturn(user);

        when(jwtService.generateToken(any(), any()))
                .thenReturn("super-secret-jwt-token");

        // Then
        var result = underTest.authenticate(authRequest);

        assertThat(result.getToken()).isEqualTo("super-secret-jwt-token");
    }

    @Test
    @DisplayName("It should try to activate an account")
    void canActivateAccount() throws MessagingException {
        // Given
        var token = Token.builder()
                .token("my-third-super-secret-jwt-token")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        // When
        when(tokenRepository.findByToken(token.getToken()))
                .thenReturn(Optional.of(token));

        when(userRepository.findById(token.getUser().getId()))
                .thenReturn(Optional.of(user));

        // Then
        underTest.activateAccount(token.getToken());

        verify(userRepository, times(1)).save(user);
        verify(tokenRepository, times(1)).save(token);
    }

    @Test
    @DisplayName("It will throw when trying to activate an account with token that is not found")
    void itWillThrowWhenTokenNotFoundWhenActivateAccount() throws MessagingException {
        // Given
        var token = Token.builder()
                .token("my-third-super-secret-jwt-token")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        // When
        when(tokenRepository.findByToken(token.getToken()))
                .thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> underTest.activateAccount(token.getToken()))
                .isInstanceOf(TokenNotFoundException.class)
                .hasMessage(String.format("Token %s was not found", token.getToken()));

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("It will fail when the token is expired while activating account")
    void itWillFailWhenTokenIsExpiredWhenActivateAccount() throws MessagingException {
        // Given
        var token = Token.builder()
                .token("my-third-super-secret-jwt-token")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().minusSeconds(1))
                .user(user)
                .build();

        // When
        when(tokenRepository.findByToken(token.getToken()))
                .thenReturn(Optional.of(token));

        // Then
        var result = underTest.activateAccount(token.getToken());

        assertThat(result).isFalse();
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("It will throw when user is not found while activating account")
    void itWillThrowWhenUserIsNotFoundWhenActivateAccount() throws MessagingException {
        // Given
        var token = Token.builder()
                .token("my-third-super-secret-jwt-token")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        // When
        when(tokenRepository.findByToken(token.getToken()))
                .thenReturn(Optional.of(token));

        when(userRepository.findById(token.getUser().getId()))
                .thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> underTest.activateAccount(token.getToken()))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("It will throw when user is already enabled while activating account")
    void itWillThrowWhenUserIsAlreadyEnabledWhenActivateAccount() throws MessagingException {
        // Given
        var token = Token.builder()
                .token("my-third-super-secret-jwt-token")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        // Set user to enabled
        user.setEnabled(true);

        // When
        when(tokenRepository.findByToken(token.getToken()))
                .thenReturn(Optional.of(token));

        when(userRepository.findById(token.getUser().getId()))
                .thenReturn(Optional.of(user));

        // Then
        assertThatThrownBy(() -> underTest.activateAccount(token.getToken()))
                .isInstanceOf(UserAlreadyEnabledException.class)
                .hasMessage("User is already enabled!");

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("It should send a token")
    void canSendNewToken() throws MessagingException {
        // Given
        var token = Token.builder()
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        // When
        when(tokenRepository.findByToken(token.getToken()))
                .thenReturn(Optional.of(token));

        // Then
        underTest.sendNewToken(token.getToken());

        verify(tokenRepository, times(1)).save(any());
        verify(emailService, times(1)).sendEmail(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("It will throw when token not found while trying to send a new token")
    void itWillThrowWhenTokenNotFoundSendNewToken() throws MessagingException {
        // Given
        var token = Token.builder()
                .token("jwt-do-not-throw")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        // When
        when(tokenRepository.findByToken(token.getToken()))
                .thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> underTest.sendNewToken(token.getToken()))
                .isInstanceOf(TokenNotFoundException.class)
                        .hasMessage(String.format("Token %s was not found", token.getToken()));

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any(), any(), any(), any());
    }
}