package no.jonathan.my_activities.authentication;

import net.datafaker.Faker;
import no.jonathan.my_activities.container.MailHogContainerInitializer;
import no.jonathan.my_activities.container.PostgreSQLContainerInitializer;
import no.jonathan.my_activities.response.ResponseType;
import no.jonathan.my_activities.response.ResponseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.spring.sql.QuickPerfSqlConfig;
import org.quickperf.sql.annotation.ExpectInsert;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.annotation.ExpectUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@Import(QuickPerfSqlConfig.class)
@QuickPerfTest
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = {PostgreSQLContainerInitializer.class, MailHogContainerInitializer.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationIntegrationTest {

    private final Faker faker = new Faker();

    private WebTestClient webTestClient;

    private final RegistrationRequest registrationRequest = new RegistrationRequest(
            faker.name().firstName(),
            faker.name().lastName(),
            "static-email@spring.no",
            "static-password123");

    @LocalServerPort
    int port;

    @Autowired
    private AuthenticationService service;

    @Autowired
    private ResponseUtil responseUtil;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient
                .bindToController(new AuthenticationController(service, responseUtil))
                .configureClient()
                .baseUrl(String.format("http://localhost:%s/api/v1/auth", port))
                .build();
    }

    @Order(1)
    @Test
    @DisplayName("It should successfully register a user with a 202 accepted response")
    @ExpectInsert(2)
    @ExpectSelect
    void itShouldRegister() {
        // Given
        // When
        // Then
        webTestClient.post()
                .uri("/register")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(registrationRequest), RegistrationRequest.class)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .isEmpty();
    }

    @Order(2)
    @Test
    @DisplayName("It should activate the account if the token is valid")
    @ExpectSelect(2)
    @ExpectUpdate(2)
    void itShouldActivateAccount() {
        // Given
        var activateAccountRequest = new ActivateAccountRequest("my-super-super-secret-token");

        // When
        // Then
        webTestClient.post()
                .uri("/activate-account")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(activateAccountRequest), ActivateAccountRequest.class)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .isEmpty();
    }

    @Order(3)
    @Test
    @DisplayName("It should authenticate")
    @Sql(scripts={"classpath:sql/authentication-it-test-set-user-to-enabled.sql"})
    @ExpectSelect
    void itShouldAuthenticate() {
        // Given
        var authenticationRequest =
                new AuthenticationRequest(registrationRequest.email(), registrationRequest.password());

        // When
        // Then
        webTestClient.post()
                .uri("/authenticate")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(authenticationRequest), AuthenticationRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.response").isEqualTo(ResponseType.SUCCESS.name())
                .jsonPath("$.result.token").isNotEmpty();
    }

    @Order(4)
    @Test
    @DisplayName("It should not authenticate if credentials are wrong")
    void itShouldNotAuthenticate() {
        // Given
        var authenticationRequest =
                new AuthenticationRequest(faker.internet().emailAddress(), faker.internet().password());

        // When
        // Then
        webTestClient.post()
                .uri("/authenticate")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(authenticationRequest), AuthenticationRequest.class)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .isEmpty();
    }

    @Order(5)
    @Test
    @DisplayName("It should not activate the account if the token is bad")
    void itShouldNotActivateAccountIfBadToken() {
        // Given
        var activateAccountRequest = new ActivateAccountRequest("bad-or-wrong-token");

        // When
        // Then
        webTestClient.post()
                .uri("/activate-account")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(activateAccountRequest), ActivateAccountRequest.class)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .isEmpty();
    }
}