package com.mortartec.appreader.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Maps the "result" object returned by the free, public postcodes.io API:
 * https://api.postcodes.io/postcodes/{postcode}
 *
 * Only the fields this service actually uses are mapped; everything else in
 * the response is ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostcodeLookupResult {

    private String postcode;
    private String region;
    private String country;

    @com.fasterxml.jackson.annotation.JsonProperty("admin_district")
    private String adminDistrict;

    private Double latitude;
    private Double longitude;

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAdminDistrict() {
        return adminDistrict;
    }

    public void setAdminDistrict(String adminDistrict) {
        this.adminDistrict = adminDistrict;
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
}
