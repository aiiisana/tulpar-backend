package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.Flashcard;
import kz.diploma.tulpar.domain.entity.SavedFlashcard;
import kz.diploma.tulpar.domain.entity.SavedFlashcardId;
import kz.diploma.tulpar.dto.request.CreateFlashcardRequest;
import kz.diploma.tulpar.dto.response.FlashcardResponse;
import kz.diploma.tulpar.dto.response.PageResponse;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.FlashcardRepository;
import kz.diploma.tulpar.repository.SavedFlashcardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final SavedFlashcardRepository savedFlashcardRepository;

    @Cacheable(value = "flashcards", key = "#page + '-' + #size")
    @Transactional(readOnly = true)
    public PageResponse<FlashcardResponse> findAll(int page, int size) {
        return PageResponse.from(
                flashcardRepository.findAll(PageRequest.of(page, size)).map(this::toResponse));
    }

    @CacheEvict(value = "flashcards", allEntries = true)
    @Transactional
    public FlashcardResponse create(CreateFlashcardRequest req) {
        Flashcard saved = flashcardRepository.save(Flashcard.builder()
                .wordRu(req.getWordRu())
                .wordKz(req.getWordKz())
                .transcription(req.getTranscription())
                .exampleSentence(req.getExampleSentence())
                .audioUrl(req.getAudioUrl())
                .build());
        return toResponse(saved);
    }

    @CacheEvict(value = "flashcards", allEntries = true)
    @Transactional
    public void delete(UUID id) {
        if (!flashcardRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Flashcard", id);
        }
        flashcardRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<FlashcardResponse> getSaved(String uid) {
        return savedFlashcardRepository.findByIdUserId(uid).stream()
                .map(sf -> toResponse(sf.getFlashcard()))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isSaved(String uid, UUID flashcardId) {
        if (!flashcardRepository.existsById(flashcardId)) {
            throw ResourceNotFoundException.of("Flashcard", flashcardId);
        }
        return savedFlashcardRepository.existsByIdUserIdAndIdFlashcardId(uid, flashcardId);
    }

    @Transactional
    public void save(String uid, UUID flashcardId) {
        if (!flashcardRepository.existsById(flashcardId)) {
            throw ResourceNotFoundException.of("Flashcard", flashcardId);
        }
        SavedFlashcardId key = new SavedFlashcardId(uid, flashcardId);
        if (!savedFlashcardRepository.existsById(key)) {
            savedFlashcardRepository.save(SavedFlashcard.builder()
                    .id(key)
                    .flashcard(flashcardRepository.getReferenceById(flashcardId))
                    .build());
        }
    }

    @Transactional
    public void unsave(String uid, UUID flashcardId) {
        savedFlashcardRepository.deleteByIdUserIdAndIdFlashcardId(uid, flashcardId);
    }

    private FlashcardResponse toResponse(Flashcard f) {
        return FlashcardResponse.builder()
                .id(f.getId())
                .wordRu(f.getWordRu())
                .wordKz(f.getWordKz())
                .transcription(f.getTranscription())
                .exampleSentence(f.getExampleSentence())
                .audioUrl(f.getAudioUrl())
                .build();
    }
}
