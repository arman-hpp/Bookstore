package com.tosan.bookstore.configs;

import com.tosan.bookstore.services.AuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class AuditingConfiguration {
    private final AuthenticationService _authenticationService;

    public AuditingConfiguration(AuthenticationService authenticationService) {
        _authenticationService = authenticationService;
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return _authenticationService::loadCurrentUsername;
    }
}
