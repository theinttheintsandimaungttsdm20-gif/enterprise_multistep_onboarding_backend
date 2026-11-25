package com.enterprise.onboarding.repository;

import com.enterprise.onboarding.model.OnboardingApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardingApplicationRepository extends JpaRepository<OnboardingApplication, String> {
}
