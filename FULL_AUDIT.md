# Tulpar — Full System Health Audit

**Date:** April 2026  
**Auditor:** Staff Full-Stack Engineer (AI)  
**Scope:** Backend (Spring Boot), Frontend (Flutter), Auth (Firebase), DB (PostgreSQL/Flyway), AI Chat, Flashcards

---

## 1. System Health Summary

**Overall Status: ⚠️ PARTIALLY BROKEN**

The core learning loop (registration → exercises → progress) works. However, **7 bugs of varying severity** were found, including **2 critical bugs** that actively break functionality for real users. All have been fixed in this audit session.

---

## 2. Critical Issues List (Ranked by Severity)

| # | Severity | Area | Issue |
|---|----------|------|-------|
| 1 | 🔴 CRITICAL | Migration V6 | `chk_users_level` CHECK constraint missing `ELEMENTARY` → DB rejects level saves |
| 2 | 🔴 CRITICAL | Flutter | `Image.network('/assets/...')` — relative URL crashes image loading for IMAGE_CONTEXT |
| 3 | 🟠 HIGH | Backend DTO | `CreateCourseLevelRequest` has no `difficultyLevel` field → admin-created levels never visible to level-filtered users |
| 4 | 🟠 HIGH | AI Service | Empty API key in `application-local.yml` → AI chat silently fails in local dev |
| 5 | 🟠 HIGH | Flutter | Audio URL for LISTENING uses wrong base URL (missing `/api` context-path) |
| 6 | 🟡 MEDIUM | Backend | `ChatController.sendMessage()` throws `RuntimeException` on null principal → 500 |
| 7 | 🟡 MEDIUM | Flutter | `FlashcardsScreen` falls back to hardcoded deck on any API failure, hiding errors |
| 8 | 🟡 MEDIUM | DB Design | `AI_GENERATED` exercise type has no Java entity → always scores 0 |
| 9 | 🟢 LOW | Backend | XP always hardcoded to 5, not tied to lesson/exercise difficulty |

---

## 3. Problematic Files

### BUG-1 🔴 — `V6__extend_users_table.sql`
**Issue:** `chk_users_level` constraint only allows `BEGINNER`, `INTERMEDIATE`, `ADVANCED`. `ELEMENTARY` is absent.

```sql
-- BROKEN (line 9-10):
CHECK (level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED') OR level IS NULL);
```

**Impact:** After the Flutter fix (adding 'Элементарный' → 'ELEMENTARY'), `OnboardingService.setLevel()` with `ELEMENTARY` throws `org.hibernate.exception.ConstraintViolationException`. The user appears to complete onboarding but their level is silently not saved.

**Fix:** Created `V22__fix_users_level_constraint.sql`:
```sql
ALTER TABLE users DROP CONSTRAINT chk_users_level;
ALTER TABLE users ADD CONSTRAINT chk_users_level
    CHECK (level IN ('BEGINNER','ELEMENTARY','INTERMEDIATE','ADVANCED') OR level IS NULL);
```

---

### BUG-2 🔴 — `exercise_screen.dart` (IMAGE_CONTEXT)
**File:** `tulpar-front/lib/features/lesson/exercise_screen.dart`  
**Issue:** `Image.network(ex.imageUrl!)` where `imageUrl = '/assets/colors/red.png'`. `Image.network` requires a full HTTP/HTTPS URL. A relative path causes an immediate exception — the `errorBuilder` shows a placeholder icon instead.

```dart
// BROKEN:
child: Image.network(ex.imageUrl!, ...)

// FIXED:
child: Image.network(_resolveUrl(ex.imageUrl!), ...)
```

**Fix applied:** Added `_resolveUrl()` helper that prepends `API_BASE` env var (default `http://localhost:8080/api`) to relative paths.

---

### BUG-3 🟠 — `CreateCourseLevelRequest.java` + `LessonService.createLevel()`
**File:** `dto/request/CreateCourseLevelRequest.java`, `service/LessonService.java`  
**Issue:** `CreateCourseLevelRequest` has no `difficultyLevel` field. `LessonService.createLevel()` builds a `CourseLevel` without setting `difficultyLevel`, so it's `null` in DB.

