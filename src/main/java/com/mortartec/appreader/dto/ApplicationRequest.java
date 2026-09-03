package com.mortartec.appreader.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * Request body for POST /api/applications.
 * Kept intentionally small — a "basic" application reader — but with real
 * validation, since this stands in for a mortgage case intake form.
 */
public class ApplicationRequest {

    @NotBlank(message = "applicantName is required")
    private String applicantName;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    private String email;

    @NotBlank(message = "propertyPostcode is required")
    @Pattern(
            regexp = "^[A-Za-z]{1,2}[0-9][A-Za-z0-9]?\\s?[0-9][A-Za-z]{2}$",
            message = "propertyPostcode must be a valid UK postcode, e.g. 'SW1A 1AA'"
    )
    private String propertyPostcode;

    @NotNull(message = "loanAmount is required")
    @DecimalMin(value = "0.01", message = "loanAmount must be greater than 0")
    private BigDecimal loanAmount;

    @NotNull(message = "propertyValue is required")
    @DecimalMin(value = "0.01", message = "propertyValue must be greater than 0")
    private BigDecimal propertyValue;

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
}
