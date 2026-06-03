package org.example.javaspringbootjooqsample.config.jooq;

import org.example.javaspringbootjooqsample.infrastructure.jooq.listener.JooqQueryCountListener;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.jooq.conf.Settings;
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("customJooqConfiguration")
public class JooqConfiguration {

    @Bean
    public DefaultConfigurationCustomizer defaultConfigurationCustomizer(JooqQueryCountListener listener) {
        return configuration -> {
            configuration.set(new DefaultExecuteListenerProvider(listener));
            configuration.set(new Settings().withRenderSchema(false));
        };
    }
}
