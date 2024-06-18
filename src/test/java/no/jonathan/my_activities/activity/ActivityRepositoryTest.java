package no.jonathan.my_activities.activity;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import({QuickPerfSqlConfig.class, TestConfig.class})
@ContextConfiguration(initializers = {PostgreSQLContainerInitializer.class})
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@QuickPerfTest
class ActivityRepositoryTest {

    @Autowired
    private ActivityRepository activityRepository;

    // TODO Check if ordered in ascending order
    @Test
    @DisplayName("It should find all activities by user and not include out-dated activities")
    @ExpectSelect
    void itShouldFindByUser_EmailAndDateGreaterThanEqualOrderByDateAsc() {
        // Given
        var userId = 100L;
        var email = "john.doe@example.com";
        var date = LocalDate.now();
        var page = PageRequest.of(0, 10);

        // Only 2 valid activities in import.sql
        int expected = 2;

        // When
        // Then
        var result = activityRepository.findByUser_EmailAndDateGreaterThanEqualOrderByDateAsc(email, date, page);

        assertThat(result.getTotalElements()).isEqualTo(expected);
        assertThat(result.getContent()
                .stream()
                .anyMatch(activity -> !activity.userId().equals(userId)))
                .isFalse();
    }

    @Test
    @DisplayName("It should not find any activities")
    @ExpectSelect
    void itNotShouldFindByUser_EmailAndDateGreaterThanEqualOrderByDateAsc() {
        // Given
        var email = "no-activities@example.com";
        var date = LocalDate.now();
        var page = PageRequest.of(0, 10);

        int expected = 0;

        // When
        // Then
        var result = activityRepository.findByUser_EmailAndDateGreaterThanEqualOrderByDateAsc(email, date, page);

        assertThat(result.getTotalElements()).isEqualTo(expected);
    }
}