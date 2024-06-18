package no.jonathan.my_activities.user;

import no.jonathan.my_activities.TestConfig;
import no.jonathan.my_activities.container.PostgreSQLContainerInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.spring.sql.QuickPerfSqlConfig;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuickPerfSqlConfig.class, TestConfig.class})
@ContextConfiguration(initializers = {PostgreSQLContainerInitializer.class})
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@QuickPerfTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("It should find a user by email")
    @ExpectSelect
    void itShouldFindByEmail() {
        // Given
        var email = "john.doe@example.com";

        // When
        // Then
        var result = userRepository.findByEmail(email);

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("It should not find a user by email")
    @ExpectSelect
    void itShouldNotFindByEmail() {
        // Given
        var email = "no-user@example.com";

        // When
        // Then
        var result = userRepository.findByEmail(email);

        assertThat(result.isPresent()).isFalse();
    }

    @Test
    @DisplayName("It should return true for an existing user")
    @ExpectSelect
    void itShouldExistByEmail() {
        // Given
        var email = "john.doe@example.com";

        // When
        // Then
        var result = userRepository.existsByEmail(email);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("It should return false for a non-existing user")
    @ExpectSelect
    void itShouldNotExistByEmail() {
        // Given
        var email = "no-user@example.com";

        // When
        // Then
        var result = userRepository.existsByEmail(email);

        assertThat(result).isFalse();
    }
}