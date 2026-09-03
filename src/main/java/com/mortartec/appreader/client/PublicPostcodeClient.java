package com.mortartec.appreader.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

/**
 * Reads publicly available UK geography data for a postcode from postcodes.io
 * (https://postcodes.io) — a free, open, no-API-key public API backed by
 * Ordnance Survey / ONS open data. Used here to enrich a submitted mortgage
 * case with the region/local authority the property sits in, the kind of
 * lookup a mortgage brokerage does routinely when placing a case with a
 * lender.
 */
@Component
public class PublicPostcodeClient {

    private static final Logger log = LoggerFactory.getLogger(PublicPostcodeClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PublicPostcodeClient(RestTemplate restTemplate,
                                 @Value("${app.public-data.postcode-api-base-url:https://api.postcodes.io}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Looks up a UK postcode against the public postcodes.io API.
     *
     * @return the lookup result, or empty if the postcode wasn't found or the
     *         public API was unreachable (this enrichment step is best-effort
     *         and must never block saving the underlying application).
     */
    public Optional<PostcodeLookupResult> lookup(String postcode) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/postcodes/{postcode}")
                .buildAndExpand(postcode)
                .toUri();
        try {
            PostcodeLookupResponse response = restTemplate.getForObject(uri, PostcodeLookupResponse.class);
            if (response != null && response.getStatus() == 200 && response.getResult() != null) {
                return Optional.of(response.getResult());
            }
            return Optional.empty();
        } catch (RestClientException ex) {
            log.warn("Public postcode lookup failed for '{}': {}", postcode, ex.getMessage());
            return Optional.empty();
        }
    }
}
