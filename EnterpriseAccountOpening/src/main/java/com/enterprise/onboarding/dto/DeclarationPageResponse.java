package com.enterprise.onboarding.dto;

import java.util.List;

public class DeclarationPageResponse {
    private String applicationId;
    private String status;
    private boolean configDriftDetected;
    private List<DeclarationItemDTO> declarations;
    // Carried-over data for Step 8 (Upload Documents)
    private List<String> requiredDocumentsForNextStep;

    public DeclarationPageResponse(String applicationId, String status,
                                   boolean configDriftDetected, List<DeclarationItemDTO> declarations,
                                   List<String> requiredDocumentsForNextStep) {
        this.applicationId = applicationId;
        this.status = status;
        this.configDriftDetected = configDriftDetected;
        this.declarations = declarations;
        this.requiredDocumentsForNextStep = requiredDocumentsForNextStep;
    }

    public String getApplicationId() { return applicationId; }
    public String getStatus() { return status; }
    public boolean isConfigDriftDetected() { return configDriftDetected; }
    public List<DeclarationItemDTO> getDeclarations() { return declarations; }
    public List<String> getRequiredDocumentsForNextStep() { return requiredDocumentsForNextStep; }
}