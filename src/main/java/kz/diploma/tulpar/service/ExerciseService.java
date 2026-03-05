package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.*;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import kz.diploma.tulpar.domain.enums.ExerciseType;
import kz.diploma.tulpar.dto.request.CreateExerciseRequest;
import kz.diploma.tulpar.dto.request.UpdateExerciseRequest;
import kz.diploma.tulpar.dto.response.ExerciseResponse;
import kz.diploma.tulpar.dto.response.PageResponse;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.ExerciseRepository;
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
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    // ─── Read operations (cached) ────────────────────────────────────────────

    @Cacheable(value = "exercises", key = "#page + '-' + #size + '-' + #type + '-' + #difficulty")
    @Transactional(readOnly = true)
    public PageResponse<ExerciseResponse> findAll(int page, int size,
                                                   ExerciseType type,
                                                   DifficultyLevel difficulty) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        var result = (type != null && difficulty != null)
                ? exerciseRepository.findAllByTypeAndDifficultyLevel(type, difficulty, pageable)
                : (type != null)
                ? exerciseRepository.findAllByType(type, pageable)
                : (difficulty != null)
                ? exerciseRepository.findAllByDifficultyLevel(difficulty, pageable)
                : exerciseRepository.findAll(pageable);

        return PageResponse.from(result.map(this::toResponse));
    }

    @Cacheable(value = "exercise-detail", key = "#id")
    @Transactional(readOnly = true)
    public ExerciseResponse findById(UUID id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Exercise", id));
        return toResponse(exercise);
    }

    // ─── Write operations (cache-evicting) ───────────────────────────────────

    @CacheEvict(value = "exercises", allEntries = true)
    @Transactional
    public ExerciseResponse create(CreateExerciseRequest req) {
        Exercise exercise = buildExercise(req);
        return toResponse(exerciseRepository.save(exercise));
    }

    @Caching(evict = {
        @CacheEvict(value = "exercises", allEntries = true),
        @CacheEvict(value = "exercise-detail", key = "#id")
    })
    @Transactional
    public ExerciseResponse update(UUID id, UpdateExerciseRequest req) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Exercise", id));
        applyUpdates(exercise, req);
        return toResponse(exerciseRepository.save(exercise));
    }

    @Caching(evict = {
        @CacheEvict(value = "exercises", allEntries = true),
        @CacheEvict(value = "exercise-detail", key = "#id")
    })
    @Transactional
    public void delete(UUID id) {
        if (!exerciseRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Exercise", id);
        }
        exerciseRepository.deleteById(id);
    }

    // ─── Mapping ─────────────────────────────────────────────────────────────

    public ExerciseResponse toResponse(Exercise ex) {
        ExerciseResponse.ExerciseResponseBuilder builder = ExerciseResponse.builder()
                .id(ex.getId())
                .type(ex.getType())
                .difficultyLevel(ex.getDifficultyLevel())
                .question(ex.getQuestion())
                .explanation(ex.getExplanation())
                .createdAt(ex.getCreatedAt());

        switch (ex) {
            case VocabularyExercise v -> builder
                    .word(v.getWord())
                    .translation(v.getTranslation())
                    .options(v.getOptions());
                    // correctAnswer intentionally omitted from response
            case ListeningExercise l -> builder
                    .audioUrl(l.getAudioUrl())
                    .transcript(l.getTranscript())
                    .options(l.getOptions());
            case VideoExercise v -> builder
                    .videoUrl(v.getVideoUrl())
                    .startTime(v.getStartTime())
                    .endTime(v.getEndTime())
                    .options(v.getOptions());
            case ImageExercise i -> builder
                    .imageUrl(i.getImageUrl())
                    .options(i.getOptions());
            case SentenceBuilderExercise s -> builder
                    .shuffledWords(s.getShuffledWords());
            default -> { /* base Exercise — no extra fields */ }
        }

        return builder.build();
    }

    // ─── Factory / update helpers ────────────────────────────────────────────

    private Exercise buildExercise(CreateExerciseRequest req) {
        return switch (req.getType()) {
            case VOCABULARY -> {
                VocabularyExercise e = new VocabularyExercise();
                copyBase(e, req);
                e.setWord(req.getWord());
                e.setTranslation(req.getTranslation());
                e.setOptions(req.getOptions());
                e.setCorrectAnswer(req.getCorrectAnswer());
                yield e;
            }
            case LISTENING -> {
                ListeningExercise e = new ListeningExercise();
                copyBase(e, req);
                e.setAudioUrl(req.getAudioUrl());
                e.setTranscript(req.getTranscript());
                e.setOptions(req.getOptions());
                e.setCorrectAnswer(req.getCorrectAnswer());
                yield e;
            }
            case VIDEO_CONTEXT -> {
                VideoExercise e = new VideoExercise();
                copyBase(e, req);
                e.setVideoUrl(req.getVideoUrl());
                e.setStartTime(req.getStartTime());
                e.setEndTime(req.getEndTime());
                e.setOptions(req.getOptions());
                e.setCorrectAnswer(req.getCorrectAnswer());
                yield e;
            }
            case IMAGE_CONTEXT -> {
                ImageExercise e = new ImageExercise();
                copyBase(e, req);
                e.setImageUrl(req.getImageUrl());
                e.setOptions(req.getOptions());
                e.setCorrectAnswer(req.getCorrectAnswer());
                yield e;
            }
            case SENTENCE_BUILDER -> {
                SentenceBuilderExercise e = new SentenceBuilderExercise();
                copyBase(e, req);
                e.setCorrectSentence(req.getCorrectSentence());
                e.setShuffledWords(req.getShuffledWords());
                yield e;
            }
            default -> throw new IllegalArgumentException("Unsupported exercise type: " + req.getType());
        };
    }

    private void copyBase(Exercise e, CreateExerciseRequest req) {
        e.setType(req.getType());
        e.setDifficultyLevel(req.getDifficultyLevel());
        e.setQuestion(req.getQuestion());
        e.setExplanation(req.getExplanation());
    }

    private void applyUpdates(Exercise exercise, UpdateExerciseRequest req) {
        if (req.getDifficultyLevel() != null) exercise.setDifficultyLevel(req.getDifficultyLevel());
        if (req.getQuestion()        != null) exercise.setQuestion(req.getQuestion());
        if (req.getExplanation()     != null) exercise.setExplanation(req.getExplanation());

        switch (exercise) {
            case VocabularyExercise v -> {
                if (req.getWord()          != null) v.setWord(req.getWord());
                if (req.getTranslation()   != null) v.setTranslation(req.getTranslation());
                if (req.getOptions()       != null) v.setOptions(req.getOptions());
                if (req.getCorrectAnswer() != null) v.setCorrectAnswer(req.getCorrectAnswer());
            }
            case ListeningExercise l -> {
                if (req.getAudioUrl()      != null) l.setAudioUrl(req.getAudioUrl());
                if (req.getTranscript()    != null) l.setTranscript(req.getTranscript());
                if (req.getOptions()       != null) l.setOptions(req.getOptions());
                if (req.getCorrectAnswer() != null) l.setCorrectAnswer(req.getCorrectAnswer());
            }
            case VideoExercise v -> {
                if (req.getVideoUrl()      != null) v.setVideoUrl(req.getVideoUrl());
                if (req.getStartTime()     != null) v.setStartTime(req.getStartTime());
                if (req.getEndTime()       != null) v.setEndTime(req.getEndTime());
                if (req.getOptions()       != null) v.setOptions(req.getOptions());
                if (req.getCorrectAnswer() != null) v.setCorrectAnswer(req.getCorrectAnswer());
            }
            case ImageExercise i -> {
                if (req.getImageUrl()      != null) i.setImageUrl(req.getImageUrl());
                if (req.getOptions()       != null) i.setOptions(req.getOptions());
                if (req.getCorrectAnswer() != null) i.setCorrectAnswer(req.getCorrectAnswer());
            }
            case SentenceBuilderExercise s -> {
                if (req.getCorrectSentence() != null) s.setCorrectSentence(req.getCorrectSentence());
                if (req.getShuffledWords()   != null) s.setShuffledWords(req.getShuffledWords());
            }
            default -> { /* base — nothing extra to update */ }
        }
    }
}
