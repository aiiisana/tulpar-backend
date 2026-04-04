package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.Article;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {
    Page<Article> findAllByDifficultyLevel(DifficultyLevel level, Pageable pageable);
}
