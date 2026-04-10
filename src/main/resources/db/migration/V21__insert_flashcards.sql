INSERT INTO flashcards (id, word_ru, word_kz, transcription, example_sentence)
VALUES (gen_random_uuid(), 'Привет', 'Сәлем', 'sälem', 'Сәлем! Қалың қалай?'),
       (gen_random_uuid(), 'До свидания', 'Сау бол', 'sau bol', 'Сау бол! Көріскенше!'),
       (gen_random_uuid(), 'Спасибо', 'Рахмет', 'rahmet', 'Көмегіңе рахмет'),
       (gen_random_uuid(), 'Да', 'Иә', 'yä', 'Иә, дұрыс'),
       (gen_random_uuid(), 'Нет', 'Жоқ', 'jok', 'Жоқ, келмеймін'),
       (gen_random_uuid(), 'Извините', 'Кешіріңіз', 'keshiriñiz', 'Кешіріңіз, түсінбедім'),
       (gen_random_uuid(), 'Как дела?', 'Қалайсыз?', 'qalaysyz', 'Сәлем! Қалайсыз?'),
       (gen_random_uuid(), 'Меня зовут Арман', 'Менің атым Арман', 'meniñ atym Arman', 'Менің атым Арман'),
       (gen_random_uuid(), 'Откуда вы?', 'Қайдансыз?', 'qaidansyz', 'Сіз қайдансыз?'),
       (gen_random_uuid(), 'Не понял(а)', 'Түсінбедім', 'tüsinbedim', 'Кешіріңіз, түсінбедім');