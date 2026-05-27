package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.PlacementQuestion;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import kz.diploma.tulpar.dto.request.PlacementSubmitRequest;
import kz.diploma.tulpar.dto.response.PlacementQuestionResponse;
import kz.diploma.tulpar.dto.response.PlacementResultResponse;
import kz.diploma.tulpar.repository.PlacementQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlacementTestService {

    private final PlacementQuestionRepository questionRepository;
    private final OnboardingService onboardingService;
    private final AchievementService achievementService;

    @Transactional(readOnly = true)
    public List<PlacementQuestionResponse> getQuestions() {
        return questionRepository.findAllByOrderByOrderIndexAsc().stream()
                .map(q -> PlacementQuestionResponse.builder()
                        .id(q.getId())
                        .orderIndex(q.getOrderIndex())
                        .question(q.getQuestion())
                        .options(q.getOptions())
                        .build())
                .toList();
    }

    /**
     * Scores the submitted answers, determines the appropriate difficulty level,
     * persists it on the user's profile and awards the PLACEMENT_DONE achievement.
     *
     * <p>Scoring algorithm:
     * <ul>
     *   <li>Questions are evenly spread across 4 difficulty tiers (3 per tier).</li>
     *   <li>Score per tier = correct answers in that tier / total questions in tier.</li>
     *   <li>The highest tier where score ≥ 50 % determines the level.
     *       If no tier reaches 50 %, level defaults to BEGINNER.</li>
     * </ul>
     */
    @Transactional
    public PlacementResultResponse submit(String userId,
                                          PlacementSubmitRequest req) {

        // Load all questions into a map for O(1) lookup
        Map<UUID, PlacementQuestion> qMap = questionRepository
                .findAllByOrderByOrderIndexAsc().stream()
                .collect(Collectors.toMap(PlacementQuestion::getId, q -> q));

        // Count correct answers per difficulty tier
        Map<DifficultyLevel, Long> totalPerTier = qMap.values().stream()
                .collect(Collectors.groupingBy(
                        PlacementQuestion::getDifficulty, Collectors.counting()));

        Map<DifficultyLevel, Long> correctPerTier = req.getAnswers().stream()
                .filter(a -> {
                    PlacementQuestion q = qMap.get(a.getQuestionId());
                    return q != null && q.getCorrectIndex() == a.getSelectedIndex();
                })
                .collect(Collectors.groupingBy(
                        a -> qMap.get(a.getQuestionId()).getDifficulty(),
                        Collectors.counting()));

        int totalCorrect = (int) correctPerTier.values().stream()
                .mapToLong(Long::longValue).sum();
        int totalQ = qMap.size();
        int scorePercent = totalQ > 0 ? (int) Math.round(totalCorrect * 100.0 / totalQ) : 0;

        // Determine the highest tier with ≥ 50% correct answers
        DifficultyLevel[] tiers = {
                DifficultyLevel.BEGINNER,
                DifficultyLevel.ELEMENTARY,
                DifficultyLevel.INTERMEDIATE,
                DifficultyLevel.ADVANCED
        };
        DifficultyLevel determined = DifficultyLevel.BEGINNER;
        for (DifficultyLevel tier : tiers) {
            long total   = totalPerTier.getOrDefault(tier, 1L);
            long correct = correctPerTier.getOrDefault(tier, 0L);
            if ((double) correct / total >= 0.5) {
                determined = tier;
            }
        }

        // Persist level + award achievement
        onboardingService.setLevel(userId, determined);
        achievementService.award(userId, "PLACEMENT_DONE");

        String label = switch (determined) {
            case BEGINNER      -> "Начинающий";
            case ELEMENTARY    -> "Элементарный";
            case INTERMEDIATE  -> "Средний";
            case ADVANCED      -> "Продвинутый";
        };

        String message = switch (determined) {
            case BEGINNER      -> "Отличное начало! Начнём с основ казахского языка.";
            case ELEMENTARY    -> "Хорошие базовые знания! Продолжим развивать их.";
            case INTERMEDIATE  -> "Неплохой уровень! Углубим ваши знания.";
            case ADVANCED      -> "Отличный уровень! Работаем над совершенствованием.";
        };

        return PlacementResultResponse.builder()
                .totalQuestions(totalQ)
                .correctAnswers(totalCorrect)
                .scorePercent(scorePercent)
                .determinedLevel(determined)
                .levelLabel(label)
                .message(message)
                .build();
    }
}
