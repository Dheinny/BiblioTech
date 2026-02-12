package com.fenix.bibliotech.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class DataTimeConfig {
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