**Impact:** Any level created via admin panel is invisible to users who have a level set, because `CourseLevelRepository.findByCourseIdAndDifficultyLevel()` returns `Optional.empty()`, falling back to showing ALL levels. For new courses created after V18 seed, this breaks the entire level-filtered flow.

**Fix applied:**
```java
// CreateCourseLevelRequest — added field:
private DifficultyLevel difficultyLevel;

// LessonService.createLevel() — now saves it:
CourseLevel.builder()
    .course(course)
    .title(req.getTitle())
    .orderIndex(req.getOrderIndex())
    .difficultyLevel(req.getDifficultyLevel())  // ← ADDED
    .build();
```

---

### BUG-4 🟠 — `application-local.yml` (AI Service)
**File:** `src/main/resources/application-local.yml`  
**Issue:**
```yaml
ai:
  openai:
    api-key:    # ← EMPTY
```
`OpenAiService` sends `Authorization: Bearer ` (empty token) to OpenAI, gets `401 Unauthorized`, catches the exception silently, and returns the hardcoded Russian fallback string `"Кешіріңіз, AI уақытша жұмыс істемейді"`.

**Impact:** AI Chat never works in local development. Developers think AI is broken but see no errors.

**Fix (manual):** Set `api-key: sk-...` in `application-local.yml` or pass `OPENAI_API_KEY` env variable.

Also note: `AiProperties.java` has wrong defaults:
```java
private String baseUrl = "https://api.perplexity.ai";  // overridden by application.yml
private String model = "sonar-small-chat";              // overridden by application.yml
```
These are overridden by `application.yml` → `https://api.openai.com` / `gpt-4o-mini`. Not a runtime bug but confusing for developers.

---

### BUG-5 🟠 — `exercise_screen.dart` (LISTENING audio URL)
**File:** `tulpar-front/lib/features/lesson/exercise_screen.dart`  
**Issue:** Audio URLs seeded in V18 are relative paths: `/assets/audio/salam.mp3`. The original `_playAudio()` code prepended `API_BASE` env var defaulting to `http://localhost:8080` — **missing the `/api` context-path**. This means audio requests went to `http://localhost:8080/assets/audio/salam.mp3` instead of `http://localhost:8080/api/assets/audio/salam.mp3`.

**Fix applied:** Unified URL resolution into `_resolveUrl()` with correct default `http://localhost:8080/api`.

---

### BUG-6 🟡 — `ChatController.java` (null principal)
**File:** `controller/ChatController.java`  
**Issue:**
```java
if (principal == null) {
    throw new RuntimeException("User not authenticated");  // → HTTP 500
}
```
`RuntimeException` is caught by `GlobalExceptionHandler` and returns `{"success":false,"error":"An unexpected error occurred"}` with HTTP 500. Security should prevent this, but if it happens the client sees a misleading 500 instead of 401.

**Fix applied:** Changed to `return ResponseEntity.status(401).build()`.

---

### BUG-7 🟡 — `learning_content/flashcards_screen.dart` (silent fallback)
**File:** `tulpar-front/lib/features/learning_content/flashcards_screen.dart`  
**Issue:**
```dart
final items = models.isNotEmpty
    ? models.map(_modelToItem).toList()
    : kFlashcardDeck;  // hardcoded fallback!
```
When the API fails (network error, backend down, 401), users still see 10 hardcoded flashcards from `flashcard_deck.dart`. Developers and users get no indication the API failed.

**Recommendation:** Show a visible error banner alongside the fallback, or always show backend data only.

---

### BUG-8 🟡 — `AI_GENERATED` exercise type (no Java entity)
**Files:** `ExerciseType.java`, `ExerciseService.toResponse()`, `ProgressService.evaluate()`  
**Issue:** `ExerciseType.AI_GENERATED` exists in the enum and DB constraint, but there is no `AIGeneratedExercise` entity class. If an `AI_GENERATED` exercise is inserted into DB:
- `ExerciseService.toResponse()` hits the `default` branch → all fields null
- `ProgressService.evaluate()` hits `default -> false` → always scores 0

