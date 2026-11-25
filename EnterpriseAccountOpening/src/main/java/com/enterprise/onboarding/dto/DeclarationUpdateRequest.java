package com.enterprise.onboarding.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class DeclarationUpdateRequest {

    @NotNull(message = "Action type is required (SAVE_DRAFT, SAVE_AND_EXIT, SAVE_AND_CONTINUE)")
    private SaveActionType action;

    @NotNull(message = "Selections map cannot be null")
    private Map<String, Boolean> selections;

    public SaveActionType getAction() { return action; }
    public void setAction(SaveActionType action) { this.action = action; }
    public Map<String, Boolean> getSelections() { return selections; }
    public void setSelections(Map<String, Boolean> selections) { this.selections = selections; }
}
