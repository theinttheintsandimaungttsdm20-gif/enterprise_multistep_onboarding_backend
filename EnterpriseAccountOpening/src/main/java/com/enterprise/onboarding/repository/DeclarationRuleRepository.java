package com.enterprise.onboarding.repository;

import com.enterprise.onboarding.model.DeclarationRuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeclarationRuleRepository extends JpaRepository<DeclarationRuleConfig, Long> {
    List<DeclarationRuleConfig> findByActiveTrueOrderByIdAsc();
}
