package com.mortartec.appreader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mortartec.appreader.client.PostcodeLookupResult;
import com.mortartec.appreader.client.PublicPostcodeClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.storage.file=./data/test-applications.json")
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PublicPostcodeClient publicPostcodeClient;

    @Test
    void submitApplication_thenReadItBack_viaPostThenGet() throws Exception {
        PostcodeLookupResult mockResult = new PostcodeLookupResult();
        mockResult.setPostcode("SW1A 1AA");
        mockResult.setRegion("London");
        mockResult.setAdminDistrict("Westminster");
        mockResult.setCountry("England");
        mockResult.setLatitude(51.501);
        mockResult.setLongitude(-0.141);
        when(publicPostcodeClient.lookup(anyString())).thenReturn(Optional.of(mockResult));

        String requestBody = """
                {
                  "applicantName": "Jordan Smith",
                  "email": "jordan.smith@example.com",
                  "propertyPostcode": "SW1A 1AA",
                  "loanAmount": 240000,
                  "propertyValue": 300000
                }
                """;

        String responseJson = mockMvc.perform(post("/api/applications")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.region").value("London"))
                .andExpect(jsonPath("$.loanToValuePercent").value(80.0))
                .andReturn().getResponse().getContentAsString();

        JsonNode responseNode = new ObjectMapper().readTree(responseJson);
        String id = responseNode.get("id").asText();

        mockMvc.perform(get("/api/applications/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicantName").value("Jordan Smith"));

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void submitApplication_withInvalidPostcode_returns400() throws Exception {
        String requestBody = """
                {
                  "applicantName": "Jordan Smith",
                  "email": "jordan.smith@example.com",
                  "propertyPostcode": "NOT-A-POSTCODE",
                  "loanAmount": 240000,
                  "propertyValue": 300000
                }
                """;

        mockMvc.perform(post("/api/applications")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.propertyPostcode").exists());
    }
}
