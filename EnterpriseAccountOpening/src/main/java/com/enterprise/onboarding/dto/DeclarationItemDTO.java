package com.enterprise.onboarding.dto;

public class DeclarationItemDTO {
    private String code;
    private String label;
    private boolean mandatory;
    private boolean checked;

    public DeclarationItemDTO(String code, String label, boolean mandatory, boolean checked) {
        this.code = code;
        this.label = label;
        this.mandatory = mandatory;
        this.checked = checked;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public boolean isMandatory() { return mandatory; }
    public boolean isChecked() { return checked; }
}
