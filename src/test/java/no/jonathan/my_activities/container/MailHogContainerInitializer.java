package no.jonathan.my_activities.container;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;

// SOURCE https://stackoverflow.com/a/68890310
public class MailHogContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final GenericContainer<?> emailContainer;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private static int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.from-email}")
    private String fromEmail;

    static {
        emailContainer = new GenericContainer<>("42bv/mailhog");
        //emailContainer.withExposedPorts(port);
        emailContainer.start();
    }

    public void initialize (ConfigurableApplicationContext configurableApplicationContext){
        TestPropertyValues.of(
                "spring.mail.host=" + host,
                "spring.mail.port=" + port,
                "spring.mail.username=" + username,
                "spring.mail.password=" + password,
                "spring.mail.from-email=" + fromEmail
        ).applyTo(configurableApplicationContext.getEnvironment());
    }

}