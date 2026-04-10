package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.DailyChallenge;
import kz.diploma.tulpar.dto.request.CreateDailyChallengeRequest;
import kz.diploma.tulpar.dto.response.DailyChallengeResponse;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.DailyChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DailyChallengeService {

    private final DailyChallengeRepository repository;

    @Cacheable("daily-challenge-v2")
    @Transactional(readOnly = true)
    public DailyChallengeResponse getForToday() {
        return repository.findByChallengeDate(LocalDate.now())
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No daily challenge available for today"));
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
