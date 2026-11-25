package com.enterprise.onboarding.model;

import jakarta.persistence.*;

@Entity
@Table(name = "admin_declaration_rules")
public class DeclarationRuleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // e.g., "DECL_TAX_RESIDENCY", "DECL_BENEFICIAL_OWNER"

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private boolean mandatory;

    @Column(nullable = false)
    private boolean active;

    // Document type triggered for Step 8 if this declaration is true
    private String triggeredDocumentType; // e.g., "TAX_SELF_CERT_FORM"

    public DeclarationRuleConfig() {}

    public DeclarationRuleConfig(String code, String label, boolean mandatory, boolean active,
                                 String triggeredDocumentType) {
        this.code = code;
        this.label = label;
        this.mandatory = mandatory;
        this.active = active;
        this.triggeredDocumentType = triggeredDocumentType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public boolean isMandatory() { return mandatory; }
    public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getTriggeredDocumentType() { return triggeredDocumentType; }
    public void setTriggeredDocumentType(String triggeredDocumentType) { this.triggeredDocumentType = triggeredDocumentType; }
}
