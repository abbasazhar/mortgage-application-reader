package com.mortartec.appreader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Basic Application Reader (Spring Boot).
 *
 * Small backend service, modeled on Mortartec's mortgage/protection case
 * workflow, that:
 *  1. Accepts a mortgage case submission over POST,
 *  2. Enriches it with publicly available UK postcode data (postcodes.io),
 *  3. Persists the enriched case to local storage (a JSON file on disk),
 *  4. Returns stored cases back over GET.
 */
@SpringBootApplication(proxyBeanMethods = false)
public class MortgageApplicationReaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MortgageApplicationReaderApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }
}
