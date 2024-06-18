package no.jonathan.my_activities.activity;

import net.datafaker.Faker;
import no.jonathan.my_activities.container.PostgreSQLContainerInitializer;
import no.jonathan.my_activities.response.ResponseType;
import no.jonathan.my_activities.security.JwtService;
import no.jonathan.my_activities.utils.LocalDateFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.spring.sql.QuickPerfSqlConfig;
import org.quickperf.sql.annotation.ExpectDelete;
import org.quickperf.sql.annotation.ExpectInsert;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.annotation.ExpectUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Import(QuickPerfSqlConfig.class)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = {PostgreSQLContainerInitializer.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@QuickPerfTest
class ActivityIntegrationTest {

    private final Faker faker = new Faker();

    private final String userWithUserRole = "john.doe@example.com";

    private final String userWithNoneRole = "guest.user@example.com"; // User with role NONE

    private final String userWithAdminRole = "admin.user@example.com"; // User with role ADMIN

    private final String invalidJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODk" +
            "wIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    private WebTestClient webTestClient;

    @LocalServerPort
    int port;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    @DisplayName("It should get all activities for a user")
    @ExpectSelect(2)
    void itShouldGetActivities() {
        // Given
        var jwtToken = jwtService.generateTokenFromUsernameOnly(userWithUserRole);

        // When
        // Then
        webTestClient.get()
                .uri("/api/v1/activities")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.response").isEqualTo(ResponseType.SUCCESS.name())
                .jsonPath("$.result.page").isNotEmpty()
                .jsonPath("$.result.links").isNotEmpty()
                .jsonPath("$.result.links").isArray()
                .jsonPath("$.result.content").isNotEmpty()
                .jsonPath("$.result.content").isArray();
    }

    @Test
    @DisplayName("It should get all activities for a user with role ADMIN")
    @ExpectSelect(2)
    void itShouldGetActivitiesForAdmin() {
        // Given
        var jwtToken = jwtService.generateTokenFromUsernameOnly(userWithAdminRole);

        // When
        // Then
        webTestClient.get()
                .uri("/api/v1/activities")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.response").isEqualTo(ResponseType.SUCCESS.name())
                .jsonPath("$.result.page").isNotEmpty()
                .jsonPath("$.result.links").isNotEmpty()
                .jsonPath("$.result.links").isArray()
                .jsonPath("$.result.content").isNotEmpty()
                .jsonPath("$.result.content").isArray();
    }

    @Test
    @DisplayName("It should not get all activities for a user with no USER/ADMIN role")
    @ExpectSelect
    void itShouldNotGetActivitiesIfNoUserRole() {
        // Given
        var jwtToken = jwtService.generateTokenFromUsernameOnly(userWithNoneRole);

        // When
        // Then
        webTestClient.get()
                .uri("/api/v1/activities")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .isEmpty();
    }

    @Test
    @DisplayName("It should not get all activities for a user if jwtToken is invalid")
    void itShouldNotGetActivitiesIfTokenIsInvalid() {
        // Given
        // When
        // Then
        webTestClient.get()
                .uri("/api/v1/activities")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Bearer %s", invalidJwtToken))
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .isEmpty();
    }

    @Test
    @DisplayName("It should create an activity for a user")
    @ExpectSelect
    @ExpectInsert
    void itShouldCreateActivity() {
        // Given
        var jwtToken = jwtService.generateTokenFromUsernameOnly(userWithUserRole);

        var createRequest = new ActivityCreateRequest(
                LocalDate.now().plusYears(faker.random().nextInt(5, 10)),
                faker.lorem().sentence(),
                faker.lorem().characters(50, 100));

        // When
        // Then
        webTestClient.post()
                .uri("/api/v1/activities")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(createRequest), ActivityCreateRequest.class)
                .header("Authorization", String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.response").isEqualTo(ResponseType.SUCCESS.name())
                .jsonPath("$.result.id").isNotEmpty()
                .jsonPath("$.result.date").isEqualTo(LocalDateFormatter.format(createRequest.date()))
                .jsonPath("$.result.title").isEqualTo(createRequest.title())
                .jsonPath("$.result.description").isEqualTo(createRequest.description());
    }

    @Test
    @DisplayName("It should create an activity for a user with role ADMIN")
    @ExpectSelect
    @ExpectInsert
    void itShouldCreateActivityForAdmin() {
        // Given
        var jwtToken = jwtService.generateTokenFromUsernameOnly(userWithAdminRole);

        var createRequest = new ActivityCreateRequest(
                LocalDate.now().plusYears(faker.random().nextInt(5, 10)),
                faker.lorem().sentence(),
                faker.lorem().characters(50, 100));

        // When
        // Then
        webTestClient.post()
                .uri("/api/v1/activities")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(createRequest), ActivityCreateRequest.class)
                .header("Authorization", String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.response").isEqualTo(ResponseType.SUCCESS.name())
                .jsonPath("$.result.id").isNotEmpty()
                .jsonPath("$.result.date").isEqualTo(LocalDateFormatter.format(createRequest.date()))
                .jsonPath("$.result.title").isEqualTo(createRequest.title())
                .jsonPath("$.result.description").isEqualTo(createRequest.description());
    }

    @Test
    @DisplayName("It should not create an activity for user with no USER/ADMIN role")
    @ExpectSelect
    void itShouldNotCreateActivityIfNoUserRole() {
        // Given
        var jwtToken = jwtService.generateTokenFromUsernameOnly(userWithNoneRole);

        var createRequest = new ActivityCreateRequest(
                LocalDate.now().plusYears(faker.random().nextInt(5, 10)),
                faker.lorem().sentence(),
                faker.lorem().characters(50, 100));

        // When
        // Then
        webTestClient.post()
                .uri("/api/v1/activities")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(createRequest), ActivityCreateRequest.class)
                .header("Authorization", String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .isEmpty();
    }

    @Test
    @DisplayName("It should not create an activity for a user if jwtToken is invalid")
    void itShouldNotCreateActivityIfTokenIsInvalid() {
        // Given
        var createRequest = new ActivityCreateRequest(
                LocalDate.now().plusYears(faker.random().nextInt(5, 10)),
                faker.lorem().sentence(),
                faker.lorem().characters(50, 100));

        // When
        // Then
        webTestClient.post()
                .uri("/api/v1/activities")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(createRequest), ActivityCreateRequest.class)
                .header("Authorization", String.format("Bearer %s", invalidJwtToken))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .isEmpty();
    }

    @Test
    @DisplayName("It should update activity for user")
    @ExpectSelect(2)
    @ExpectUpdate
    void itShouldUpdateActivity() {
        // Given
        var jwtToken = jwtService.generateTokenFromUsernameOnly(userWithUserRole);

        var updateRequest = new ActivityUpdateRequest(
                400L,
                LocalDate.now().plusYears(faker.random().nextInt(5, 10)),
                faker.lorem().sentence(),
                faker.lorem().characters(100, 200));

        // When
        // Then
        webTestClient.patch()
                .uri("/api/v1/activities")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), ActivityUpdateRequest.class)
                .header("Authorization", String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.response").isEqualTo(ResponseType.SUCCESS.name())
                .jsonPath("$.result.id").isEqualTo(updateRequest.id())
                .jsonPath("$.result.date").isEqualTo(LocalDateFormatter.format(updateRequest.date()))
                .jsonPath("$.result.title").isEqualTo(updateRequest.title())
                .jsonPath("$.result.description").isEqualTo(updateRequest.description());
    }

    @Test
    @DisplayName("It should update activity for user with role ADMIN")
    @ExpectSelect(2)
    @ExpectUpdate
    void itShouldUpdateActivityForAdmin() {
        // Given
        var jwtToken = jwtService.generateTokenFromUsernameOnly(userWithAdminRole);

        var updateRequest = new ActivityUpdateRequest(
                800L,
                LocalDate.now().plusYears(faker.random().nextInt(5, 10)),
                faker.lorem().sentence(),
                faker.lorem().characters(100, 200));

        // When
        // Then
        webTestClient.patch()
                .uri("/api/v1/activities")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), ActivityUpdateRequest.class)
                .header("Authorization", String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.response").isEqualTo(ResponseType.SUCCESS.name())
                .jsonPath("$.result.id").isEqualTo(updateRequest.id())
                .jsonPath("$.result.date").isEqualTo(LocalDateFormatter.format(updateRequest.date()))
                .jsonPath("$.result.title").isEqualTo(updateRequest.title())
                .jsonPath("$.result.description").isEqualTo(updateRequest.description());
    }

    @Test
    @DisplayName("It should not update activity for user with role USER if the activity belongs to a different user")
    @ExpectSelect(2)
    void itShouldNotUpdateActivityForUserWhenActivityOwnerIsDifferent() {
        // Given
        var jwtToken = jwtService.generateTokenFromUsernameOnly(userWithUserRole);

        var updateRequest = new ActivityUpdateRequest(
                800L,
                LocalDate.now().plusYears(faker.random().nextInt(5, 10)),
                faker.lorem().sentence(),
                faker.lorem().characters(100, 200));

        // When
        // Then
        webTestClient.patch()
                .uri("/api/v1/activities")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), ActivityUpdateRequest.class)
                .header("Authorization", String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.response").isEqualTo(ResponseType.FAILED.name())
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors").isNotEmpty()
                .jsonPath("$.errors[0].message").isEqualTo("No permission");
    }

    @Test
    @DisplayName("It should update activity for user with role ADMIN if the activity belongs to a different user")
    @ExpectSelect(2)
    @ExpectUpdate
    void itShouldUpdateActivityForAdminWhenActivityOwnerIsDifferent() {
        // Given
        var jwtToken = jwtService.generateTokenFromUsernameOnly(userWithAdminRole);

        var updateRequest = new ActivityUpdateRequest(
                300L,
                LocalDate.now().plusYears(faker.random().nextInt(5, 10)),
                faker.lorem().sentence(),
                faker.lorem().characters(100, 200));

        // When
        // Then
        webTestClient.patch()
                .uri("/api/v1/activities")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), ActivityUpdateRequest.class)
                .header("Authorization", String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.response").isEqualTo(ResponseType.SUCCESS.name())
                .jsonPath("$.result.id").isEqualTo(updateRequest.id())
                .jsonPath("$.result.date").isEqualTo(LocalDateFormatter.format(updateRequest.date()))
                .jsonPath("$.result.title").isEqualTo(updateRequest.title())
                .jsonPath("$.result.description").isEqualTo(updateRequest.description());
    }

    @Test
    @DisplayName("It should update activity for user with role USER/ADMIN role")
    @ExpectSelect
    void itShouldNotUpdateActivityIfNoUserRole() {
        // Given
        var jwtToken = jwtService.generateTokenFromUsernameOnly(userWithNoneRole);

        var updateRequest = new ActivityUpdateRequest(
                500L,
                LocalDate.now().plusYears(faker.random().nextInt(5, 10)),
                faker.lorem().sentence(),
                faker.lorem().characters(100, 200));

        // When
        // Then
        webTestClient.patch()
                .uri("/api/v1/activities")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), ActivityUpdateRequest.class)
                .header("Authorization", String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .isEmpty();
    }

    @Test
    @DisplayName("It should not update activity if jwtToken is invalid")
    void itShouldNotUpdateActivityIfTokenIsInvalid() {
        // Given
        var createRequest = new ActivityUpdateRequest(
                200L,
                LocalDate.now().plusYears(faker.random().nextInt(5, 10)),
                faker.lorem().sentence(),
                faker.lorem().characters(100, 200));

        // When
        // Then
        webTestClient.patch()
                .uri("/api/v1/activities")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(createRequest), ActivityUpdateRequest.class)
                .header("Authorization", String.format("Bearer %s", invalidJwtToken))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .isEmpty();
    }

    @Test
    @DisplayName("It should delete an activity for user")
    @ExpectSelect(2)
    @ExpectDelete
    void itShouldDeleteActivity() {
        // Given
        var jwtToken = jwtService.generateTokenFromUsernameOnly(userWithUserRole);
        var activityIdToDelete = 100L;

        // When
        // Then
        webTestClient.delete()
                .uri("/api/v1/activities?id={id}", activityIdToDelete)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.response").isEqualTo(ResponseType.SUCCESS.name())
                .jsonPath("$.result").isEqualTo(activityIdToDelete);
    }

    @Test
    @DisplayName("It should delete an activity for user")
    @ExpectSelect(2)
    @ExpectDelete
    void itShouldDeleteActivityForAdmin() {
        // Given
        var jwtToken = jwtService.generateTokenFromUsernameOnly(userWithAdminRole);
        var activityIdToDelete = 700L;

        // When
        // Then
        webTestClient.delete()
                .uri("/api/v1/activities?id={id}", activityIdToDelete)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.response").isEqualTo(ResponseType.SUCCESS.name())
                .jsonPath("$.result").isEqualTo(activityIdToDelete);
    }

    @Test
    @DisplayName("It should not delete activity for user with role USER if the activity belongs to a different user")
    @ExpectSelect(2)
    void itShouldNotDeleteActivityForUserWhenActivityOwnerIsDifferent() {
        // Given
        var jwtToken = jwtService.generateTokenFromUsernameOnly(userWithUserRole);
        var activityIdToDelete = 800L;

        // When
        // Then
        webTestClient.delete()
                .uri("/api/v1/activities?id={id}", activityIdToDelete)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.response").isEqualTo(ResponseType.FAILED.name())
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors").isNotEmpty()
                .jsonPath("$.errors[0].message").isEqualTo("No permission");
    }

    @Test
    @DisplayName("It should delete an activity for user with ADMIN role even if the activity belongs to a different user")
    @ExpectSelect(2)
    @ExpectDelete
    void itShouldDeleteActivityForAdminWhenActivityOwnerIsDifferent() {
        // Given
        var jwtToken = jwtService.generateTokenFromUsernameOnly(userWithAdminRole);
        var activityIdToDelete = 200L;

        // When
        // Then
        webTestClient.delete()
                .uri("/api/v1/activities?id={id}", activityIdToDelete)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.response").isEqualTo(ResponseType.SUCCESS.name())
                .jsonPath("$.result").isEqualTo(activityIdToDelete);
    }

    @Test
    @DisplayName("It should not delete an activity for user if the token is invalid")
    void itShouldNotDeleteActivityIfTokenIsInvalid() {
        // Given
        var activityIdToDelete = 200L;

        // When
        // Then
        webTestClient.delete()
                .uri("/api/v1/activities?id={id}", activityIdToDelete)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Bearer %s", invalidJwtToken))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .isEmpty();
    }
}