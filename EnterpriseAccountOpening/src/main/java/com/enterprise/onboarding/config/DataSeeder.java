package com.enterprise.onboarding.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.enterprise.onboarding.model.DeclarationRuleConfig;
import com.enterprise.onboarding.model.OnboardingApplication;
import com.enterprise.onboarding.repository.DeclarationRuleRepository;
import com.enterprise.onboarding.repository.OnboardingApplicationRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedDatabase(DeclarationRuleRepository ruleRepo,
                                   OnboardingApplicationRepository appRepo) {
        return args -> {
            ruleRepo.save(new DeclarationRuleConfig(
                    "DECL_TERMS_POLICY",
                    "I confirm all corporate account information provided is accurate.",
                    true, true, null
            ));
            ruleRepo.save(new DeclarationRuleConfig(
                    "DECL_TAX_RESIDENCY",
                    "I declare foreign tax residency obligations (triggers Tax Form upload).",
                    false, true, "TAX_SELF_CERTIFICATION_PDF"
            ));
            ruleRepo.save(new DeclarationRuleConfig(
                    "DECL_BENEFICIAL_OWNER",
                    "I confirm Ultimate Beneficial Ownership (UBO) details are declared.",
                    true, true, "UBO_STRUCTURE_CHART"
            ));

            appRepo.save(new OnboardingApplication("APP-2026-001", "DRAFT"));
        };
    }
}