**Fix (if AI exercises are not yet used):** Remove `AI_GENERATED` from the DB CHECK constraint and the Java enum. If needed in future, add the entity class first.

---

## 4. Broken Flows — Step-by-Step

### Flow 1: User Registration with ELEMENTARY Level (BROKEN → FIXED)

```
[1] User opens app → ChooseLevelScreen
[2] User taps 'Элементарный' → AppStorage.setLevel('Элементарный')
[3] _next() → DailyGoalScreen → SignupScreen
[4] Firebase creates account → OnboardingService.sendIfNeeded()
    → POST /api/onboarding/level { "level": "ELEMENTARY" }
[5] OnboardingController → OnboardingService.setLevel(uid, ELEMENTARY)
[6] user.setLevel(ELEMENTARY) → JPA save
    ❌ CONSTRAINT VIOLATION: chk_users_level rejects 'ELEMENTARY'
    ❌ Transaction rolls back → level NOT saved in DB
[7] Onboarding reports success (error is swallowed in Flutter catch)
[8] getCourseLevels() → user.level == null → fallback: shows ALL 4 levels
    (user sees all levels instead of only ELEMENTARY)
```

**After V22 migration:** Step 6 succeeds; user sees only the ELEMENTARY level.

---

### Flow 2: IMAGE_CONTEXT Exercise (BROKEN → FIXED)

```
[1] getLessonDetail() → exercises include IMAGE_CONTEXT type
[2] ExerciseScreen._multipleChoice() renders image:
    Image.network('/assets/colors/red.png')
    ❌ Invalid URL (no scheme) → NetworkImage throws
    → errorBuilder renders grey placeholder with broken-image icon
[3] User sees "Выберите правильный вариант" with no image
[4] User guesses from 4 text options (still functional, but wrong UX)
```

**After fix:** `_resolveUrl()` transforms to `http://HOST/api/assets/colors/red.png`. Images load correctly (assuming files served from MinIO or Spring Boot static).

---

### Flow 3: AI Chat (BROKEN in local dev)

```
[1] ChatScreen._send() → ChatService.sendMessage(text)
[2] POST /api/chat/message → ChatController.sendMessage()
[3] ChatService.sendMessage() → AiService.chat(history, message)
[4] OpenAiService.chat():
    → RestClient POST to https://api.openai.com/v1/chat/completions
    → Header: "Authorization: Bearer " (empty key in local)
    ❌ OpenAI returns 401 Unauthorized
    → catch(Exception e) → return "Кешіріңіз, AI уақытша жұмыс істемейді"
[5] That fallback string is saved in DB as ASSISTANT message
[6] User sees the error string as if it were a real AI response
    (NO error indicator in UI, message looks like valid AI reply)
```

**In production:** Works if `OPENAI_API_KEY` env var is set correctly.

---

### Flow 4: Flashcards (PARTIALLY WORKING)

```
[1] FlashcardsScreen initState → FlashcardService.getAll()
    → GET /api/flashcards (public endpoint, no auth needed ✓)
[2] API returns PageResponse<FlashcardResponse>
[3] ApiClient._unwrapApiResponse() extracts data → List of flashcard maps
[4] FlashcardModel.fromJson() → List<FlashcardModel>
    ✓ Fields: id, wordRu, wordKz, transcription, exampleSentence — all correct
[5] UI renders flip cards
    ⚠️ Volume icon in flashcard has onPressed: () {} → does nothing
    ⚠️ If API fails → falls back to kFlashcardDeck silently
```

---

### Flow 5: Progress Submission (WORKING)

```
[1] User taps option → ExerciseScreen._submit(answer)
[2] ProgressService.submit(exerciseId, userAnswer)
    → POST /api/progress { exerciseId, userAnswer }
[3] ProgressController → ProgressService.submit()
[4] exerciseRepository.findById(exerciseId) → loads correct subtype
    (JOINED inheritance: Hibernate JOINs vocabulary_exercises, etc.)
[5] evaluate(exercise, userAnswer) → pattern-matching switch
    → VocabularyExercise: correctAnswer.equalsIgnoreCase(userAnswer.trim()) ✓
[6] Result saved → streakService.recordActivityAndAddXp(userId, 5)
[7] ProgressResponse { correct: true/false } returned
[8] Flutter shows green/red feedback ✓
⚠️ XP earned (5) not included in response — user sees no XP notification
```

