package com.enterprise.onboarding.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enterprise.onboarding.dto.DeclarationPageResponse;
import com.enterprise.onboarding.dto.DeclarationUpdateRequest;
import com.enterprise.onboarding.model.DeclarationRuleConfig;
import com.enterprise.onboarding.repository.DeclarationRuleRepository;
import com.enterprise.onboarding.service.DeclarationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:4200")
public class DeclarationController {

    private final DeclarationService declarationService;
    private final DeclarationRuleRepository ruleRepository;

    public DeclarationController(DeclarationService declarationService,
                                 DeclarationRuleRepository ruleRepository) {
        this.declarationService = declarationService;
        this.ruleRepository = ruleRepository;
    }

    @GetMapping("/onboarding/{applicationId}/declarations")
    @PreAuthorize("hasAnyRole('APPLICANT', 'ADMIN')")
    public ResponseEntity<DeclarationPageResponse> getDeclarations(@PathVariable String applicationId) {
        return ResponseEntity.ok(declarationService.getDeclarationConfig(applicationId));
    }

    @PutMapping("/onboarding/{applicationId}/declarations")
    @PreAuthorize("hasRole('APPLICANT')")
    public ResponseEntity<DeclarationPageResponse> updateDeclarations(
            @PathVariable String applicationId,
            @Valid @RequestBody DeclarationUpdateRequest request) {
        return ResponseEntity.ok(declarationService.updateDeclarations(applicationId, request));
    }

    @PostMapping("/admin/declarations/{code}/toggle-mandatory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> simulateAdminFlagChangeMandatory(@PathVariable String code) {
        DeclarationRuleConfig rule = ruleRepository.findByActiveTrueOrderByIdAsc().stream()
                .filter(r -> r.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + code));

        rule.setMandatory(true);
        ruleRepository.save(rule);

        return ResponseEntity.ok("Admin toggled " + code + " mandatory=" + rule.isMandatory());
    }
    
    @PostMapping("/admin/declarations/{code}/toggle-optional")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> simulateAdminFlagChangeOptional(@PathVariable String code) {
        DeclarationRuleConfig rule = ruleRepository.findByActiveTrueOrderByIdAsc().stream()
                .filter(r -> r.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + code));

        rule.setMandatory(false);
        ruleRepository.save(rule);
        System.out.println("Is mandatory: "+rule.isMandatory());
        return ResponseEntity.ok("Admin toggled " + code + " optional=" + !rule.isMandatory());
    }
}