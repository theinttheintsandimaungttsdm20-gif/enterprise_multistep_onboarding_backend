package com.enterprise.onboarding.service;

import com.enterprise.onboarding.dto.*;
import com.enterprise.onboarding.exception.AdminConfigChangedException;
import com.enterprise.onboarding.exception.MandatoryDeclarationException;
import com.enterprise.onboarding.exception.ResourceNotFoundException;
import com.enterprise.onboarding.model.DeclarationRuleConfig;
import com.enterprise.onboarding.model.OnboardingApplication;
import com.enterprise.onboarding.repository.DeclarationRuleRepository;
import com.enterprise.onboarding.repository.OnboardingApplicationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeclarationService {

    private final DeclarationRuleRepository ruleRepository;
    private final OnboardingApplicationRepository applicationRepository;

    /**
     * Merges live Admin rules with saved draft answers and detects if Admin rules changed
     * since the draft was last saved.
     */
    @Transactional(readOnly = true)
    public DeclarationPageResponse getDeclarationConfig(String applicationId) {
        OnboardingApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        List<DeclarationRuleConfig> activeRules = ruleRepository.findByActiveTrueOrderByIdAsc();
        Map<String, Boolean> savedAnswers = app.getDeclarationAnswers();

        // Detect drift directly by comparing saved draft keys against active Admin rules
        boolean driftDetected = hasAdminRuleChangedSinceDraft(activeRules, savedAnswers);

        List<DeclarationItemDTO> mergedItems = activeRules.stream()
                .map(rule -> new DeclarationItemDTO(
                        rule.getCode(),
                        rule.getLabel(),
                        rule.isMandatory(),
                        savedAnswers.getOrDefault(rule.getCode(), false)
                ))
                .collect(Collectors.toList());

        List<String> carriedDocs = computeRequiredDocuments(activeRules, savedAnswers);

        return new DeclarationPageResponse(
                app.getApplicationId(),
                app.getStatus(),
                driftDetected,
                mergedItems,
                carriedDocs
        );
    }

    /**
     * Unified Update API handling SAVE_DRAFT, SAVE_AND_EXIT, and SAVE_AND_CONTINUE.
     */
    @Transactional
    public DeclarationPageResponse updateDeclarations(String applicationId, DeclarationUpdateRequest request) {
        OnboardingApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        List<DeclarationRuleConfig> activeRules = ruleRepository.findByActiveTrueOrderByIdAsc();
        Set<String> activeCodes = activeRules.stream()
                .map(DeclarationRuleConfig::getCode)
                .collect(Collectors.toSet());

        // 1. Detect if client payload is out of sync with live Admin rules (e.g., admin added/removed a rule)
        if (!request.getSelections().keySet().equals(activeCodes)) {
            throw new AdminConfigChangedException(
                    "Declaration criteria were updated by administrator. Please review the latest items."
            );
        }

        // 2. Sanitize selections against active Admin rules
        Map<String, Boolean> sanitizedAnswers = new HashMap<>();
        for (DeclarationRuleConfig rule : activeRules) {
            sanitizedAnswers.put(rule.getCode(), request.getSelections().getOrDefault(rule.getCode(), false));
        }

        // 3. Conditional Validation: Enforce mandatory flags ONLY when advancing to Step 8
        if (request.getAction() == SaveActionType.SAVE_AND_CONTINUE) {
            List<String> missingMandatory = activeRules.stream()
                    .filter(DeclarationRuleConfig::isMandatory)
                    .filter(rule -> !sanitizedAnswers.getOrDefault(rule.getCode(), false))
                    .map(DeclarationRuleConfig::getCode)
                    .collect(Collectors.toList());

            if (!missingMandatory.isEmpty()) {
                throw new MandatoryDeclarationException(
                        "Mandatory declarations must be accepted before proceeding: " + missingMandatory
                );
            }
            app.setStatus("DECLARATIONS_COMPLETED");
        } else if (request.getAction() == SaveActionType.SAVE_AND_EXIT) {
            app.setStatus("DRAFT_EXITED");
        } else {
            app.setStatus("DRAFT");
        }

        // 4. Persist updated selections
        app.setDeclarationAnswers(sanitizedAnswers);
        applicationRepository.save(app);

        // 5. Build carried-over payload for Step 8 (Upload Documents)
        List<DeclarationItemDTO> updatedItems = activeRules.stream()
                .map(rule -> new DeclarationItemDTO(
                        rule.getCode(),
                        rule.getLabel(),
                        rule.isMandatory(),
                        sanitizedAnswers.getOrDefault(rule.getCode(), false)
                ))
                .collect(Collectors.toList());

        List<String> carriedDocs = computeRequiredDocuments(activeRules, sanitizedAnswers);

        return new DeclarationPageResponse(
                app.getApplicationId(),
                app.getStatus(),
                false,
                updatedItems,
                carriedDocs
        );
    }

    /**
     * Checks if an existing draft has stale keys or newly mandated items unchecked.
     */
    private boolean hasAdminRuleChangedSinceDraft(List<DeclarationRuleConfig> activeRules,
                                                  Map<String, Boolean> savedAnswers) {
        if (savedAnswers == null || savedAnswers.isEmpty()) {
            return false; // Fresh application, no drift
        }

        Set<String> activeCodes = activeRules.stream()
                .map(DeclarationRuleConfig::getCode)
                .collect(Collectors.toSet());

        // Rule added or removed by admin since draft was saved
        if (!savedAnswers.keySet().equals(activeCodes)) {
            return true;
        }

        // Rule flipped from optional -> mandatory while user had saved false
        return activeRules.stream()
                .anyMatch(rule -> rule.isMandatory() && !Boolean.TRUE.equals(savedAnswers.get(rule.getCode())));
    }

    private List<String> computeRequiredDocuments(List<DeclarationRuleConfig> rules, Map<String, Boolean> answers) {
        List<String> docs = new ArrayList<>();
        docs.add("COMPANY_REGISTRATION_CERT");
        for (DeclarationRuleConfig rule : rules) {
            if (Boolean.TRUE.equals(answers.get(rule.getCode())) && rule.getTriggeredDocumentType() != null) {
                docs.add(rule.getTriggeredDocumentType());
            }
        }
        return docs;
    }
}