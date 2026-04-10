package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.Course;
import kz.diploma.tulpar.domain.entity.CourseLevel;
import kz.diploma.tulpar.domain.entity.Lesson;
import kz.diploma.tulpar.domain.entity.User;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import kz.diploma.tulpar.domain.enums.ProgressStatus;
import kz.diploma.tulpar.dto.request.CreateCourseLevelRequest;
import kz.diploma.tulpar.dto.request.CreateCourseRequest;
import kz.diploma.tulpar.dto.request.CreateLessonRequest;
import kz.diploma.tulpar.dto.response.*;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final CourseRepository courseRepository;
    private final CourseLevelRepository levelRepository;
    private final LessonRepository lessonRepository;
    private final LessonExerciseRepository lessonExerciseRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;
    private final ExerciseService exerciseService;

    @Cacheable("courses")
    @Transactional(readOnly = true)
    public List<CourseResponse> getCourses() {
        return courseRepository.findAllByOrderByOrderIndexAsc().stream()
                .map(c -> CourseResponse.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .description(c.getDescription())
                        .orderIndex(c.getOrderIndex())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseLevelResponse> getCourseLevels(UUID courseId, String userId) {
        if (!courseRepository.existsById(courseId)) {
            throw ResourceNotFoundException.of("Course", courseId);
        }

        // If user has a language level set, show only that level's lessons.
        // Otherwise fall back to showing all levels (e.g. admin / not onboarded yet).
        User user = userRepository.findById(userId).orElse(null);
        DifficultyLevel userLevel = user != null ? user.getLevel() : null;

        List<CourseLevel> levels;
        if (userLevel != null) {
            levels = levelRepository
                    .findByCourseIdAndDifficultyLevel(courseId, userLevel)
                    .map(List::of)
                    .orElseGet(() -> levelRepository.findAllByCourseIdOrderByOrderIndexAsc(courseId));
        } else {
            levels = levelRepository.findAllByCourseIdOrderByOrderIndexAsc(courseId);
        }

        return levels.stream()
                .map(lvl -> {
                    List<LessonResponse> lessons = lessonRepository
                            .findAllByLevelIdOrderByOrderIndexAsc(lvl.getId()).stream()
                            .map(lesson -> toLessonSummary(lesson, userId))
                            .toList();
                    return CourseLevelResponse.builder()
                            .id(lvl.getId())
                            .title(lvl.getTitle())
                            .orderIndex(lvl.getOrderIndex())
                            .lessons(lessons)
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public LessonResponse getLessonDetail(UUID lessonId, String userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> ResourceNotFoundException.of("Lesson", lessonId));

        // IMPORTANT: use the scalar-ID projection so that no base Exercise proxy
        // is placed in the Hibernate L1-cache before the subtypes are loaded.
        // Accessing le.getExercise() first would put an Exercise (base) proxy into
        // the L1-cache; a subsequent findById() would return that proxy, and the
        // pattern-matching switch in toResponse() would hit the `default` branch,
        // leaving `options` / `shuffledWords` null.
        List<ExerciseResponse> exercises = lessonExerciseRepository
                .findExerciseIdsByLessonIdOrdered(lessonId).stream()
                .map(exerciseService::findById)
                .toList();

        return LessonResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .orderIndex(lesson.getOrderIndex())
                .xpReward(lesson.getXpReward())
                .unlocked(isUnlocked(lesson, userId))
                .completed(isCompleted(lesson, userId))
                .exercises(exercises)
                .build();
    }

    private LessonResponse toLessonSummary(Lesson lesson, String userId) {
        return LessonResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .orderIndex(lesson.getOrderIndex())
                .xpReward(lesson.getXpReward())
                .unlocked(isUnlocked(lesson, userId))
                .completed(isCompleted(lesson, userId))
                .build();
    }

    private boolean isCompleted(Lesson lesson, String userId) {
        List<UUID> exerciseIds = lessonExerciseRepository
                .findExerciseIdsByLessonIdOrdered(lesson.getId());
        if (exerciseIds.isEmpty()) return false;
        return exerciseIds.stream()
                .allMatch(exId -> userProgressRepository
                        .findByUserIdAndExerciseId(userId, exId)
                        .map(p -> p.getStatus() == ProgressStatus.COMPLETED)
                        .orElse(false));
    }

    // ── Admin CRUD ────────────────────────────────────────────────────────────

    @CacheEvict(value = "courses", allEntries = true)
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest req) {
        Course saved = courseRepository.save(Course.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .orderIndex(req.getOrderIndex())
                .build());
        return CourseResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .orderIndex(saved.getOrderIndex())
                .build();
    }

    @CacheEvict(value = "courses", allEntries = true)
    @Transactional
    public void deleteCourse(UUID id) {
        if (!courseRepository.existsById(id)) throw ResourceNotFoundException.of("Course", id);
        courseRepository.deleteById(id);
    }

    @Caching(evict = {
            @CacheEvict(value = "course-levels", allEntries = true),
            @CacheEvict(value = "courses", allEntries = true)
    })
    @Transactional
    public CourseLevelResponse createLevel(CreateCourseLevelRequest req) {
        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> ResourceNotFoundException.of("Course", req.getCourseId()));
        CourseLevel saved = levelRepository.save(CourseLevel.builder()
                .course(course)
                .title(req.getTitle())
                .orderIndex(req.getOrderIndex())
                .build());
        return CourseLevelResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .orderIndex(saved.getOrderIndex())
                .lessons(List.of())
                .build();
    }

    @Caching(evict = {
            @CacheEvict(value = "course-levels", allEntries = true),
            @CacheEvict(value = "courses", allEntries = true)
    })
    @Transactional
    public void deleteLevel(UUID id) {
        if (!levelRepository.existsById(id)) throw ResourceNotFoundException.of("CourseLevel", id);
        levelRepository.deleteById(id);
    }

    @CacheEvict(value = "course-levels", allEntries = true)
    @Transactional
    public LessonResponse createLesson(CreateLessonRequest req) {
        CourseLevel level = levelRepository.findById(req.getLevelId())
                .orElseThrow(() -> ResourceNotFoundException.of("CourseLevel", req.getLevelId()));
        Lesson saved = lessonRepository.save(Lesson.builder()
                .level(level)
                .title(req.getTitle())
                .orderIndex(req.getOrderIndex())
                .xpReward(req.getXpReward())
                .build());
        return LessonResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .orderIndex(saved.getOrderIndex())
                .xpReward(saved.getXpReward())
                .unlocked(true) // newly created lesson — caller decides placement
                .build();
    }

    @CacheEvict(value = "course-levels", allEntries = true)
    @Transactional
    public void deleteLesson(UUID id) {
        if (!lessonRepository.existsById(id)) throw ResourceNotFoundException.of("Lesson", id);
        lessonRepository.deleteById(id);
    }

    // ── Unlock logic ──────────────────────────────────────────────────────────

    /**
     * First lesson in a level is always unlocked.
     * "First" = no lesson with a smaller orderIndex exists in the same level.
     * Subsequent lessons require all exercises of the previous lesson to be COMPLETED.
     */
    private boolean isUnlocked(Lesson lesson, String userId) {
        // Find the lesson immediately before this one in the same level
        Optional<Lesson> prevOpt = lessonRepository
                .findFirstByLevelIdAndOrderIndexLessThanOrderByOrderIndexDesc(
                        lesson.getLevel().getId(), lesson.getOrderIndex());

        // No previous lesson → this IS the first lesson → always unlocked
        if (prevOpt.isEmpty()) return true;

        Lesson prev = prevOpt.get();
        List<UUID> exerciseIds = lessonExerciseRepository
                .findExerciseIdsByLessonIdOrdered(prev.getId());

        if (exerciseIds.isEmpty()) return true;

        long completed = exerciseIds.stream()
                .filter(exId -> userProgressRepository
                        .findByUserIdAndExerciseId(userId, exId)
                        .map(p -> p.getStatus() == ProgressStatus.COMPLETED)
                        .orElse(false))
                .count();

        return completed == exerciseIds.size();
    }
}
