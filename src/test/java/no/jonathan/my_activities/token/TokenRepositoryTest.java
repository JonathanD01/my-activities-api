package no.jonathan.my_activities.token;

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
class TokenRepositoryTest {

    @Autowired
    private TokenRepository underTest;

    @Test
    @DisplayName("It should find a token")
    @ExpectSelect
    void itShouldFindTokenEntityByToken() {
        // Given
        var token = "my-super-super-secret-token";

        // When
        // Then
        var result = underTest.findByToken(token);

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(token);
    }

    @Test
    @DisplayName("It should not find a token")
    @ExpectSelect
    void itShouldNotFindTokenEntityByToken() {
        // Given
        var token = "my-non-existing-secret-token";

        // When
        // Then
        var result = underTest.findByToken(token);

        assertThat(result).isEmpty();
    }
}