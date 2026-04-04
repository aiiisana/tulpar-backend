package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.GrammarRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GrammarRuleRepository extends JpaRepository<GrammarRule, UUID> {
}
