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

    /**
     * Returns the lesson map for the current user's difficulty group.
     *
     * <p>Design invariant (Duolingo-style):
     * <ul>
     *   <li>Each {@link DifficultyLevel} is a <em>completely independent progression path</em>.</li>
     *   <li>A user sees <em>only</em> the lessons that belong to their selected difficulty.</li>
     *   <li>There is no cross-difficulty unlocking — ELEMENTARY does not "unlock after BEGINNER".</li>
     *   <li>If the user has not selected a difficulty yet (level == null), an empty list is returned
     *       so the frontend can prompt them to complete onboarding.</li>
     * </ul>
     *
     * <p>Root-cause note: the previous implementation returned ALL difficulty groups when no level
     * was set and used a flat index for the entire list. Because {@link #isUnlocked} searches for
     * the previous lesson <em>within the same {@link CourseLevel}</em>, the first lesson of every
     * group always had no predecessor → always appeared unlocked, producing the "Level 6, 11, 16
     * randomly unlocked" symptom.
     */
    @Transactional(readOnly = true)
    public List<CourseLevelResponse> getCourseLevels(UUID courseId, String userId) {
        if (!courseRepository.existsById(courseId)) {
            throw ResourceNotFoundException.of("Course", courseId);
        }

        User user = userRepository.findById(userId).orElse(null);
        DifficultyLevel userLevel = user != null ? user.getLevel() : null;

        // No difficulty selected → return empty. Frontend must redirect to level-selection.
        if (userLevel == null) {
            return List.of();
        }

        // Fetch the ONE CourseLevel that belongs to the user's difficulty.
        Optional<CourseLevel> levelOpt =
                levelRepository.findByCourseIdAndDifficultyLevel(courseId, userLevel);

        if (levelOpt.isEmpty()) {
            // No content seeded for this difficulty yet — return empty.
            return List.of();
        }

        CourseLevel level = levelOpt.get();
        List<LessonResponse> lessons = lessonRepository
                .findAllByLevelIdOrderByOrderIndexAsc(level.getId()).stream()
                .map(lesson -> toLessonSummary(lesson, userId))
                .toList();

        return List.of(CourseLevelResponse.builder()
                .id(level.getId())
                .title(level.getTitle())
                .orderIndex(level.getOrderIndex())
                .difficultyLevel(level.getDifficultyLevel())
                .lessons(lessons)
                .build());
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
        return hasPassedThreshold(lesson.getId(), userId);
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
                .difficultyLevel(req.getDifficultyLevel())   // must be set or level won't be visible to filtered users
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
     * Determines whether a lesson is available for the user to attempt.
     *
     * <h3>Rules (in evaluation order):</h3>
     * <ol>
     *   <li>If there is a previous lesson in the same {@link CourseLevel} (same difficulty group),
     *       the user must have completed all of its exercises first.</li>
     *   <li>If this is the first lesson in its {@link CourseLevel}, check whether there is a
     *       preceding {@link CourseLevel} in the same course (by {@code orderIndex}).  If one
     *       exists, every lesson in that preceding level must be fully completed before this
     *       level's first lesson becomes available.  This ensures sequential difficulty
     *       progression: ELEMENTARY unlocks only after all BEGINNER lessons are done, etc.</li>
     *   <li>If neither condition applies (no previous lesson, no previous level), the lesson is
     *       the absolute starting point → always unlocked.</li>
     * </ol>
     */
    private boolean isUnlocked(Lesson lesson, String userId) {
        CourseLevel currentLevel = lesson.getLevel();

        // ── Case 1: check the previous lesson WITHIN the same level ─────────
        Optional<Lesson> prevInLevelOpt = lessonRepository
                .findFirstByLevelIdAndOrderIndexLessThanOrderByOrderIndexDesc(
                        currentLevel.getId(), lesson.getOrderIndex());

        if (prevInLevelOpt.isPresent()) {
            // There IS a predecessor in the same level — user must have passed ≥90 %.
            return hasPassedThreshold(prevInLevelOpt.get().getId(), userId);
        }

        // ── Case 2: this is the FIRST lesson of its level ────────────────────
        // Check whether the previous CourseLevel (by orderIndex) has been fully completed.
        Optional<CourseLevel> prevLevelOpt = levelRepository
                .findFirstByCourseIdAndOrderIndexLessThanOrderByOrderIndexDesc(
                        currentLevel.getCourse().getId(), currentLevel.getOrderIndex());

        if (prevLevelOpt.isEmpty()) {
            // No prior level exists — this is the very first lesson in the course → unlocked.
            return true;
        }

        // Every lesson in the preceding level must be fully completed.
        List<Lesson> prevLevelLessons = lessonRepository
                .findAllByLevelIdOrderByOrderIndexAsc(prevLevelOpt.get().getId());

        if (prevLevelLessons.isEmpty()) {
            // Previous level has no lessons (not yet seeded) → treat as completed.
            return true;
        }

        return prevLevelLessons.stream()
                .allMatch(l -> hasPassedThreshold(l.getId(), userId));
    }

    /**
     * Returns {@code true} when the user has correctly answered at least 90 % of
     * the exercises in the given lesson (i.e. ≥ ⌈total × 0.9⌉ exercises are COMPLETED).
     *
     * <p>Examples:
     * <ul>
     *   <li>10 exercises → need 9 correct  (9/10 = 90 %)</li>
     *   <li>5  exercises → need 5 correct  (⌈4.5⌉ = 5)</li>
     *   <li>0  exercises → trivially true</li>
     * </ul>
     *
     * <p>This single method is used both for the "lesson completed" badge and for
     * the "next lesson unlocked" gate, so they always stay in sync.
     */
    private boolean hasPassedThreshold(UUID lessonId, String userId) {
        List<UUID> ids = lessonExerciseRepository.findExerciseIdsByLessonIdOrdered(lessonId);
        if (ids.isEmpty()) return true;

        long total    = ids.size();
        long required = (long) Math.ceil(total * 0.9);   // e.g. ⌈10*0.9⌉ = 9
        long completed = userProgressRepository
                .countByUserIdAndExerciseIdInAndStatus(userId, ids, ProgressStatus.COMPLETED);

        return completed >= required;
    }
}
