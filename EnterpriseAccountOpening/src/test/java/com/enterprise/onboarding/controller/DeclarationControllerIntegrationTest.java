package com.enterprise.onboarding.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DeclarationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Unauthenticated request should be rejected with 401 Unauthorized")
    void getDeclarations_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/onboarding/APP-2026-001/declarations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "applicant", roles = {"APPLICANT"})
    @DisplayName("Applicant cannot access Admin toggle endpoint (403 Forbidden)")
    void toggleAdminRule_asApplicant_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/declarations/DECL_TAX_RESIDENCY/toggle-mandatory"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    @WithMockUser(username = "applicant", roles = {"APPLICANT"})
    @DisplayName("SAVE_AND_CONTINUE with missing mandatory field returns 400 from RestControllerAdvice")
    void updateDeclarations_missingMandatory_returns400() throws Exception {
        String payload = """
                {
                  "action": "SAVE_AND_CONTINUE",
                  "selections": {
                    "DECL_TERMS_POLICY": true,
                    "DECL_TAX_RESIDENCY": false,
                    "DECL_BENEFICIAL_OWNER": false
                  }
                }
                """;

        mockMvc.perform(put("/api/v1/onboarding/APP-2026-001/declarations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MANDATORY_DECLARATION_MISSING"));
    }
}