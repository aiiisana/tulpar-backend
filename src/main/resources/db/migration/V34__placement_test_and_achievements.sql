-- ─── Placement test questions ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS placement_questions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_index   INT  NOT NULL,
    question      TEXT NOT NULL,
    options       JSONB NOT NULL,          -- ["opt1","opt2","opt3","opt4"]
    correct_index INT  NOT NULL,           -- 0-based index into options
    difficulty    VARCHAR(32) NOT NULL     -- BEGINNER / ELEMENTARY / INTERMEDIATE / ADVANCED
);

-- 12 built-in questions covering all 4 difficulty tiers
INSERT INTO placement_questions (order_index, question, options, correct_index, difficulty) VALUES
-- BEGINNER (q 1-3)
(1,  'Как будет «привет» по-казахски?',
     '["Сау бол","Сәлем","Рақмет","Кешіріңіз"]', 1, 'BEGINNER'),
(2,  'Как будет «спасибо» по-казахски?',
     '["Иә","Жоқ","Рақмет","Сәлем"]', 2, 'BEGINNER'),
(3,  'Переведите: «Менің атым Айгерім»',
     '["Я говорю по-казахски","Меня зовут Айгерим","Как дела?","До свидания"]', 1, 'BEGINNER'),
-- ELEMENTARY (q 4-6)
(4,  'Выберите правильный вариант: «Сіздің үйіңіз қайда?»',
     '["Где ваш дом?","Как вас зовут?","Сколько вам лет?","Откуда вы?"]', 0, 'ELEMENTARY'),
(5,  'Что означает суффикс «-ға/-ге» в казахском языке?',
     '["Принадлежность","Направление (дательный падеж)","Исходный падеж","Множественное число"]', 1, 'ELEMENTARY'),
(6,  'Как сказать «Я хочу пить воду»?',
     '["Мен су ішкім келеді","Мен тамақ жегім келеді","Мен ұйықтағым келеді","Мен кетемін"]', 0, 'ELEMENTARY'),
-- INTERMEDIATE (q 7-9)
(7,  'Какой вид имеет глагол в предложении «Ол кітап оқып жатыр»?',
     '["Прошедшее время","Настоящее длительное","Будущее время","Повелительное наклонение"]', 1, 'INTERMEDIATE'),
(8,  'Выберите правильную форму: «Ол мектепке ___ барды».',
     '["жаяу","жаяу-жалпы","жаяулап","жаялы"]', 0, 'INTERMEDIATE'),
(9,  'Что означает конструкция «болса да»?',
     '["Поэтому","Хотя / несмотря на то что","Чтобы","Когда"]', 1, 'INTERMEDIATE'),
-- ADVANCED (q 10-12)
(10, 'Выберите правильный вариант с деепричастием: «Ол ән ___, үйге кірді».',
     '["айтып","айтқан","айтса","айтады"]', 0, 'ADVANCED'),
(11, 'Какое значение имеет суффикс «-лық/-лік» в слове «қазақтық»?',
     '["Уменьшительное","Абстрактное существительное (принадлежность к группе)","Глагольная форма","Прилагательное-усилитель"]', 1, 'ADVANCED'),
(12, 'Переведите: «Ол мәселені шеше алмады».',
     '["Он не смог решить эту проблему","Он решил проблему","Он не понял вопрос","Он ответил на вопрос"]', 0, 'ADVANCED');

-- ─── Achievements ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS achievements (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(64)  NOT NULL UNIQUE,
    title       VARCHAR(128) NOT NULL,
    description TEXT         NOT NULL,
    icon_name   VARCHAR(64)  NOT NULL,   -- icon key used in Flutter
    xp_reward   INT          NOT NULL DEFAULT 0,
    sort_order  INT          NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS user_achievements (
    id              UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         VARCHAR(128) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    achievement_code VARCHAR(64) NOT NULL REFERENCES achievements(code) ON DELETE CASCADE,
    earned_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    UNIQUE (user_id, achievement_code)
);

CREATE INDEX IF NOT EXISTS idx_user_achievements_user ON user_achievements(user_id);

-- Built-in achievement catalogue
INSERT INTO achievements (code, title, description, icon_name, xp_reward, sort_order) VALUES
('FIRST_LESSON',      'Первый шаг',          'Завершите свой первый урок',                 'star',       10,  1),
('STREAK_3',          '3 дня подряд',         'Занимайтесь 3 дня подряд',                  'fire',       15,  2),
('STREAK_7',          'Неделя без перерыва',  'Занимайтесь 7 дней подряд',                 'fire_gold',  30,  3),
('STREAK_30',         'Месяц силы',           'Занимайтесь 30 дней подряд',                'fire_red',   100, 4),
('WORDS_10',          '10 слов',              'Изучите 10 слов',                            'book',       10,  5),
('WORDS_50',          '50 слов',              'Изучите 50 слов',                            'book_gold',  25,  6),
('WORDS_100',         '100 слов',             'Изучите 100 слов',                           'trophy',     50,  7),
('FIRST_DAILY',       'Ежедневное задание',   'Выполните первое ежедневное задание',        'calendar',   10,  8),
('DAILY_7',           'Недельный марафон',    'Выполните ежедневное задание 7 дней подряд','calendar_gold',30, 9),
('FIRST_COURSE',      'Курс пройден!',        'Завершите первый курс полностью',            'graduation', 100, 10),
('PLACEMENT_DONE',    'Тест пройден',         'Пройдите тест на определение уровня',        'clipboard',  20,  11),
('XP_500',            '500 очков опыта',      'Наберите 500 XP',                            'lightning',  25,  12),
('XP_1000',           '1000 очков опыта',     'Наберите 1000 XP',                           'lightning_gold', 50, 13);
