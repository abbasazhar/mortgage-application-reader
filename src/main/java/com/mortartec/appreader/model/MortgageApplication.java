package com.mortartec.appreader.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * A single mortgage case as tracked by the application reader.
 *
 * Fields fall into three groups:
 *  - submitted by the caller (applicant + case details),
 *  - looked up from publicly available data (postcode enrichment),
 *  - derived by the service itself (loan-to-value, timestamps, id).
 */
public class MortgageApplication {

    private String id;

    // --- submitted by the caller ---
    private String applicantName;
    private String email;
    private String propertyPostcode;
    private BigDecimal loanAmount;
    private BigDecimal propertyValue;

    // --- enriched from publicly available data (postcodes.io) ---
    private String region;
    private String adminDistrict;
    private String country;
    private Double latitude;
    private Double longitude;

    // --- derived ---
    private BigDecimal loanToValuePercent;
    private String caseStatus;
    private Instant submittedAt;

    public MortgageApplication() {
    }

    public static MortgageApplication newCase(String applicantName, String email,
                                               String propertyPostcode,
                                               BigDecimal loanAmount,
                                               BigDecimal propertyValue) {
        MortgageApplication app = new MortgageApplication();
        app.id = UUID.randomUUID().toString();
        app.applicantName = applicantName;
        app.email = email;
        app.propertyPostcode = propertyPostcode;
        app.loanAmount = loanAmount;
        app.propertyValue = propertyValue;
        app.submittedAt = Instant.now();
        app.caseStatus = "RECEIVED";
        if (propertyValue != null && propertyValue.compareTo(BigDecimal.ZERO) > 0 && loanAmount != null) {
            app.loanToValuePercent = loanAmount
                    .divide(propertyValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return app;
    }

    public void applyPublicEnrichment(String region, String adminDistrict, String country,
                                       Double latitude, Double longitude) {
        this.region = region;
        this.adminDistrict = adminDistrict;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.caseStatus = "ENRICHED";
    }

    public void markEnrichmentUnavailable() {
        this.caseStatus = "RECEIVED_NO_ENRICHMENT";
    }

    // --- getters / setters (needed for Jackson (de)serialization) ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPropertyPostcode() {
        return propertyPostcode;
    }

    public void setPropertyPostcode(String propertyPostcode) {
        this.propertyPostcode = propertyPostcode;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public BigDecimal getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(BigDecimal propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAdminDistrict() {
        return adminDistrict;
    }

    public void setAdminDistrict(String adminDistrict) {
        this.adminDistrict = adminDistrict;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLoanToValuePercent() {
        return loanToValuePercent;
    }

    public void setLoanToValuePercent(BigDecimal loanToValuePercent) {
        this.loanToValuePercent = loanToValuePercent;
    }

    public String getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(String caseStatus) {
        this.caseStatus = caseStatus;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }
}
