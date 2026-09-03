package com.mortartec.appreader.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Top-level envelope returned by https://api.postcodes.io/postcodes/{postcode}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostcodeLookupResponse {

    private int status;
    private PostcodeLookupResult result;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public PostcodeLookupResult getResult() {
        return result;
    }

    public void setResult(PostcodeLookupResult result) {
        this.result = result;
    }
}
