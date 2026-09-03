package com.mortartec.appreader.service;

import com.mortartec.appreader.client.PostcodeLookupResult;
import com.mortartec.appreader.client.PublicPostcodeClient;
import com.mortartec.appreader.dto.ApplicationRequest;
import com.mortartec.appreader.model.MortgageApplication;
import com.mortartec.appreader.repository.LocalStorageApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Core workflow of the application reader:
 *   POST -> enrich with public data -> save to local storage
 *   GET  -> read back from local storage
 */
@Service
public class ApplicationService {

    private final LocalStorageApplicationRepository repository;
    private final PublicPostcodeClient postcodeClient;

    public ApplicationService(LocalStorageApplicationRepository repository,
                               PublicPostcodeClient postcodeClient) {
        this.repository = repository;
        this.postcodeClient = postcodeClient;
    }

    public MortgageApplication submit(ApplicationRequest request) {
        MortgageApplication application = MortgageApplication.newCase(
                request.getApplicantName(),
                request.getEmail(),
                request.getPropertyPostcode(),
                request.getLoanAmount(),
                request.getPropertyValue()
        );

        // Use publicly available data (postcodes.io) to enrich the case with
        // the region/local authority the property sits in. Best-effort: if
        // the public API is unreachable, the case is still saved.
        Optional<PostcodeLookupResult> enrichment = postcodeClient.lookup(request.getPropertyPostcode());
        if (enrichment.isPresent()) {
            PostcodeLookupResult result = enrichment.get();
            application.applyPublicEnrichment(
                    result.getRegion(),
                    result.getAdminDistrict(),
                    result.getCountry(),
                    result.getLatitude(),
                    result.getLongitude()
            );
        } else {
            application.markEnrichmentUnavailable();
        }

        return repository.save(application);
    }

    public List<MortgageApplication> findAll() {
        return repository.findAll();
    }

    public Optional<MortgageApplication> findById(String id) {
        return repository.findById(id);
    }
}
