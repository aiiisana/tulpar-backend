package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.DailyChallenge;
import kz.diploma.tulpar.dto.request.CreateDailyChallengeRequest;
import kz.diploma.tulpar.dto.response.DailyChallengeResponse;
import kz.diploma.tulpar.dto.response.DailyChallengeSubmitResponse;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.DailyChallengeRepository;
import kz.diploma.tulpar.repository.UserDailyActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final StreakService streakService;

    @Cacheable("daily-challenge-v2")
    @Transactional(readOnly = true)
    public DailyChallengeResponse getForToday() {
        return repository.findByChallengeDate(LocalDate.now())
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No daily challenge available for today"));
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

        // Already earned XP today — idempotent, safe to call multiple times.
        boolean alreadyDone = activityRepository
                .findByUserIdAndActivityDate(userId, today)
                .map(a -> a.isCompleted())
                .orElse(false);

        if (alreadyDone) {
            return 0;
        }

        streakService.recordActivityAndAddXp(userId, CHALLENGE_XP);
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
                    .findByUserIdAndActivityDate(userId, today)
                    .map(a -> a.isCompleted())
                    .orElse(false);

            if (!alreadyDone) {
                streakService.recordActivityAndAddXp(userId, CHALLENGE_XP);
                xpAwarded = CHALLENGE_XP;
            }
        }

        return DailyChallengeSubmitResponse.builder()
                .correct(correct)
                .xpAwarded(xpAwarded)
                .correctWord(correctWord)
                .build();
    }

    @CacheEvict(value = "daily-challenge-v2", allEntries = true)
    @Transactional
    public DailyChallengeResponse create(CreateDailyChallengeRequest req) {
        DailyChallenge saved = repository.save(DailyChallenge.builder()
                .challengeDate(req.getChallengeDate())
                .correctWord(req.getCorrectWord())
                .letters(req.getLetters())
                .imageUrls(req.getImageUrls())
                .build());
        return toResponse(saved);
    }

    private DailyChallengeResponse toResponse(DailyChallenge c) {
        return DailyChallengeResponse.builder()
                .id(c.getId())
                .challengeDate(c.getChallengeDate())
                .letters(c.getLetters())
                .imageUrls(c.getImageUrls())
                .wordLength(c.getCorrectWord() != null ? c.getCorrectWord().length() : 0)
                .correctWord(c.getCorrectWord())
                .build();
    }
}
