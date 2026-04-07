# Tulpar — Полная документация проекта

> Мобильное приложение для изучения казахского языка (Flutter + Spring Boot)

---

## Содержание

1. [Обзор проекта](#1-обзор-проекта)
2. [Технологический стек](#2-технологический-стек)
3. [Архитектура системы](#3-архитектура-системы)
4. [Бэкенд — подробное описание](#4-бэкенд--подробное-описание)
   - 4.1 [Структура модулей](#41-структура-модулей)
   - 4.2 [База данных — сущности и схема](#42-база-данных--сущности-и-схема)
   - 4.3 [REST API — все эндпоинты](#43-rest-api--все-эндпоинты)
   - 4.4 [Сервисный слой](#44-сервисный-слой)
   - 4.5 [Безопасность и аутентификация](#45-безопасность-и-аутентификация)
   - 4.6 [Конфигурация инфраструктуры](#46-конфигурация-инфраструктуры)
5. [Фронтенд — подробное описание](#5-фронтенд--подробное-описание)
   - 5.1 [Структура проекта](#51-структура-проекта)
   - 5.2 [Навигация и архитектура экранов](#52-навигация-и-архитектура-экранов)
   - 5.3 [HTTP-клиент и сервисный слой](#53-http-клиент-и-сервисный-слой)
   - 5.4 [Локальное хранилище](#54-локальное-хранилище)
   - 5.5 [Описание каждого экрана](#55-описание-каждого-экрана)
   - 5.6 [Локализация](#56-локализация)
   - 5.7 [Тема и дизайн](#57-тема-и-дизайн)
6. [Карта интеграции фронт → бэк](#6-карта-интеграции-фронт--бэк)
7. [Что сделано с нуля](#7-что-сделано-с-нуля)
8. [Исправленные баги при интеграции](#8-исправленные-баги-при-интеграции)
9. [Что ещё не реализовано](#9-что-ещё-не-реализовано)
10. [Рекомендации по дальнейшей разработке](#10-рекомендации-по-дальнейшей-разработке)

---

## 1. Обзор проекта

**Tulpar** — это мобильное приложение для изучения казахского языка, разработанное в стиле Duolingo. Проект состоит из двух частей:

- **`tulpar/`** — серверная часть на Spring Boot 3.x / Java 21
- **`tulpar-front/`** — мобильное приложение на Flutter (Dart), целевые платформы: Android и iOS

Приложение позволяет:
- Проходить уроки с упражнениями (словарный запас, конструктор предложений, аудирование, изображения, видео)
- Отслеживать прогресс обучения: стрик, XP, разблокированные уроки
- Использовать ИИ-ассистента для разговорной практики (OpenAI GPT)
- Читать статьи и работать с карточками (флэшкарты)
- Изучать грамматику казахского языка
- Выполнять ежедневные задания (4 картинки — 1 слово)
- Управлять профилем, настройками, безопасностью (PIN/биометрия)

---

## 2. Технологический стек

### Бэкенд

| Компонент | Технология | Версия |
|---|---|---|
| Язык | Java | 21 |
| Фреймворк | Spring Boot | 3.x |
| База данных | PostgreSQL | 15+ |
| Миграции БД | Flyway | — |
| ORM | Spring Data JPA / Hibernate | — |
| Кэш | Redis | 7+ |
| Объектное хранилище | MinIO (S3-совместимый) | — |
| Аутентификация | Firebase Admin SDK | — |
| ИИ | OpenAI API (GPT) | — |
| HTTP-клиент (ИИ) | Spring RestClient | — |
| Документация API | SpringDoc OpenAPI (Swagger UI) | — |
| Сборка | Gradle | — |

### Фронтенд

| Компонент | Технология | Версия |
|---|---|---|
| Язык | Dart | 3.x |
| Фреймворк | Flutter | 3.x |
| HTTP-клиент | Dio | ^5.4.0 |
| Аутентификация | Firebase Auth | ^4.17.0 |
| Firebase Core | firebase_core | ^2.25.0 |
| Google Sign-In | google_sign_in | ^6.2.1 |
| Apple Sign-In | sign_in_with_apple | ^6.1.0 |
| Локальное хранилище | shared_preferences | ^2.2.3 |
| Безопасное хранилище | flutter_secure_storage | ^9.2.2 |
| Биометрия | local_auth | ^2.2.0 |
| Шрифты | google_fonts | ^6.2.1 |
| Хэширование | crypto | ^3.0.3 |
| Ссылки | url_launcher | ^6.2.6 |

---

## 3. Архитектура системы

```
┌──────────────────────────────────────────────────────────────┐
│                     МОБИЛЬНОЕ ПРИЛОЖЕНИЕ                      │
│                     Flutter (Dart)                            │
│                                                              │
│  ┌─────────┐  ┌──────────┐  ┌──────────┐  ┌─────────────┐  │
│  │ Screens │  │ Services │  │ AppStorage│  │ ApiClient   │  │
│  │ (UI)    │→ │ (HTTP)   │→ │(SharedPref│  │(Dio + Token)│  │
│  └─────────┘  └──────────┘  └──────────┘  └──────┬──────┘  │
└──────────────────────────────────────────────────┼──────────┘
                                                   │
                              HTTPS / Bearer JWT   │
                                                   ▼
┌──────────────────────────────────────────────────────────────┐
│                     SPRING BOOT BACKEND                       │
│                                                              │
│  ┌──────────────┐    ┌─────────────┐    ┌────────────────┐  │
│  │ REST         │    │ Service     │    │ Repository     │  │
│  │ Controllers  │ →  │ Layer       │ →  │ (Spring Data)  │  │
│  └──────────────┘    └─────┬───────┘    └───────┬────────┘  │
│                            │                    │           │
│                     ┌──────┼──────┐             │           │
│                     ▼      ▼      ▼             ▼           │
│                  Redis  MinIO  OpenAI        PostgreSQL      │
│                  Cache  Files   API          Database        │
└──────────────────────────────────────────────────────────────┘
                            │
                      Firebase Auth
                    (JWT verification)
```

---

## 4. Бэкенд — подробное описание

### 4.1 Структура модулей

```
tulpar/
├── config/
│   ├── SecurityConfig.java          # Spring Security, правила доступа
│   ├── FirebaseConfig.java          # Инициализация Firebase Admin SDK
│   ├── RedisConfig.java             # CacheManager с TTL 10 минут
│   ├── MinioConfig.java             # MinIO bucket configuration
│   └── ApiResponseAdvice.java       # Глобальная обёртка ответов
│
├── security/
│   └── FirebaseTokenFilter.java     # JWT → UserPrincipal filter
│
├── domain/
│   ├── entity/                      # JPA-сущности
│   │   ├── User.java
│   │   ├── Course.java
│   │   ├── CourseLevel.java
│   │   ├── Lesson.java
│   │   ├── LessonExercise.java
│   │   ├── Exercise.java (базовый)
│   │   ├── VocabularyExercise.java
│   │   ├── SentenceBuilderExercise.java
│   │   ├── ListeningExercise.java
│   │   ├── ImageExercise.java
│   │   ├── VideoExercise.java
│   │   ├── UserProgress.java
│   │   ├── UserStats.java
│   │   ├── UserDailyActivity.java
│   │   ├── ChatMessage.java
│   │   ├── Article.java
│   │   ├── Flashcard.java
│   │   ├── GrammarRule.java
│   │   ├── DailyChallenge.java
│   │   ├── Notification.java
│   │   └── MediaFile.java
│   │
│   └── enums/
│       ├── DifficultyLevel.java     # BEGINNER/ELEMENTARY/INTERMEDIATE/ADVANCED
│       ├── DailyGoal.java           # CASUAL/REGULAR/SERIOUS/INTENSE
│       ├── ExerciseType.java        # VOCABULARY/SENTENCE_BUILDER/LISTENING/...
│       ├── MessageRole.java         # USER/ASSISTANT
│       ├── ProgressStatus.java      # IN_PROGRESS/COMPLETED/FAILED
│       └── UserRole.java            # USER/ADMIN
│
├── repository/                      # Spring Data JPA Repositories
│
├── service/                         # Бизнес-логика
│   ├── ChatService.java
│   ├── StreakService.java
│   ├── LessonService.java
│   ├── ProgressService.java
│   ├── ProfileService.java
│   ├── OnboardingService.java
│   ├── FlashcardService.java
│   ├── ArticleService.java
│   ├── GrammarService.java
│   ├── DailyChallengeService.java
│   ├── NotificationService.java
│   ├── SettingsService.java
│   ├── MinioService.java
│   └── OpenAiService.java (impl AiService)
│
├── controller/
│   ├── OnboardingController.java
│   ├── StatsController.java
│   ├── LessonController.java
│   ├── ExerciseController.java
│   ├── ProgressController.java
│   ├── FlashcardController.java
│   ├── ArticleController.java
│   ├── ChatController.java
│   ├── GrammarController.java
│   ├── DailyChallengeController.java
│   ├── ProfileController.java
│   ├── SettingsController.java
│   ├── NotificationController.java
│   └── admin/
│       ├── AdminArticleController.java
│       ├── AdminCourseController.java
│       ├── AdminExerciseController.java
│       ├── AdminFlashcardController.java
│       ├── AdminGrammarController.java
│       ├── AdminMediaController.java
│       └── AdminUserController.java
│
├── dto/
│   ├── request/                     # Request DTOs
│   └── response/
│       └── PageResponse.java        # Универсальный пагинированный ответ
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── ForbiddenException.java
│
└── converter/
    └── StringListConverter.java     # JSON массив ↔ List<String> в колонке БД
```

---

### 4.2 База данных — сущности и схема

#### User
```sql
users (
  id              VARCHAR(128) PRIMARY KEY,  -- Firebase UID
  email           VARCHAR(255) UNIQUE NOT NULL,
  username        VARCHAR(100),
  avatar_url      TEXT,
  role            VARCHAR(20) DEFAULT 'USER',
  level           VARCHAR(20),               -- DifficultyLevel enum
  daily_goal      VARCHAR(20),               -- DailyGoal enum
  notifications_enabled BOOLEAN DEFAULT TRUE,
  onboarding_completed  BOOLEAN DEFAULT FALSE,
  created_at      TIMESTAMP
)
```

#### Контент уроков (иерархия: Course → Level → Lesson → Exercise)
```sql
courses (id UUID PK, title, description, icon_url, order_index)
course_levels (id UUID PK, course_id FK, title, order_index)
lessons (id UUID PK, level_id FK, title, order_index, xp_reward INT)
exercises (id UUID PK, type VARCHAR(30), question TEXT, ...)
  -- Наследование через JOINED table strategy:
vocabulary_exercises (exercise_id FK, word, translation, image_url, options JSONB)
sentence_builder_exercises (exercise_id FK, correct_sentence, shuffled_words JSONB)
listening_exercises (exercise_id FK, audio_url, transcript, correct_answer)
image_exercises (exercise_id FK, image_url, correct_answer, options JSONB)
video_exercises (exercise_id FK, video_url, transcript, correct_answer)
lesson_exercises (lesson_id FK, exercise_id FK, order_index)  -- many-to-many
```

#### Прогресс пользователей
```sql
user_progress (
  id UUID PK,
  user_id VARCHAR(128) FK users(id),
  exercise_id UUID FK exercises(id),
  UNIQUE(user_id, exercise_id),
  attempts INT DEFAULT 0,
  status VARCHAR(20),             -- IN_PROGRESS/COMPLETED/FAILED
  completed_at TIMESTAMP
)

user_stats (
  user_id VARCHAR(128) PK FK users(id),
  current_streak   INT DEFAULT 0,
  longest_streak   INT DEFAULT 0,
  total_xp         INT DEFAULT 0,
  last_activity_date DATE
)

user_daily_activity (
  id UUID PK,
  user_id VARCHAR(128) FK,
  activity_date DATE NOT NULL,
  UNIQUE(user_id, activity_date)
)
```

#### Контент приложения
```sql
articles (id UUID PK, title, content TEXT, difficulty_level, created_at TIMESTAMP)
flashcards (id UUID PK, word_ru, word_kz, transcription, example_sentence)
grammar_rules (id UUID PK, title, explanation TEXT, examples TEXT)  -- examples как JSON
daily_challenges (
  id UUID PK,
  challenge_date DATE UNIQUE NOT NULL,
  letters TEXT,     -- JSON массив букв ["Қ","А","Л","А","М"]
  image_urls TEXT,  -- JSON массив URL изображений
  correct_word VARCHAR(100)  -- НЕ отправляется клиенту
)
chat_messages (
  id UUID PK,
  user_id VARCHAR(128) FK,
  role VARCHAR(20),           -- USER/ASSISTANT
  content TEXT NOT NULL,
  created_at TIMESTAMP
)
notifications (
  id UUID PK,
  user_id VARCHAR(128) FK,
  title VARCHAR(255),
  body TEXT,
  read BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP
)
media_files (id UUID PK, filename, bucket, content_type, size BIGINT, url TEXT)
```

#### Общая характеристика схемы
- Flyway управляет миграциями — каждое изменение схемы в виде версионированных SQL-файлов (`V1__init.sql`, `V2__...`, и т.д.)
- Hibernate в режиме `validate` — не изменяет схему автоматически, только проверяет соответствие
- Большинство ID — `UUID` генерируемые базой; исключение — `User.id` = Firebase UID (строка)
- Массивы хранятся через `StringListConverter` — конвертирует `List<String>` ↔ JSON-строка в одной колонке

---

### 4.3 REST API — все эндпоинты

Все эндпоинты имеют префикс `/api`. Все требуют заголовка `Authorization: Bearer <Firebase JWT>`.

#### Онбординг (`/api/onboarding`)

| Метод | Путь | Тело | Описание |
|---|---|---|---|
| POST | `/onboarding/level` | `{"level": "BEGINNER"}` | Установить уровень сложности |
| POST | `/onboarding/goal` | `{"dailyGoal": "REGULAR"}` | Установить ежедневную цель |
| POST | `/onboarding/complete` | — | Завершить онбординг |

#### Статистика (`/api/stats`)

| Метод | Путь | Описание | Ответ |
|---|---|---|---|
| GET | `/stats` | Текущий стрик, рекорд, XP | `{currentStreak, longestStreak, totalXp}` |
| GET | `/stats/calendar` | Активность за 7 дней | `[{date: "2026-04-06", completed: true}, ...]` |

#### Уроки (`/api/courses`, `/api/lessons`)

| Метод | Путь | Описание | Ответ |
|---|---|---|---|
| GET | `/courses` | Все курсы | `[{id, title, description, orderIndex}]` |
| GET | `/courses/{id}/levels` | Уровни курса с уроками и статусом разблокировки | `[{id, title, lessons: [{..., unlocked}]}]` |
| GET | `/lessons/{id}` | Детали урока со всеми упражнениями | `{id, title, xpReward, exercises: [...]}` |

#### Упражнения (`/api/exercises`)

| Метод | Путь | Параметры | Описание |
|---|---|---|---|
| GET | `/exercises` | `page, size, type` | Список упражнений (PageResponse) |
| GET | `/exercises/{id}` | — | Одно упражнение по ID |

#### Прогресс (`/api/progress`)

| Метод | Путь | Тело | Описание |
|---|---|---|---|
| POST | `/progress` | `{exerciseId, userAnswer}` | Сдать ответ (валидация на сервере) |
| GET | `/progress` | `exerciseId?` (опц.) | История прогресса пользователя |
| GET | `/progress/summary` | — | Сводка: завершено / всего / % |

Ответ POST `/progress`:
```json
{
  "progressId": "uuid",
  "exerciseId": "uuid",
  "correct": true,
  "status": "COMPLETED",
  "attempts": 1
}
```

#### Флэшкарты (`/api/flashcards`)

| Метод | Путь | Параметры | Ответ |
|---|---|---|---|
| GET | `/flashcards` | `page=0&size=50` | PageResponse с полями `wordRu, wordKz, transcription, exampleSentence` |

#### Статьи (`/api/articles`)

| Метод | Путь | Параметры | Ответ |
|---|---|---|---|
| GET | `/articles` | `page, size, level?` | PageResponse статей |
| GET | `/articles/{id}` | — | Полная статья |

#### Чат с ИИ (`/api/chat`)

| Метод | Путь | Тело | Описание |
|---|---|---|---|
| POST | `/chat/message` | `{"message": "Сәлем!"}` | Отправить сообщение ИИ-тьютору |
| GET | `/chat/history` | `page, size` | История переписки (PageResponse) |

#### Грамматика (`/api/grammar`)

| Метод | Путь | Описание |
|---|---|---|
| GET | `/grammar` | Список всех правил `[{id, title, explanation, examples}]` |
| GET | `/grammar/{id}` | Одно правило |

#### Ежедневное задание (`/api/daily-challenge`)

| Метод | Путь | Описание |
|---|---|---|
| GET | `/daily-challenge` | Задание на сегодня: `{letters, imageUrls}` (без `correctWord`) |

#### Профиль (`/api/profile`)

| Метод | Путь | Тело | Описание |
|---|---|---|---|
| GET | `/profile` | — | `{id, email, username, avatarUrl, currentStreak, totalXp, ...}` |
| PUT | `/profile` | `{username?, avatarUrl?, notificationsEnabled?}` | Обновить профиль |

#### Настройки (`/api/settings`)

| Метод | Путь | Описание |
|---|---|---|
| GET | `/settings` | `{supportEmail, termsUrl, privacyPolicyUrl, appVersion}` |
| GET | `/conversation-club` | `{status: "coming_soon"}` |

#### Уведомления (`/api/notifications`)

| Метод | Путь | Описание |
|---|---|---|
| GET | `/notifications` | Список уведомлений пользователя |
| PATCH | `/notifications/{id}/read` | Отметить уведомление как прочитанное |

#### Админские эндпоинты (`/api/admin`) — только роль ADMIN

| Контроллер | Методы |
|---|---|
| AdminArticleController | POST `/admin/articles`, DELETE `/admin/articles/{id}` |
| AdminCourseController | POST/DELETE `/admin/courses`, POST/DELETE `/admin/levels`, POST/DELETE `/admin/lessons` |
| AdminExerciseController | POST, PUT, DELETE `/admin/exercises` |
| AdminFlashcardController | POST, DELETE `/admin/flashcards` |
| AdminGrammarController | POST, DELETE `/admin/grammar` |
| AdminMediaController | POST `/admin/media/upload` (multipart/form-data) |
| AdminUserController | GET `/admin/users`, PATCH `/admin/users/{id}/role` |

#### Формат ответов

Все ответы оборачиваются `ApiResponseAdvice` в конверт:
```json
{
  "success": true,
  "data": { ... },
  "message": null
}
```

Ошибки:
```json
{
  "success": false,
  "data": null,
  "message": "Resource not found"
}
```

HTTP-статусы: 200 OK, 201 Created, 400 Bad Request (валидация), 401 Unauthorized, 403 Forbidden, 404 Not Found, 500 Internal Server Error.

---

### 4.4 Сервисный слой

#### ChatService
```
sendMessage(userId, message):
  1. Сохранить UserMessage в БД
  2. Загрузить последние 50 сообщений для контекста
  3. Вызвать OpenAiService.chat(messages)
  4. Сохранить ответ ASSISTANT в БД
  5. Вернуть ChatMessageResponse

getHistory(userId, page, size):
  → ChatMessageRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
  → PageResponse.from(page)
```

#### OpenAiService (реализует AiService)
```
chat(messages):
  Системный промпт:
    "You are a Kazakh language tutor. Help the student practice Kazakh.
     Always respond in the language the student is using (KZ/RU/EN).
     Correct grammar errors kindly. Encourage progress."

  POST https://api.openai.com/v1/chat/completions
    model: gpt-4o-mini (из application.yml)
    messages: [system, ...history (max 50), userMessage]

  При ошибке → "Кешіріңіз, қазіргі уақытта жауап бере алмаймын."
```

#### StreakService
```
getStats(userId):
  → UserStatsRepository.findByUserId(userId)
  → {currentStreak, longestStreak, totalXp}

getWeeklyCalendar(userId):
  Генерирует даты за последние 7 дней
  Для каждой даты проверяет UserDailyActivity.existsByUserIdAndActivityDate()
  → [{date, completed}, ...]

recordActivity(userId):
  Загрузить UserStats
  Если last_activity_date != сегодня:
    Если last_activity_date == вчера → current_streak++
    Иначе → current_streak = 1
  longest_streak = max(longest_streak, current_streak)
  Сохранить UserDailyActivity для сегодня (ON CONFLICT DO NOTHING)
  Добавить XP (например, +10 за правильный ответ)
```

#### LessonService
```
getCourses():
  @Cacheable("courses")
  → CourseRepository.findAllByOrderByOrderIndexAsc()

getCourseLevels(courseId, userId):
  @Cacheable("course-levels")
  Загрузить все уровни курса с уроками
  Для каждого урока → isUnlocked(lesson, userId)

isUnlocked(lesson, userId):
  Если orderIndex == 0 → true (первый урок всегда открыт)
  Иначе: загрузить предыдущий урок
  Проверить: все упражнения предыдущего урока имеют UserProgress.status == COMPLETED
  → boolean

getLessonDetail(lessonId, userId):
  Загрузить урок + упражнения через LessonExercise JOIN
  Отсортировать по order_index
  → LessonDetailResponse
```

#### ProgressService
```
submit(userId, {exerciseId, userAnswer}):
  1. Загрузить упражнение по ID
  2. Найти или создать UserProgress(userId, exerciseId)
  3. progress.attempts++
  4. Проверить ответ в зависимости от типа:
     - VOCABULARY: userAnswer.equals(exercise.translation) (case-insensitive)
     - SENTENCE_BUILDER: userAnswer.equals(exercise.correctSentence) (trim)
     - LISTENING/IMAGE: userAnswer.equals(exercise.correctAnswer)
  5. Если correct:
     progress.status = COMPLETED
     progress.completedAt = now()
     streakService.recordActivity(userId)
  6. Иначе: если attempts >= 3 → status = FAILED, иначе IN_PROGRESS
  7. Сохранить
  @CacheEvict("user-progress", key = userId)
  → ProgressResult
```

#### ProfileService
```
getProfile(uid):
  user = UserRepository.findById(uid) or 404
  stats = UserStatsRepository.findByUserId(uid) or empty
  → ProfileResponse {id, email, username, avatarUrl, role, level,
                     dailyGoal, notificationsEnabled,
                     currentStreak, longestStreak, totalXp}

updateProfile(uid, req):
  user.username = req.username if present
  user.avatarUrl = req.avatarUrl if present
  user.notificationsEnabled = req.notificationsEnabled if present
  → ProfileResponse
```

#### OnboardingService
```
setLevel(uid, level):   user.level = DifficultyLevel.valueOf(level); save
setDailyGoal(uid, goal): user.dailyGoal = DailyGoal.valueOf(goal); save
complete(uid):          user.onboardingCompleted = true; save
```

---

### 4.5 Безопасность и аутентификация

```
HTTP Request
    │
    ▼
FirebaseTokenFilter (OncePerRequestFilter)
    │ Извлечь заголовок Authorization: Bearer <token>
    │ FirebaseAuth.getInstance().verifyIdToken(token)
    │ Создать UserPrincipal(uid, email, role)
    │ SecurityContextHolder.setAuthentication(...)
    ▼
SecurityConfig (HttpSecurity)
    ├── /api/admin/** → требует роль ADMIN
    ├── /api/**       → требует аутентификацию
    └── /** остальное → открыто (Swagger UI, healthcheck)

UserPrincipal:
  String uid        — уникальный Firebase User ID
  String email
  UserRole role     — берётся из БД при первом обращении
                      (или создаётся запись User при первом логине)
```

**Важно**: Токен Firebase — это JWT, содержащий `uid` (sub), `email`, `exp`. Верификация производится публичными ключами Firebase без запроса к базе данных — это максимально быстрая и надёжная проверка.

---

### 4.6 Конфигурация инфраструктуры

#### application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tulpar
    username: ${DB_USER}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate.dialect: PostgreSQLDialect

  flyway:
    enabled: true
    locations: classpath:db/migration

  data:
    redis:
      host: localhost
      port: 6379

  cache:
    type: redis
    redis:
      time-to-live: 600000  # 10 минут в мс

ai:
  openai:
    api-key: ${OPENAI_API_KEY}
    model: gpt-4o-mini
    base-url: https://api.openai.com

minio:
  endpoint: http://localhost:9000
  access-key: ${MINIO_ACCESS_KEY}
  secret-key: ${MINIO_SECRET_KEY}
  bucket: tulpar-media

firebase:
  credentials-path: ${FIREBASE_CREDENTIALS_PATH}
```

#### Redis кэшируемые ключи
| Кэш | TTL | Инвалидация |
|---|---|---|
| `courses` | 10 мин | При создании/удалении курса |
| `course-levels` | 10 мин | При изменении уровней/уроков |
| `user-progress` | 10 мин | При submit ответа (@CacheEvict) |
| `flashcards` | 10 мин | При создании/удалении |
| `grammar` | 10 мин | При создании/удалении |

---

## 5. Фронтенд — подробное описание

### 5.1 Структура проекта

```
tulpar-front/lib/
│
├── app/
│   ├── api_client.dart          # Singleton Dio client + interceptors
│   ├── app_gate.dart            # Точка входа: маршрутизация по состоянию логина
│   ├── app_storage.dart         # SharedPreferences обёртка (весь локальный стейт)
│   ├── app_strings.dart         # Локализация (RU/KZ/EN)
│   ├── theme.dart               # AppTheme — цвета, стили
│   ├── ui_locale.dart           # UiLocaleScope — provider языка UI
│   └── content_assets.dart      # Вспомогательные ассеты
│
├── services/
│   ├── stats_service.dart       # GET /stats, /stats/calendar
│   ├── lesson_service.dart      # GET /courses, /courses/{id}/levels, /lessons/{id}
│   ├── progress_service.dart    # POST /progress
│   ├── profile_service.dart     # GET/PUT /profile
│   ├── onboarding_service.dart  # POST /onboarding/*
│   ├── chat_service.dart        # POST /chat/message, GET /chat/history
│   ├── grammar_service.dart     # GET /grammar, /grammar/{id}
│   ├── article_service.dart     # GET /articles, /articles/{id}
│   ├── flashcard_service.dart   # GET /flashcards
│   ├── daily_challenge_service.dart  # GET /daily-challenge
│   └── settings_service.dart   # GET /settings
│
├── models/
│   ├── flashcard_item.dart      # Локальная модель карточки
│   ├── saved_flashcard.dart     # SavedFlashcard для избранных
│   └── level_map_node.dart      # Узел карты уровней
│
├── data/
│   └── flashcard_deck.dart      # Локальный набор карточек
│
├── widgets/
│   ├── circle_back_button.dart  # Кнопка "назад" (круглая)
│   ├── primary_button.dart      # Основная кнопка приложения
│   └── home_level_map.dart      # Виджет карты уровней на главном экране
│
├── features/
│   ├── onboarding/
│   │   ├── splash_screen.dart
│   │   ├── welcome_screen.dart
│   │   ├── login_screen.dart
│   │   ├── signup_screen.dart
│   │   ├── onboarding_screen.dart  # Выбор уровня
│   │   └── daily_goal_screen.dart  # Выбор ежедневной цели
│   │
│   ├── auth/
│   │   ├── enter_pin_screen.dart
│   │   └── set_pin_screen.dart
│   │
│   ├── main_screen.dart             # BottomNavigationBar (4 таба)
│   │
│   ├── tabs/
│   │   ├── home_tab.dart            # Главная: стрик, недеља, кнопка урока, карта
│   │   ├── learning_tab.dart        # Обучение: 6 карточек разделов
│   │   ├── tasks_tab.dart           # Задания: ежедневная игра "4 картинки 1 слово"
│   │   └── profile_tab.dart         # Профиль: имя, стрик, XP, настройки цели
│   │
│   ├── lesson/
│   │   ├── lesson_map_screen.dart   # Карта уроков курса
│   │   └── exercise_screen.dart     # Прохождение урока с упражнениями
│   │
│   ├── challenge/
│   │   └── daily_challenge_screen.dart  # Ежедневное задание (4 картинки, буквы)
│   │
│   ├── chat/
│   │   └── chat_screen.dart         # ИИ-ассистент (чат)
│   │
│   ├── grammar/
│   │   └── grammar_screen.dart      # Список правил + детали
│   │
│   ├── settings/
│   │   ├── settings_screen.dart     # Главный экран настроек
│   │   ├── account_screen.dart      # Аккаунт
│   │   ├── notifications_screen.dart
│   │   ├── privacy_security_hub_screen.dart
│   │   ├── help_support_screen.dart
│   │   ├── terms_screen.dart
│   │   └── security_screen.dart     # PIN / биометрия
│   │
│   └── learning_content/
│       ├── flashcards_screen.dart
│       ├── articles_screen.dart
│       ├── ai_assistant_screen.dart  # → переходит на ChatScreen
│       ├── grammar_screen.dart       # → переходит на GrammarScreen
│       ├── speaking_clubs_screen.dart
│       ├── sample_module_screen.dart
│       ├── saved_words_screen.dart
│       └── club_agreement_screen.dart
```

---

### 5.2 Навигация и архитектура экранов

#### Флоу запуска приложения

```
AppGate (FutureBuilder → isLoggedIn)
    │
    ├── false → SplashScreen → WelcomeScreen
    │                            ├── LoginScreen (email/pwd, Google, Apple)
    │                            └── SignupScreen (регистрация + Firebase)
    │                                    └── OnboardingScreen (выбор уровня)
    │                                            └── DailyGoalScreen
    │                                                    └── MainScreen
    │
    └── true → PIN check
                ├── has PIN → EnterPinScreen → (биометрия или ввод PIN)
                └── no PIN  → SetPinScreen → MainScreen
```

#### Главный экран (MainScreen) — BottomNavigationBar

```
MainScreen
    ├── Tab 0: HomeTab (Главная)
    │     ├── → LessonMapScreen (список уровней/уроков)
    │     │         └── → ExerciseScreen (прохождение урока)
    │     └── "Начать урок" → открывает рекомендованный урок
    │
    ├── Tab 1: LearningTab (Обучение)
    │     ├── → FlashcardsScreen
    │     ├── → ArticlesScreen
    │     ├── → AiAssistantScreen → ChatScreen
    │     ├── → GrammarScreen
    │     ├── → SpeakingClubsScreen (coming soon)
    │     └── → SampleModuleScreen
    │
    ├── Tab 2: TasksTab (Задания)
    │     └── Встроенная мини-игра (4 картинки + клавиатура)
    │
    └── Tab 3: ProfileTab (Профиль)
          └── → SettingsScreen
                    ├── → AccountScreen
                    ├── → NotificationsScreen
                    ├── → PrivacySecurityHubScreen
                    │         └── → SecurityScreen (PIN/биометрия)
                    ├── → HelpSupportScreen
                    └── → TermsScreen
```

---

### 5.3 HTTP-клиент и сервисный слой

#### ApiClient (`lib/app/api_client.dart`)

```dart
class ApiClient {
  static final ApiClient _instance = ApiClient._internal();
  late final Dio _dio;

  // Базовый URL — меняется для prod/emulator
  static const String _baseUrl = 'http://10.0.2.2:8080/api';

  ApiClient._internal() {
    _dio = Dio(BaseOptions(
      baseUrl: _baseUrl,
      connectTimeout: Duration(seconds: 10),
      receiveTimeout: Duration(seconds: 30),
    ));

    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        // Получить Firebase ID Token
        final token = await FirebaseAuth.instance.currentUser?.getIdToken();
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        handler.next(options);
      },
      onError: (error, handler) async {
        // 401 — принудительно обновить токен и повторить
        if (error.response?.statusCode == 401) {
          final newToken = await FirebaseAuth.instance.currentUser
              ?.getIdToken(true); // forceRefresh
          if (newToken != null) {
            error.requestOptions.headers['Authorization'] = 'Bearer $newToken';
            final response = await _dio.fetch(error.requestOptions);
            handler.resolve(response);
            return;
          }
        }
        handler.next(error);
      },
    ));
  }

  Future<Response> get(String path, {Map<String, dynamic>? params}) =>
      _dio.get(path, queryParameters: params);
  Future<Response> post(String path, {dynamic data}) =>
      _dio.post(path, data: data);
  Future<Response> put(String path, {dynamic data}) =>
      _dio.put(path, data: data);
  Future<Response> patch(String path, {dynamic data}) =>
      _dio.patch(path, data: data);
}
```

#### Принцип работы всех сервисов

```dart
// Шаблон правильного использования Dio:
final response = await _api.get('/endpoint');
final data = response.data as Map<String, dynamic>;  // НЕ response as Map!

// Для PageResponse:
final data = response.data as Map<String, dynamic>;
final list = (data['content'] as List).map((e) => Model.fromJson(e)).toList();
```

---

### 5.4 Локальное хранилище

`AppStorage` — обёртка над `SharedPreferences`. Все данные разделены по аккаунтам через хэш email.

#### Хранимые данные

| Ключ (prefix + suffix) | Тип | Описание |
|---|---|---|
| `logged_in` | bool | Состояние авторизации |
| `user_email` | String | Email текущего пользователя |
| `first_name` | String | Имя (из профиля) |
| `ui_lang` | String | Язык интерфейса (ru/kz/en) |
| `selected_level` | String | Уровень сложности |
| `daily_goal_minutes` | int | Ежедневная цель (5/10/20/30) |
| `pin_set` | bool | Установлен ли PIN |
| `{hash}_pin` | String | SHA-256 хэш PIN-кода |
| `{hash}_streak` | int | Стрик (дней подряд) |
| `{hash}_streak_last_date` | String | Дата последнего визита |
| `{hash}_saved_flashcards` | String | JSON список сохранённых слов |
| `{hash}_completed_lessons` | String | JSON список ID пройденных уроков |

#### Ключевые методы

```dart
AppStorage.isLoggedIn()           → Future<bool>
AppStorage.login(email, firstName) → Future<void>
AppStorage.logout()               → Future<void>

AppStorage.getStreakDays()         → Future<int>
AppStorage.recordStreakVisitIfNeeded() → Future<void>
  // Если последний визит был вчера → streak++
  // Если сегодня уже был → без изменений
  // Иначе → streak = 1

AppStorage.getSavedFlashcards()    → Future<List<SavedFlashcard>>
AppStorage.saveFlashcard(card)     → Future<void>
AppStorage.removeFlashcard(id)     → Future<void>

AppStorage.getCompletedLessons()   → Future<List<String>>
AppStorage.markLessonCompleted(id) → Future<void>
AppStorage.isLessonUnlocked(id, index) → Future<bool>
```

**Изоляция по аккаунту**: ключи для прогресса, стрика и сохранённых слов получают префикс `sha256(email)[:8]`, что предотвращает смешивание данных при переключении аккаунтов.

---

### 5.5 Описание каждого экрана

#### SplashScreen
- Логотип приложения + анимация появления
- Через 2 сек переходит на WelcomeScreen

#### WelcomeScreen
- Онбординг-слайды (3 изображения: `onboarding_1,2,3.png`)
- Кнопки "Войти" → LoginScreen, "Создать аккаунт" → SignupScreen

#### LoginScreen
- Email + Password → `FirebaseAuth.signInWithEmailAndPassword`
- Google Sign-In → `GoogleSignIn().signIn()` → Firebase credential
- Apple Sign-In → `SignInWithApple.getAppleIDCredential()` → Firebase credential
- При успехе: `AppStorage.login()` → CheckPin → MainScreen

#### SignupScreen
- Имя + Email + Password
- `FirebaseAuth.createUserWithEmailAndPassword`
- После регистрации вызывает `OnboardingService.sendAll()`:
  - POST `/onboarding/level` с выбранным уровнем
  - POST `/onboarding/goal` с выбранной целью
  - POST `/onboarding/complete`
- → OnboardingScreen → DailyGoalScreen → MainScreen

#### SetPinScreen / EnterPinScreen
- 4-значный PIN-код
- Хранится как SHA-256 hash в `flutter_secure_storage`
- `LocalAuth.authenticate()` для биометрии (Face ID / Touch ID / Fingerprint)

#### HomeTab
- **Заголовок**: "Қайырлы таң! {name}"
- **Мини-календарь**: 7 дней недели, выделен сегодняшний
- **Стрик**: "🔥 {N} дней подряд"
- **Кнопка**: "Начать урок" → открывает первый разблокированный урок
- **HomeLevelMap**: интерактивная карта с пузырьками уровней
- **Данные**: StatsService.loadHomeData() + LessonService.getCourses() параллельно

#### LearningTab
- GridView 2×3 с карточками разделов:
  - Флэшкарты, Разговорные клубы, Статьи, ИИ-ассистент, Грамматика, Примеры модулей
- Каждая карточка — изображение + название, тап → соответствующий экран

#### TasksTab
- Заголовок "Задание дня" + описание
- **Сетка 2×2**: 4 изображения из `ContentAssets.dailyTaskCell(i)`
- **Слоты ответа**: 5 ячеек (длина слова "ҚАЛАМ")
- **Клавиатура**: 2 ряда круглых кнопок с казахскими буквами
- Логика: тап → буква добавляется в слот, кнопка ⌫ удаляет последнюю
- *Статика*: слово и картинки фиксированы (offline демо)

#### ProfileTab
- Аватар (круглый, из assets)
- Имя пользователя (редактируемое)
- Статистика: стрик, XP, уровень
- Кнопка "Изменить цель" → диалог выбора DailyGoal
- Кнопка "Настройки" → SettingsScreen
- Данные: AppStorage.getFirstName() + ProfileService.getProfile() параллельно

#### LessonMapScreen
- Список курсов → уровней → уроков
- Каждый уровень раскрывается (ExpansionTile)
- Урок: иконка 🔒 (заблокирован) или ▶ (доступен)
- Тап на доступный урок → ExerciseScreen(lessonId)
- Данные: LessonService.getCourseLevels(courseId)

#### ExerciseScreen
- Загружает урок: LessonService.getLessonDetail(lessonId)
- Прогресс-бар вверху: текущее/всего упражнений
- **Типы упражнений**:
  - `VOCABULARY`: вопрос + 4 варианта ответа (radio buttons)
  - `SENTENCE_BUILDER`: перетаскиваемые слова в нужном порядке
  - `LISTENING`: плейер + поле ввода (⚠ плейер не реализован)
  - `IMAGE_CONTEXT`: изображение + варианты ответа
  - `VIDEO_CONTEXT`: видео + варианты (⚠ видео не реализовано)
- **Проверка ответа**: POST `/progress` → `{correct: bool}`
- **Фидбек**: зелёный фон (правильно) / красный (неправильно) + объяснение
- **Финиш**: экран с результатом (X из N правильных) + кнопка завершения
- При завершении: `AppStorage.markLessonCompleted(lessonId)` + добавляется XP

#### DailyChallengeScreen
- Загружает GET `/daily-challenge`
- 2×2 сетка Image.network с placeholder-ами
- Строка слотов ответа
- Клавиатура из букв (letters из ответа сервера)
- Кнопка "Проверить" → клиентская проверка (⚠ correctWord не приходит с сервера)

#### ChatScreen (ИИ-ассистент)
- История сообщений: GET `/chat/history` при открытии
- Пузыри: сообщения пользователя — справа (зелёные), ассистента — слева (белые)
- Поле ввода + кнопка отправки
- Анимированный индикатор "печатает..." во время запроса
- POST `/chat/message` → добавляет ответ ассистента
- Auto-scroll в конец списка при новых сообщениях

#### GrammarScreen
- GET `/grammar` → список правил
- `_GrammarCard`: заголовок + превью объяснения + количество примеров
- Тап → `GrammarDetailScreen`: полное объяснение + все примеры в карточках

#### SettingsScreen
- Версия приложения справа в заголовке (из SettingsService)
- Список разделов: Аккаунт, Уведомления, Конфиденциальность, Помощь, Условия
- Кнопка выхода → AppStorage.logout() → SplashScreen

#### FlashcardsScreen
- Загружает GET `/flashcards?page=0&size=50`
- Карточки с казахским словом + перевод
- Кнопка сохранить → AppStorage.saveFlashcard()

#### ArticlesScreen
- GET `/articles?page=0&size=20`
- Список статей: заголовок + уровень сложности + дата
- Тап → открывает полный текст статьи

---

### 5.6 Локализация

`AppStr` содержит все строки интерфейса на трёх языках:

```dart
class AppStr {
  final String settings;
  final String account;
  final String notifications;
  final String privacySecurity;
  final String helpSupport;
  final String terms;
  final String logout;
  final String learningTitle;
  final String learnFlashcards;
  final String learnClubs;
  final String learnArticles;
  final String learnAi;
  final String learnGrammar;
  final String learnSample;
  final String taskOfTheDay;
  final String taskOfTheDayHint;
  final String dailyPractice;
  final String startLesson;
  final List<String> weekDayLabels;  // ['Пн','Вт','Ср','Чт','Пт','Сб','Вс']

  String streakLine(int n) => "🔥 $n ${_dayWord(n)} подряд";

  factory AppStr.fromContext(String lang) {
    switch (lang) {
      case 'kz': return AppStr._kz();
      case 'en': return AppStr._en();
      default:   return AppStr._ru();
    }
  }
}
```

Язык UI хранится в `AppStorage.getUiLang()` и предоставляется через `UiLocaleScope` (InheritedWidget), доступен из любого виджета через `UiLocaleScope.langOf(context)`.

---

### 5.7 Тема и дизайн

```dart
class AppTheme {
  // Основной брендовый цвет — тёмно-зелёный
  static const Color primary     = Color(0xFF2D4F3C);

  // Фон приложения — бежевый
  static const Color background  = Color(0xFFF3F2ED);

  // Вторичный текст
  static const Color textPrimary    = Color(0xFF1A1A1A);
  static const Color textSecondary  = Color(0xFF6B6B6B);

  // Разделители и границы
  static const Color border      = Color(0xFFDDDDD8);

  // Фоны компонентов
  static const Color chipFill       = Color(0xFFE8F0DC);
  static const Color calendarDayBg  = Color(0xFFE4E2DA);
  static const Color learningCardBg = Color(0xFFDEDAD2);
  static const Color learningTileBg = Color(0xFFEBE8E0);
}
```

Стиль: минималистичный, природный. Без ярких цветов, акцент на типографику и структуру. Все карточки имеют `borderRadius: 18–22`, мягкие тени `blurRadius: 10`, цвет тени `Colors.black.withOpacity(0.06–0.10)`.

---

## 6. Карта интеграции фронт → бэк

| Экран (Flutter) | Метод | Эндпоинт | Сервис |
|---|---|---|---|
| SignupScreen | POST | `/onboarding/level` | OnboardingService |
| SignupScreen | POST | `/onboarding/goal` | OnboardingService |
| SignupScreen | POST | `/onboarding/complete` | OnboardingService |
| HomeTab | GET | `/stats` | StatsService |
| HomeTab | GET | `/stats/calendar` | StatsService |
| HomeTab | GET | `/courses` | LessonService |
| ProfileTab | GET | `/profile` | ProfileService |
| ProfileTab | PUT | `/profile` | ProfileService |
| ProfileTab | POST | `/onboarding/goal` | OnboardingService |
| LessonMapScreen | GET | `/courses/{id}/levels` | LessonService |
| ExerciseScreen | GET | `/lessons/{id}` | LessonService |
| ExerciseScreen | POST | `/progress` | ProgressService |
| ChatScreen | GET | `/chat/history` | ChatService |
| ChatScreen | POST | `/chat/message` | ChatService |
| GrammarScreen | GET | `/grammar` | GrammarService |
| GrammarScreen | GET | `/grammar/{id}` | GrammarService |
| FlashcardsScreen | GET | `/flashcards?page=0&size=50` | FlashcardService |
| ArticlesScreen | GET | `/articles` | ArticleService |
| ArticlesScreen | GET | `/articles/{id}` | ArticleService |
| DailyChallengeScreen | GET | `/daily-challenge` | DailyChallengeService |
| SettingsScreen | GET | `/settings` | SettingsService |

---

## 7. Что сделано с нуля

### Бэкенд (создан полностью)

- ✅ Конфигурация Spring Boot проекта (Gradle, зависимости)
- ✅ Firebase Admin SDK интеграция + `FirebaseTokenFilter`
- ✅ Полная схема БД (15+ таблиц) с Flyway миграциями
- ✅ Все JPA-сущности с связями (OneToMany, ManyToOne, ManyToMany через join-таблицу)
- ✅ Иерархия упражнений через JPA Inheritance (JOINED strategy)
- ✅ Все Spring Data JPA репозитории
- ✅ Redis кэширование через `@Cacheable`/`@CacheEvict`
- ✅ MinIO интеграция для хранения медиафайлов
- ✅ OpenAI API интеграция — ИИ-тьютор казахского языка
- ✅ Сервисный слой: Chat, Streak, Lesson, Progress, Profile, Onboarding, Flashcard, Article, Grammar, DailyChallenge, Settings, Notification, Media
- ✅ 13 пользовательских + 7 административных REST контроллеров
- ✅ Глобальная обёртка ответов (`ApiResponseAdvice`)
- ✅ Глобальная обработка ошибок (`GlobalExceptionHandler`)
- ✅ Валидация ответов на стороне сервера (правильность ответа не раскрывается клиенту)
- ✅ Роль-based авторизация (USER/ADMIN)
- ✅ Swagger/OpenAPI документация

### Фронтенд (создан/доработан)

- ✅ `ApiClient` — Singleton Dio с автоматическим добавлением Firebase JWT токена и retry на 401
- ✅ `AppStorage` — полноценное локальное хранилище с изоляцией данных по аккаунту
- ✅ `AppStr` + `UiLocaleScope` — система локализации на 3 языках (RU/KZ/EN)
- ✅ `AppTheme` — единая цветовая схема
- ✅ Все сервисы: StatsService, LessonService, ProgressService, ProfileService, OnboardingService, ChatService, GrammarService, ArticleService, FlashcardService, DailyChallengeService, SettingsService
- ✅ Полный онбординг-флоу: Splash → Welcome → Login/Signup → Level → Goal → PIN → Main
- ✅ Google Sign-In + Apple Sign-In + Email/Password аутентификация
- ✅ PIN-код + биометрия
- ✅ HomeTab с реальными данными (стрик, календарь, старт урока)
- ✅ LearningTab с 6 разделами
- ✅ TasksTab — офлайн мини-игра "4 картинки 1 слово"
- ✅ ProfileTab с редактированием имени и цели
- ✅ LessonMapScreen — карта уровней с логикой разблокировки
- ✅ ExerciseScreen — прохождение урока, проверка через API, фидбек
- ✅ ChatScreen — ИИ-чат с историей и анимацией
- ✅ GrammarScreen — список правил + детали
- ✅ DailyChallengeScreen — ежедневное задание
- ✅ SettingsScreen + 5 дочерних экранов
- ✅ FlashcardsScreen с сохранением в избранное
- ✅ ArticlesScreen с детальным просмотром

---

## 8. Исправленные баги при интеграции

### Баг 1 — Все сервисы использовали `response as Map` вместо `response.data`

**Проблема**: Dio возвращает `Response<T>` объект. Прямое приведение `response as Map<String, dynamic>` всегда бросало `TypeError` в runtime.

**Было** (все 5 сервисов):
```dart
final response = await _api.get('/endpoint');
final data = response as Map<String, dynamic>; // ОШИБКА
```

**Стало**:
```dart
final response = await _api.get('/endpoint');
final data = response.data as Map<String, dynamic>; // ПРАВИЛЬНО
```

**Затронуто**: article_service, flashcard_service, chat_service, grammar_service, daily_challenge_service.

---

### Баг 2 — FlashcardService ожидал `List`, бэкенд возвращает `PageResponse`

**Проблема**: `GET /flashcards` возвращает `{content: [...], page: 0, totalElements: N, ...}`. Сервис делал `response.data as List<dynamic>` — RuntimeException.

**Исправление**:
```dart
final data = response.data as Map<String, dynamic>;
final list = (data['content'] as List)
    .map((e) => FlashcardItem.fromJson(e))
    .toList();
```
Также добавлены параметры `?page=0&size=50` к запросу.

---

### Баг 3 — Невалидное свойство `min: 60` у Container

**Проблема**: `Container` не имеет поля `min`. Использовалось в ExerciseScreen для минимальной высоты блока фидбека.

**Исправление**: Обёртка в `ConstrainedBox`:
```dart
ConstrainedBox(
  constraints: BoxConstraints(minHeight: 60),
  child: Container(
    padding: EdgeInsets.all(12),
    // ...
  ),
),
```

---

### Баг 4 — Неправильные закрывающие скобки после рефакторинга

После добавления `ConstrainedBox` образовались висячие скобки. Исправлено добавлением соответствующих `)` для `Container` и `ConstrainedBox`.

---

### Баг 5 — Неиспользуемые extension-блоки

После рефакторинга ExerciseScreen остались мёртвые `extension on BoxDecoration` и `extension ContainerMin on Widget`. Удалены для чистоты кода.

---

## 9. Что ещё не реализовано

### Критически важное (MVP-blocking)

#### 9.1 Экран уведомлений
- **Бэкенд**: Готово — `GET /notifications`, `PATCH /notifications/{id}/read`
- **Фронтенд**: Файл `notifications_screen.dart` создан, но содержит только заглушку
- **Что нужно**: ListView уведомлений, `NotificationService.getAll()`, mark as read

#### 9.2 Проверка ответа в DailyChallengeScreen
- **Проблема**: `GET /daily-challenge` не возвращает `correctWord` (намеренно, для безопасности)
- **Что нужно**: Добавить эндпоинт `POST /daily-challenge/submit` с `{word}` → `{correct: bool}`
- **Фронтенд**: Кнопка "Проверить" есть, но нет вызова API

#### 9.3 Аудио в упражнениях LISTENING
- **Бэкенд**: `ListeningExercise.audioUrl` заполняется через MinIO
- **Фронтенд**: ExerciseScreen не имеет аудиоплейера
- **Что нужно**: Пакет `audioplayers` или `just_audio`, кнопка воспроизведения

#### 9.4 Видео в упражнениях VIDEO_CONTEXT
- **Бэкенд**: `VideoExercise.videoUrl` готово
- **Фронтенд**: Нет видеоплейера
- **Что нужно**: Пакет `video_player` или `better_player`

### Важное (улучшение UX)

#### 9.5 Сводка прогресса в ProfileTab
- **Бэкенд**: `GET /progress/summary` → `{completed, total, percentage}`
- **Фронтенд**: Эндпоинт не используется; profile_tab показывает только стрик и XP
- **Что нужно**: ProgressService.getSummary() + circular progress widget

#### 9.6 Загрузка аватара
- **Бэкенд**: `PUT /profile` принимает `avatarUrl`; `POST /admin/media/upload` для загрузки файлов
- **Фронтенд**: Нет UI для выбора фото из галереи/камеры
- **Что нужно**: `image_picker` пакет + загрузка на MinIO + обновление профиля

#### 9.7 Разговорные клубы
- **Бэкенд**: `GET /conversation-club` возвращает `{status: "coming_soon"}`
- **Фронтенд**: Тайл задизейблен
- **Что нужно**: Реализовать расписание клубов и/или интеграцию с видеозвонками

#### 9.8 Сохранение статей
- **Бэкенд**: Нет эндпоинта для сохранения статей
- **Фронтенд**: Нет кнопки "сохранить" в ArticlesScreen
- **Что нужно**: `POST /articles/{id}/bookmark`, `GET /articles/bookmarked` + локальное хранение

### Инфраструктурное (prod-ready)

#### 9.9 Push-уведомления (FCM)
- Firebase Cloud Messaging не настроен
- Нет `FirebaseMessagingService` на бэкенде
- Нет обработчика `onMessage` во Flutter
- **Что нужно**: FCM интеграция на обеих сторонах, отправка уведомлений при:
  - Ежедневное напоминание об уроке
  - Сообщение от собеседника по клубу
  - Новые материалы добавлены

#### 9.10 Административный интерфейс
- **Бэкенд**: Все `/api/admin/**` эндпоинты готовы (7 контроллеров)
- **Фронтенд**: Нет ни мобильного, ни веб-интерфейса для администраторов
- **Что нужно**: Веб-панель (React/Next.js) или Flutter Web с маршрутизацией по роли

#### 9.11 Смена URL для prod
- `ApiClient._baseUrl` = `http://10.0.2.2:8080` — только Android emulator
- **Что нужно**: `dart-define` / `flutter_dotenv` для env-переменных:
  ```
  --dart-define=API_URL=https://api.tulpar.kz
  ```

#### 9.12 Обработка офлайн-режима
- Нет graceful degradation при потере сети
- **Что нужно**: `connectivity_plus` пакет + кэширование последних данных локально

---

## 10. Рекомендации по дальнейшей разработке

### Приоритет 1 — Запуск MVP

1. **Реализовать NotificationsScreen** — 2–3 часа работы, бэкенд готов
2. **Добавить аудиоплейер** — `just_audio` пакет, поставить в ExerciseScreen для типа LISTENING
3. **POST /daily-challenge/submit** — добавить эндпоинт на бэке + вызов во Flutter

### Приоритет 2 — Улучшение контента

4. **Наполнить базу данных** — через Swagger UI или Postman:
   - Создать хотя бы 2 курса с 5+ уровнями каждый
   - Добавить 50+ упражнений разных типов
   - Загрузить аудиофайлы через `POST /admin/media/upload`
   - Заполнить `daily_challenges` на 30 дней вперёд

5. **Добавить страницу прогресса** в ProfileTab с CircularProgressIndicator

### Приоритет 3 — Масштабирование

6. **Настроить FCM** — напоминания увеличивают DAU на 30–40%
7. **Реализовать Admin Panel** — веб на React или Flutter Web
8. **Перейти на BLoC/Riverpod** — текущий StatefulWidget подход не масштабируется
9. **Добавить аналитику** — Firebase Analytics для отслеживания воронки
10. **E2E тесты** — Flutter integration_test пакет для критических флоу

---

*Документация составлена по состоянию на апрель 2026. Версия приложения: 1.0.0+1.*