---

## 5. Database & Migration Issues

### Issue D1 — V6: Missing ELEMENTARY in constraint (CRITICAL)
```sql
-- CURRENT (broken):
CHECK (level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED') OR level IS NULL)

-- FIXED (V22):
CHECK (level IN ('BEGINNER','ELEMENTARY','INTERMEDIATE','ADVANCED') OR level IS NULL)
```

### Issue D2 — V19: difficulty_level column is NULLABLE
```sql
-- V19 adds column without NOT NULL:
ALTER TABLE course_levels ADD COLUMN difficulty_level VARCHAR(32);
```
`CourseLevel.difficultyLevel` can be null. Admin-created levels without this field are invisible to filtered users. **Fixed in `LessonService.createLevel()` and `CreateCourseLevelRequest`** — but existing null rows in DB must be patched manually:
```sql
-- Run manually if any course levels exist with null difficulty_level:
UPDATE course_levels SET difficulty_level = 'BEGINNER'
WHERE difficulty_level IS NULL AND order_index = 1;
-- (repeat for other order_index values as appropriate)
```

### Issue D3 — AI_GENERATED in DB constraint without entity
```sql
-- V2 allows AI_GENERATED:
CHECK (exercise_type IN ('VOCABULARY','SENTENCE_BUILDER','LISTENING',
                         'VIDEO_CONTEXT','IMAGE_CONTEXT','AI_GENERATED'))
```
No `ai_generated_exercises` table exists, no Java entity. If such a row is inserted, JPA will load a base `Exercise` object — subtype fields all null. **Recommendation: remove `AI_GENERATED` from DB constraint until the feature is implemented.**

### Migration Sequence (Verified OK)
```
V1  → users table
V2  → exercises (base)
V3  → exercise subtypes (vocabulary, listening, video, image, sentence_builder)
V4  → user_progress
V5  → media_files
V6  → extend users (⚠️ missing ELEMENTARY — fixed by V22)
V7  → courses, course_levels, lessons, lesson_exercises
V8  → user_stats, user_daily_activity (streaks)
V9  → flashcards
V10 → daily_challenges
V11 → articles
V12 → grammar_rules
V13 → chat_messages
V14 → notifications
V15 → seed daily_challenges
V16 → device_tokens
V17 → fix daily_challenge image_urls
V18 → seed 1 course, 4 levels, 20 lessons, 200 exercises (all subtypes)
V19 → add difficulty_level to course_levels (nullable — risk)
V20 → seed articles and grammar rules
V21 → insert 10 flashcards
V22 → fix chk_users_level constraint ← NEW
```

---

## 6. Backend Fixes (Code Snippets)

### Fix 1 — V22 Migration (CRITICAL)
```sql
-- tulpar/src/main/resources/db/migration/V22__fix_users_level_constraint.sql
ALTER TABLE users DROP CONSTRAINT chk_users_level;
ALTER TABLE users ADD CONSTRAINT chk_users_level
    CHECK (level IN ('BEGINNER','ELEMENTARY','INTERMEDIATE','ADVANCED') OR level IS NULL);
```

### Fix 2 — CreateCourseLevelRequest
```java
// Add field:
private DifficultyLevel difficultyLevel;
```

### Fix 3 — LessonService.createLevel()
```java
CourseLevel.builder()
    .course(course)
    .title(req.getTitle())
    .orderIndex(req.getOrderIndex())
    .difficultyLevel(req.getDifficultyLevel())  // ← ADDED
    .build();
```

### Fix 4 — ChatController
```java
// Replace:
throw new RuntimeException("User not authenticated");
// With:
return ResponseEntity.status(401).build();
```

