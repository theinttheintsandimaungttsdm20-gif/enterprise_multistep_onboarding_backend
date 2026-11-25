package com.enterprise.onboarding.service;

import com.enterprise.onboarding.dto.*;
import com.enterprise.onboarding.exception.AdminConfigChangedException;
import com.enterprise.onboarding.exception.MandatoryDeclarationException;
import com.enterprise.onboarding.model.DeclarationRuleConfig;
import com.enterprise.onboarding.model.OnboardingApplication;
import com.enterprise.onboarding.repository.DeclarationRuleRepository;
import com.enterprise.onboarding.repository.OnboardingApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeclarationServiceTest {

    @Mock
    private DeclarationRuleRepository ruleRepository;

    @Mock
    private OnboardingApplicationRepository applicationRepository;

    @InjectMocks
    private DeclarationService declarationService;

    private List<DeclarationRuleConfig> activeRules;
    private OnboardingApplication sampleApp;

    @BeforeEach
    void setUp() {
        activeRules = List.of(
                new DeclarationRuleConfig("DECL_TERMS", "Terms", true, true, null),
                new DeclarationRuleConfig("DECL_TAX", "Tax Residency", false, true, "TAX_FORM_PDF")
        );
        sampleApp = new OnboardingApplication("APP-001", "DRAFT");
    }

    @Test
    @DisplayName("SAVE_DRAFT should succeed even when mandatory declaration is unchecked")
    void saveDraft_allowsUncheckedMandatoryFields() {
        when(applicationRepository.findById("APP-001")).thenReturn(Optional.of(sampleApp));
        when(ruleRepository.findByActiveTrueOrderByIdAsc()).thenReturn(activeRules);

        DeclarationUpdateRequest request = new DeclarationUpdateRequest();
        request.setAction(SaveActionType.SAVE_DRAFT);
        request.setSelections(Map.of("DECL_TERMS", false, "DECL_TAX", false));

        DeclarationPageResponse response = declarationService.updateDeclarations("APP-001", request);

        assertEquals("DRAFT", response.getStatus());
        verify(applicationRepository, times(1)).save(sampleApp);
    }

    @Test
    @DisplayName("SAVE_AND_CONTINUE should throw MandatoryDeclarationException when mandatory item is false")
    void saveAndContinue_blocksMissingMandatoryDeclaration() {
        when(applicationRepository.findById("APP-001")).thenReturn(Optional.of(sampleApp));
        when(ruleRepository.findByActiveTrueOrderByIdAsc()).thenReturn(activeRules);

        DeclarationUpdateRequest request = new DeclarationUpdateRequest();
        request.setAction(SaveActionType.SAVE_AND_CONTINUE);
        request.setSelections(Map.of("DECL_TERMS", false, "DECL_TAX", true));

        assertThrows(MandatoryDeclarationException.class,
                () -> declarationService.updateDeclarations("APP-001", request));

        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("SAVE_AND_CONTINUE should compute carried-over documents for Step 8 when valid")
    void saveAndContinue_carriesRequiredDocumentsForward() {
        when(applicationRepository.findById("APP-001")).thenReturn(Optional.of(sampleApp));
        when(ruleRepository.findByActiveTrueOrderByIdAsc()).thenReturn(activeRules);

        DeclarationUpdateRequest request = new DeclarationUpdateRequest();
        request.setAction(SaveActionType.SAVE_AND_CONTINUE);
        request.setSelections(Map.of("DECL_TERMS", true, "DECL_TAX", true));

        DeclarationPageResponse response = declarationService.updateDeclarations("APP-001", request);

        assertEquals("DECLARATIONS_COMPLETED", response.getStatus());
        assertTrue(response.getRequiredDocumentsForNextStep().contains("TAX_FORM_PDF"));
        verify(applicationRepository, times(1)).save(sampleApp);
    }

    @Test
    @DisplayName("GET config should flag configDriftDetected when Admin flips optional rule to mandatory mid-draft")
    void getConfig_detectsAdminFlagChangeSinceDraft() {
        // User previously saved DECL_TAX as false when it was optional
        sampleApp.setDeclarationAnswers(Map.of("DECL_TERMS", true, "DECL_TAX", false));

        // Admin later flips DECL_TAX to mandatory = true
        List<DeclarationRuleConfig> updatedAdminRules = List.of(
                new DeclarationRuleConfig("DECL_TERMS", "Terms", true, true, null),
                new DeclarationRuleConfig("DECL_TAX", "Tax Residency", true, true, "TAX_FORM_PDF")
        );

        when(applicationRepository.findById("APP-001")).thenReturn(Optional.of(sampleApp));
        when(ruleRepository.findByActiveTrueOrderByIdAsc()).thenReturn(updatedAdminRules);

        DeclarationPageResponse response = declarationService.getDeclarationConfig("APP-001");

        assertTrue(response.isConfigDriftDetected());
    }

    @Test
    @DisplayName("PUT update should throw AdminConfigChangedException if client keys do not match live Admin rules")
    void updateDeclarations_rejectsStaleClientKeys() {
        when(applicationRepository.findById("APP-001")).thenReturn(Optional.of(sampleApp));
        when(ruleRepository.findByActiveTrueOrderByIdAsc()).thenReturn(activeRules);

        DeclarationUpdateRequest request = new DeclarationUpdateRequest();
        request.setAction(SaveActionType.SAVE_DRAFT);
        request.setSelections(Map.of("DECL_TERMS", true)); // Missing DECL_TAX key

        assertThrows(AdminConfigChangedException.class,
                () -> declarationService.updateDeclarations("APP-001", request));
    }
}