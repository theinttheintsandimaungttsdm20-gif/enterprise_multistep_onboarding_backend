package com.enterprise.onboarding.model;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "onboarding_applications")
public class OnboardingApplication {

    @Id
    private String applicationId; // e.g., "APP-2026-001"[cite: 11]

    @Column(nullable = false)
    private String status; // "DRAFT", "DECLARATIONS_COMPLETED", "EXITED"[cite: 11]

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "application_declaration_answers",
                     joinColumns = @JoinColumn(name = "application_id"))
    @MapKeyColumn(name = "declaration_code")
    @Column(name = "accepted")
    private Map<String, Boolean> declarationAnswers = new HashMap<>(); //[cite: 11]

    public OnboardingApplication() {} //[cite: 11]

    public OnboardingApplication(String applicationId, String status) {
        this.applicationId = applicationId;
        this.status = status;
    }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Map<String, Boolean> getDeclarationAnswers() { return declarationAnswers; }
    public void setDeclarationAnswers(Map<String, Boolean> declarationAnswers) { this.declarationAnswers = declarationAnswers; }
}