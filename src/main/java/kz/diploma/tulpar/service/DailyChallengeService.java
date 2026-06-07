package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.DailyChallenge;
import kz.diploma.tulpar.domain.entity.User;
import kz.diploma.tulpar.domain.entity.UserDailyActivity;
import kz.diploma.tulpar.dto.request.CreateDailyChallengeRequest;
import kz.diploma.tulpar.dto.response.DailyChallengeResponse;
import kz.diploma.tulpar.dto.response.DailyChallengeSubmitResponse;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.DailyChallengeRepository;
import kz.diploma.tulpar.repository.UserDailyActivityRepository;
import kz.diploma.tulpar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DailyChallengeService {

    private static final int CHALLENGE_XP = 10;

    private final DailyChallengeRepository repository;
    private final UserDailyActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final StreakService streakService;
    private final AchievementService achievementService;

    /**
     * Returns today's challenge. When {@code userId} is non-null (authenticated user),
     * the response includes {@code completedByCurrentUser} so the client knows
     * whether to show the "completed" screen without any local-cache tricks.
     *
     * @param userId Firebase UID of the caller, or {@code null} for anonymous access.
     */
    @Transactional(readOnly = true)
    public DailyChallengeResponse getForToday(String userId) {
        DailyChallenge challenge = repository.findByChallengeDate(LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("No daily challenge available for today"));

        // Use challenge_completed (not completed) — the latter is set for ANY
        // activity (lessons, exercises), which would cause false positives.
        boolean completedByCurrentUser = userId != null &&
                activityRepository.existsByUserIdAndActivityDateAndChallengeCompletedTrue(
                        userId, LocalDate.now());

        return toResponse(challenge, completedByCurrentUser);
    }

    /**
     * Records a daily challenge completion for the given user.
     * Awards {@value CHALLENGE_XP} XP and updates the streak, but only once per calendar day.
     *
     * @return XP awarded (10), or 0 if the user already completed a challenge today.
     */
    @Transactional
    public int complete(String userId, UUID challengeId) {
        if (!repository.existsById(challengeId)) {
            throw ResourceNotFoundException.of("DailyChallenge", challengeId);
        }

        LocalDate today = LocalDate.now();

        // Already completed the challenge today — idempotent.
        if (activityRepository.existsByUserIdAndActivityDateAndChallengeCompletedTrue(userId, today)) {
            return 0;
        }

        streakService.recordActivityAndAddXp(userId, CHALLENGE_XP);
        markChallengeCompleted(userId, today);
        return CHALLENGE_XP;
    }

    /**
     * Verifies the user's answer against the stored correct word and, if correct,
     * awards +10 XP (once per calendar day, idempotent).
     *
     * <p>Answer comparison is case-insensitive and trims surrounding whitespace.
     *
     * @param userId      Firebase UID of the authenticated user.
     * @param challengeId UUID of the daily challenge.
     * @param userAnswer  The word assembled by the user.
     * @return Result with correct flag, XP awarded, and the correct word.
     */
    @Transactional
    public DailyChallengeSubmitResponse submit(String userId, UUID challengeId, String userAnswer) {
        DailyChallenge challenge = repository.findById(challengeId)
                .orElseThrow(() -> ResourceNotFoundException.of("DailyChallenge", challengeId));

        String correctWord = challenge.getCorrectWord() != null
                ? challenge.getCorrectWord().trim()
                : "";
        boolean correct = correctWord.equalsIgnoreCase(userAnswer != null ? userAnswer.trim() : "");

        int xpAwarded = 0;
        if (correct) {
            LocalDate today = LocalDate.now();
            boolean alreadyDone = activityRepository
                    .existsByUserIdAndActivityDateAndChallengeCompletedTrue(userId, today);

            if (!alreadyDone) {
                streakService.recordActivityAndAddXp(userId, CHALLENGE_XP);
                markChallengeCompleted(userId, today);
                xpAwarded = CHALLENGE_XP;

                // Award FIRST_DAILY on any first-time completion
                achievementService.award(userId, "FIRST_DAILY");

                // Award DAILY_7 if the user has completed daily challenge 7+ days in the last 7 days
                long challengeDaysInWeek = activityRepository
                        .countByUserIdAndActivityDateBetweenAndChallengeCompletedTrue(
                                userId, today.minusDays(6), today);
                if (challengeDaysInWeek >= 7) {
                    achievementService.award(userId, "DAILY_7");
                }
            }
        }

        return DailyChallengeSubmitResponse.builder()
                .correct(correct)
                .xpAwarded(xpAwarded)
                .correctWord(correctWord)
                .build();
    }

    @Transactional
    public DailyChallengeResponse create(CreateDailyChallengeRequest req) {
        DailyChallenge saved = repository.save(DailyChallenge.builder()
                .challengeDate(req.getChallengeDate())
                .correctWord(req.getCorrectWord())
                .letters(req.getLetters())
                .imageUrls(req.getImageUrls())
                .build());
        return toResponse(saved, false);
    }

    /**
     * Upserts today's UserDailyActivity and sets {@code challengeCompleted = true}.
     * Called after a correct answer is confirmed. Idempotent.
     */
    private void markChallengeCompleted(String userId, LocalDate date) {
        UserDailyActivity activity = activityRepository
                .findByUserIdAndActivityDate(userId, date)
                .orElseGet(() -> {
                    User user = userRepository.getReferenceById(userId);
                    return UserDailyActivity.builder()
                            .user(user)
                            .activityDate(date)
                            .build();
                });
        activity.setChallengeCompleted(true);
        activityRepository.save(activity);
    }

    private DailyChallengeResponse toResponse(DailyChallenge c, boolean completedByCurrentUser) {
        return DailyChallengeResponse.builder()
                .id(c.getId())
                .challengeDate(c.getChallengeDate())
                .letters(c.getLetters())
                .imageUrls(c.getImageUrls())
                .wordLength(c.getCorrectWord() != null ? c.getCorrectWord().length() : 0)
                .correctWord(c.getCorrectWord())
                .completedByCurrentUser(completedByCurrentUser)
                .build();
    }
}
