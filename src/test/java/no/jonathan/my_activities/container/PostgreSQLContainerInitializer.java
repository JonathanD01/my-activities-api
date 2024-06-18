package no.jonathan.my_activities.container;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

// SOURCE https://stackoverflow.com/a/68890310
public class PostgreSQLContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> sqlContainer;

    static {
        sqlContainer = new PostgreSQLContainer<>("postgres:latest");
        sqlContainer.start();
    }

    public void initialize (ConfigurableApplicationContext configurableApplicationContext){
        TestPropertyValues.of(
                "spring.datasource.url=" + sqlContainer.getJdbcUrl(),
                "spring.datasource.username=" + sqlContainer.getUsername(),
                "spring.datasource.password=" + sqlContainer.getPassword()
        ).applyTo(configurableApplicationContext.getEnvironment());
    }

}