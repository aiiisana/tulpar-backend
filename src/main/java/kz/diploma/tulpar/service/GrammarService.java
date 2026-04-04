package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.GrammarRule;
import kz.diploma.tulpar.dto.request.CreateGrammarRuleRequest;
import kz.diploma.tulpar.dto.response.GrammarRuleResponse;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.GrammarRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GrammarService {

    private final GrammarRuleRepository repository;

    @Cacheable("grammar-rules")
    @Transactional(readOnly = true)
    public List<GrammarRuleResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Cacheable(value = "grammar-rule-detail", key = "#id")
    @Transactional(readOnly = true)
    public GrammarRuleResponse findById(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.of("GrammarRule", id));
    }

    @Caching(evict = {
            @CacheEvict(value = "grammar-rules", allEntries = true),
            @CacheEvict(value = "grammar-rule-detail", allEntries = true)
    })
    @Transactional
    public GrammarRuleResponse create(CreateGrammarRuleRequest req) {
        GrammarRule saved = repository.save(GrammarRule.builder()
                .title(req.getTitle())
                .explanation(req.getExplanation())
                .examples(req.getExamples() != null ? req.getExamples() : List.of())
                .build());
        return toResponse(saved);
    }

    @Caching(evict = {
            @CacheEvict(value = "grammar-rules", allEntries = true),
            @CacheEvict(value = "grammar-rule-detail", key = "#id")
    })
    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException.of("GrammarRule", id);
        }
        repository.deleteById(id);
    }

    private GrammarRuleResponse toResponse(GrammarRule r) {
        return GrammarRuleResponse.builder()
                .id(r.getId())
                .title(r.getTitle())
                .explanation(r.getExplanation())
                .examples(r.getExamples())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
