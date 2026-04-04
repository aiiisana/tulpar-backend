package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.Article;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import kz.diploma.tulpar.dto.request.CreateArticleRequest;
import kz.diploma.tulpar.dto.response.ArticleResponse;
import kz.diploma.tulpar.dto.response.PageResponse;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    @Cacheable(value = "articles", key = "#page + '-' + #size + '-' + #difficulty")
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> findAll(int page, int size, DifficultyLevel difficulty) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var result = difficulty != null
                ? articleRepository.findAllByDifficultyLevel(difficulty, pageable)
                : articleRepository.findAll(pageable);
        return PageResponse.from(result.map(a -> toResponse(a, false)));
    }

    @Cacheable(value = "article-detail", key = "#id")
    @Transactional(readOnly = true)
    public ArticleResponse findById(UUID id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Article", id));
        return toResponse(article, true);
    }

    @Caching(evict = {
            @CacheEvict(value = "articles", allEntries = true),
            @CacheEvict(value = "article-detail", allEntries = true)
    })
    @Transactional
    public ArticleResponse create(CreateArticleRequest req) {
        Article saved = articleRepository.save(Article.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .difficultyLevel(req.getDifficultyLevel())
                .build());
        return toResponse(saved, true);
    }

    @Caching(evict = {
            @CacheEvict(value = "articles", allEntries = true),
            @CacheEvict(value = "article-detail", key = "#id")
    })
    @Transactional
    public void delete(UUID id) {
        if (!articleRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Article", id);
        }
        articleRepository.deleteById(id);
    }

    private ArticleResponse toResponse(Article a, boolean includeContent) {
        return ArticleResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(includeContent ? a.getContent() : null)
                .difficultyLevel(a.getDifficultyLevel())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
