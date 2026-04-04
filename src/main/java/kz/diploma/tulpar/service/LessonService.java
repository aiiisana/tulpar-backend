package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.Course;
import kz.diploma.tulpar.domain.entity.CourseLevel;
import kz.diploma.tulpar.domain.entity.Lesson;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final CourseRepository courseRepository;
    private final CourseLevelRepository levelRepository;
    private final LessonRepository lessonRepository;
    private final LessonExerciseRepository lessonExerciseRepository;
    private final UserProgressRepository userProgressRepository;
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

    @Cacheable(value = "course-levels", key = "#courseId")
    @Transactional(readOnly = true)
    public List<CourseLevelResponse> getCourseLevels(UUID courseId, String userId) {
        if (!courseRepository.existsById(courseId)) {
            throw ResourceNotFoundException.of("Course", courseId);
        }
        return levelRepository.findAllByCourseIdOrderByOrderIndexAsc(courseId).stream()
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

        List<ExerciseResponse> exercises = lessonExerciseRepository
                .findAllByLessonIdOrderByOrderIndexAsc(lessonId).stream()
                .map(le -> exerciseService.findById(le.getExercise().getId()))
                .toList();

        return LessonResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .orderIndex(lesson.getOrderIndex())
                .xpReward(lesson.getXpReward())
                .unlocked(isUnlocked(lesson, userId))
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
                .build();
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
                .unlocked(saved.getOrderIndex() == 0)
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
     * Subsequent lessons require all exercises of the previous lesson to be COMPLETED.
     */
    private boolean isUnlocked(Lesson lesson, String userId) {
        if (lesson.getOrderIndex() == 0) return true;

        return lessonRepository
                .findFirstByLevelIdAndOrderIndexLessThanOrderByOrderIndexDesc(
                        lesson.getLevel().getId(), lesson.getOrderIndex())
                .map(prev -> {
                    List<UUID> exerciseIds = lessonExerciseRepository
                            .findAllByLessonIdOrderByOrderIndexAsc(prev.getId()).stream()
                            .map(le -> le.getExercise().getId())
                            .toList();
                    if (exerciseIds.isEmpty()) return true;
                    long completed = exerciseIds.stream()
                            .filter(exId -> userProgressRepository
                                    .findByUserIdAndExerciseId(userId, exId)
                                    .map(p -> p.getStatus() == ProgressStatus.COMPLETED)
                                    .orElse(false))
                            .count();
                    return completed == exerciseIds.size();
                })
                .orElse(true);
    }
}