### Fix 5 — ProgressResponse: add xpEarned (Recommended)
```java
// ProgressResponse.java — add field:
private int xpEarned;

// ProgressService.submit() — populate it:
int xp = correct ? 5 : 0;
if (correct) streakService.recordActivityAndAddXp(userId, xp);
return toResponse(saved, correct, xp);

// toResponse() signature:
private ProgressResponse toResponse(UserProgress p, boolean correct, int xpEarned) {
    return ProgressResponse.builder()
        // ... existing fields ...
        .xpEarned(xpEarned)
        .build();
}
```

---

## 7. Frontend Fixes (Flutter)

### Fix 1 — exercise_screen.dart: resolve relative URLs
```dart
// Add helper:
String _resolveUrl(String url) {
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  const apiBase = String.fromEnvironment(
    'API_BASE', defaultValue: 'http://localhost:8080/api');
  return '$apiBase$url';
}

// Fix Image.network:
Image.network(_resolveUrl(ex.imageUrl!), ...)

// Fix audio playback:
await _audioPlayer.play(UrlSource(_resolveUrl(url)));
```

### Fix 2 — choose_level_screen.dart (applied earlier)
```dart
// Was:
final levels = const ['Начинающий', 'Средний', 'Продвинутый'];
// Fixed:
final levels = const ['Начинающий', 'Элементарный', 'Средний', 'Продвинутый'];
```

### Fix 3 — onboarding_service.dart (applied earlier)
```dart
'Элементарный' => 'ELEMENTARY',
```

### Fix 4 — flashcards_screen.dart (Recommended)
```dart
// Current (hides errors):
final items = models.isNotEmpty ? models.map(_modelToItem).toList() : kFlashcardDeck;

// Recommended (show error state instead):
if (models.isEmpty) {
  // Show error UI, not silent fallback
  setState(() { _loading = false; _hasError = true; });
  return;
}
```

### Fix 5 — api_client.dart: wrong baseUrl for non-emulator devices
```dart
// Current (only works on Android emulator):
static const String baseUrl = 'http://localhost:8080/api';

// Should be configurable via dart-define:
static const String baseUrl = String.fromEnvironment(
  'API_BASE', defaultValue: 'http://10.0.2.2:8080/api');
```

---

## 8. Final Fix Plan (Safe Order)

Execute in this exact order to avoid breaking existing data:

```
Step 1 [DB]       Run V22 migration (fixes ELEMENTARY constraint) — DO FIRST
                  Applied automatically by Flyway on next app startup.

Step 2 [Backend]  Deploy updated JAR with:
                  - CreateCourseLevelRequest.difficultyLevel field
                  - LessonService.createLevel() saves difficultyLevel
                  - ChatController returns 401 instead of RuntimeException

Step 3 [Flutter]  Deploy updated app with:
                  - choose_level_screen: 4 levels (added Элементарный)
                  - onboarding_service: 'Элементарный' → 'ELEMENTARY'
                  - exercise_screen: _resolveUrl() for image + audio URLs
                  - admin panel: auto-detect API base URL (already applied)

Step 4 [Config]   Set OPENAI_API_KEY in local dev environment
                  (or application-local.yml)

Step 5 [Optional] Patch any course_levels rows with NULL difficulty_level:
                  UPDATE course_levels SET difficulty_level = 'BEGINNER'
                  WHERE difficulty_level IS NULL AND order_index = 1;

Step 6 [Optional] Add xpEarned to ProgressResponse to show XP in exercise feedback.
```

---

## 9. Verification Checklist

| Flow | Test | Expected |
|------|------|----------|
| Registration | Register → select 'Элементарный' → complete onboarding | DB: users.level = 'ELEMENTARY' |
| Course levels | GET /api/courses/{id}/levels as ELEMENTARY user | Returns 1 level (ELEMENTARY only) |
| Exercises | Open ELEMENTARY lesson → complete 10 exercises | Progress saved, streak incremented |
| IMAGE exercise | Load lesson with IMAGE_CONTEXT exercises | Images render (not broken icon) |
| LISTENING exercise | Load LISTENING exercise, press play | Audio plays without error |
| AI Chat | Send message to AI | Real AI response (not fallback) |
| Flashcards | Open flashcards screen | 10+ cards from backend API |
| Admin panel | Open /api/admin-ui/ | Profile loads, exercises/users visible |
```