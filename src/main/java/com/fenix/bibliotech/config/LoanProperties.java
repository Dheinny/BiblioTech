package com.fenix.bibliotech.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix= "app.loan")
@Component
@Getter
@Setter
public class LoanProperties {
    private int defaultTermDays;
}
