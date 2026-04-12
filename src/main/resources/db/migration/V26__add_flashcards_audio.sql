ALTER TABLE flashcards
    ADD COLUMN audio_url TEXT;

UPDATE flashcards SET audio_url = 'https://pub-cfcd1f8d9d334d638e5c522acf05a60b.r2.dev/assets/audio/flashcards_salem.mp3' WHERE word_kz = 'Сәлем';

UPDATE flashcards SET audio_url = 'https://pub-cfcd1f8d9d334d638e5c522acf05a60b.r2.dev/assets/audio/flashcards_sau_bol.mp3' WHERE word_kz = 'Сау бол';

UPDATE flashcards SET audio_url = 'https://pub-cfcd1f8d9d334d638e5c522acf05a60b.r2.dev/assets/audio/flashcards_rahmet.mp3' WHERE word_kz = 'Рахмет';

UPDATE flashcards SET audio_url = 'https://pub-cfcd1f8d9d334d638e5c522acf05a60b.r2.dev/assets/audio/flashcards_ia.mp3' WHERE word_kz = 'Иә';

UPDATE flashcards SET audio_url = 'https://pub-cfcd1f8d9d334d638e5c522acf05a60b.r2.dev/assets/audio/flashcards_zhok.mp3' WHERE word_kz = 'Жоқ';

UPDATE flashcards SET audio_url = 'https://pub-cfcd1f8d9d334d638e5c522acf05a60b.r2.dev/assets/audio/flashcards_keshiriniz.mp3' WHERE word_kz = 'Кешіріңіз';

UPDATE flashcards SET audio_url = 'https://pub-cfcd1f8d9d334d638e5c522acf05a60b.r2.dev/assets/audio/flashcards_qalaysyz.mp3' WHERE word_kz = 'Қалайсыз?';

UPDATE flashcards SET audio_url = 'https://pub-cfcd1f8d9d334d638e5c522acf05a60b.r2.dev/assets/audio/flashcards_menin_atym_arman.mp3' WHERE word_kz = 'Менің атым Арман';

UPDATE flashcards SET audio_url = 'https://pub-cfcd1f8d9d334d638e5c522acf05a60b.r2.dev/assets/audio/flashcards_qaidansyz.mp3' WHERE word_kz = 'Қайдансыз?';

UPDATE flashcards SET audio_url = 'https://pub-cfcd1f8d9d334d638e5c522acf05a60b.r2.dev/assets/audio/flashcards_tusinbedim.mp3' WHERE word_kz = 'Түсінбедім';