package no.jonathan.my_activities.authentication;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import no.jonathan.my_activities.email.EmailService;
import no.jonathan.my_activities.email.EmailTemplateName;
import no.jonathan.my_activities.exception.EmailAlreadyTakenException;
import no.jonathan.my_activities.exception.TokenNotFoundException;
import no.jonathan.my_activities.exception.UserAlreadyEnabledException;
import no.jonathan.my_activities.security.JwtService;
import no.jonathan.my_activities.token.Token;
import no.jonathan.my_activities.token.TokenRepository;
import no.jonathan.my_activities.user.User;
import no.jonathan.my_activities.user.UserRepository;
import no.jonathan.my_activities.user.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;

    @Value("${mailing.frontend.activation-url}")
    private String activationUrl;

    @Transactional
    public void register(RegistrationRequest request) throws MessagingException {
        var user = User.builder()
                .firstname(request.firstname())
                .lastname(request.lastname())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .accountLocked(false)
                .enabled(false)
                .userRole(UserRole.USER)
                .build();

        var userExists = userRepository.existsByEmail(request.email());

        if (userExists) {
            throw new EmailAlreadyTakenException(request.email());
        }

        userRepository.save(user);

        sendValidationEmail(user);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var claims = new HashMap<String, Object>();
        var user = ((User) auth.getPrincipal());
        claims.put("fullName", user.getFullName());

        var jwtToken = jwtService.generateToken(claims, (User) auth.getPrincipal());
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Transactional
    public boolean activateAccount(String token) {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException(token));

        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            return false;
        }

        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.isEnabled()) {
            throw new UserAlreadyEnabledException();
        }

        user.setEnabled(true);
        userRepository.save(user);

        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);

        return true;
    }

    @Transactional
    public void sendNewToken(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException(token));

        sendValidationEmail(savedToken.getUser());
    }

    private String generateAndSaveActivationToken(User user) {
        // Generate a token
        String generatedToken = generateActivationCode(4);
        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        tokenRepository.save(token);

        return generatedToken;
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);

        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Bruker aktivering"
        );
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();

        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }
}
