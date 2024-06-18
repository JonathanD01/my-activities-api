package no.jonathan.my_activities;


import no.jonathan.my_activities.config.ApplicationAuditAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

@Configuration
public class TestConfig {

    @Bean
    public AuditorAware<Long> auditorAware() {
        return new ApplicationAuditAware();
    }


}
