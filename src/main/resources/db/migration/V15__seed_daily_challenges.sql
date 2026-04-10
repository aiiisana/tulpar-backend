-- V15: Seed default daily challenges for the next 60 days.
--
-- Each challenge has:
--   correct_word  — the Kazakh word the user must identify/build
--   letters       — JSON array of individual letters (shuffled, stored as TEXT via StringListConverter)
--   image_urls    — JSON array of placeholder image URLs (replace with real MinIO URLs later)
--
-- ON CONFLICT DO NOTHING means re-running the migration is safe (idempotent).

INSERT INTO daily_challenges (challenge_date, correct_word, letters, image_urls)
VALUES
-- Week 1: Basic nouns
(CURRENT_DATE + 0,  'үй',       '["ү","й","а","т"]',         '["/static/assets/ui.webp","/static/assets/water.jpeg","/static/assets/nan.jpeg"]'),
(CURRENT_DATE + 1,  'су',       '["с","у","б","р"]',          '["/static/assets/water.jpeg","/static/assets/cat.png","/static/assets/nan.jpeg"]'),
(CURRENT_DATE + 2,  'нан',      '["н","а","н","т","е"]',      '["/static/assets/nan.jpeg","/static/assets/water.jpeg","/static/assets/alma.jpeg"]'),
(CURRENT_DATE + 3,  'алма',     '["а","л","м","а","к","ш"]',  '["/static/assets/alma.jpeg","/static/assets/ui.webp","/static/assets/nan.jpeg"]'),
(CURRENT_DATE + 4,  'мысық',      '["м","қ","и","ы","к","ы"]',      '["/static/assets/cat.png","/static/assets/dog.jpeg","/static/assets/horse.png"]'),
(CURRENT_DATE + 5,  'ит',       '["и","т","а","к","е"]',      '["/static/assets/dog.jpeg","/static/assets/cat.png","/static/assets/horse.png"]'),
(CURRENT_DATE + 6,  'ат',       '["а","т","ш","е","с"]',      '["/static/assets/horse.png","/static/assets/dog.jpeg","/static/assets/water.jpeg"]'),

-- Week 2: Colors
(CURRENT_DATE + 7,  'қызыл',    '["қ","ы","з","ы","л","а","к"]',   '["https://cdn.tulpar.app/challenges/qyzyl.jpg","https://cdn.tulpar.app/challenges/kок.jpg","https://cdn.tulpar.app/challenges/sary.jpg"]'),
(CURRENT_DATE + 8,  'көк',      '["к","ө","к","а","с","р"]',        '["https://cdn.tulpar.app/challenges/kok.jpg","https://cdn.tulpar.app/challenges/qyzyl.jpg","https://cdn.tulpar.app/challenges/zhashyl.jpg"]'),
(CURRENT_DATE + 9,  'сары',     '["с","а","р","ы","к","л"]',        '["https://cdn.tulpar.app/challenges/sary.jpg","https://cdn.tulpar.app/challenges/kok.jpg","https://cdn.tulpar.app/challenges/qyzyl.jpg"]'),
(CURRENT_DATE + 10, 'жасыл',    '["ж","а","с","ы","л","к","т"]',    '["https://cdn.tulpar.app/challenges/zhashyl.jpg","https://cdn.tulpar.app/challenges/sary.jpg","https://cdn.tulpar.app/challenges/ak.jpg"]'),
(CURRENT_DATE + 11, 'ақ',       '["а","қ","к","с","ш"]',            '["https://cdn.tulpar.app/challenges/ak.jpg","https://cdn.tulpar.app/challenges/qara.jpg","https://cdn.tulpar.app/challenges/kok.jpg"]'),
(CURRENT_DATE + 12, 'қара',     '["қ","а","р","а","к","л","т"]',    '["https://cdn.tulpar.app/challenges/qara.jpg","https://cdn.tulpar.app/challenges/ak.jpg","https://cdn.tulpar.app/challenges/sary.jpg"]'),
(CURRENT_DATE + 13, 'күміс',    '["к","ү","м","і","с","а","л"]',    '["https://cdn.tulpar.app/challenges/kumis.jpg","https://cdn.tulpar.app/challenges/kok.jpg","https://cdn.tulpar.app/challenges/sary.jpg"]'),

-- Week 3: Numbers
(CURRENT_DATE + 14, 'бір',      '["б","і","р","а","е","к"]',        '["https://cdn.tulpar.app/challenges/bir.jpg","https://cdn.tulpar.app/challenges/eki.jpg","https://cdn.tulpar.app/challenges/ush.jpg"]'),
(CURRENT_DATE + 15, 'екі',      '["е","к","і","б","р","ш"]',        '["https://cdn.tulpar.app/challenges/eki.jpg","https://cdn.tulpar.app/challenges/bir.jpg","https://cdn.tulpar.app/challenges/tort.jpg"]'),
(CURRENT_DATE + 16, 'үш',       '["ү","ш","е","к","б","і"]',        '["https://cdn.tulpar.app/challenges/ush.jpg","https://cdn.tulpar.app/challenges/eki.jpg","https://cdn.tulpar.app/challenges/dort.jpg"]'),
(CURRENT_DATE + 17, 'төрт',     '["т","ө","р","т","б","е","с"]',    '["https://cdn.tulpar.app/challenges/tort.jpg","https://cdn.tulpar.app/challenges/ush.jpg","https://cdn.tulpar.app/challenges/bes.jpg"]'),
(CURRENT_DATE + 18, 'бес',      '["б","е","с","а","л","т","ы"]',    '["https://cdn.tulpar.app/challenges/bes.jpg","https://cdn.tulpar.app/challenges/tort.jpg","https://cdn.tulpar.app/challenges/alty.jpg"]'),
(CURRENT_DATE + 19, 'алты',     '["а","л","т","ы","б","е","с"]',    '["https://cdn.tulpar.app/challenges/alty.jpg","https://cdn.tulpar.app/challenges/bes.jpg","https://cdn.tulpar.app/challenges/zhety.jpg"]'),
(CURRENT_DATE + 20, 'жеті',     '["ж","е","т","і","а","л","с"]',    '["https://cdn.tulpar.app/challenges/zhety.jpg","https://cdn.tulpar.app/challenges/alty.jpg","https://cdn.tulpar.app/challenges/segiz.jpg"]'),

-- Week 4: Family
(CURRENT_DATE + 21, 'ана',      '["а","н","а","к","е","б"]',        '["https://cdn.tulpar.app/challenges/ana.jpg","https://cdn.tulpar.app/challenges/ake.jpg","https://cdn.tulpar.app/challenges/bala.jpg"]'),
(CURRENT_DATE + 22, 'әке',      '["ə","к","е","а","н","б"]',        '["https://cdn.tulpar.app/challenges/ake.jpg","https://cdn.tulpar.app/challenges/ana.jpg","https://cdn.tulpar.app/challenges/aga.jpg"]'),
(CURRENT_DATE + 23, 'бала',     '["б","а","л","а","н","к","е"]',    '["https://cdn.tulpar.app/challenges/bala.jpg","https://cdn.tulpar.app/challenges/ana.jpg","https://cdn.tulpar.app/challenges/ake.jpg"]'),
(CURRENT_DATE + 24, 'аға',      '["а","ғ","а","н","б","к","е"]',    '["https://cdn.tulpar.app/challenges/aga.jpg","https://cdn.tulpar.app/challenges/ake.jpg","https://cdn.tulpar.app/challenges/bala.jpg"]'),
(CURRENT_DATE + 25, 'апа',      '["а","п","а","ш","е","к"]',        '["https://cdn.tulpar.app/challenges/apa.jpg","https://cdn.tulpar.app/challenges/ana.jpg","https://cdn.tulpar.app/challenges/aga.jpg"]'),
(CURRENT_DATE + 26, 'іні',      '["і","н","і","а","б","к"]',        '["https://cdn.tulpar.app/challenges/ini.jpg","https://cdn.tulpar.app/challenges/aga.jpg","https://cdn.tulpar.app/challenges/bala.jpg"]'),
(CURRENT_DATE + 27, 'қыз',      '["қ","ы","з","а","б","ш","е"]',    '["https://cdn.tulpar.app/challenges/qyz.jpg","https://cdn.tulpar.app/challenges/bala.jpg","https://cdn.tulpar.app/challenges/ini.jpg"]'),

-- Week 5: Common verbs / actions
(CURRENT_DATE + 28, 'бар',      '["б","а","р","ж","о","қ"]',        '["https://cdn.tulpar.app/challenges/bar.jpg","https://cdn.tulpar.app/challenges/zhoq.jpg","https://cdn.tulpar.app/challenges/kel.jpg"]'),
(CURRENT_DATE + 29, 'кел',      '["к","е","л","б","а","р"]',        '["https://cdn.tulpar.app/challenges/kel.jpg","https://cdn.tulpar.app/challenges/bar.jpg","https://cdn.tulpar.app/challenges/jet.jpg"]'),
(CURRENT_DATE + 30, 'жей',      '["ж","е","й","ал","і","п"]',       '["https://cdn.tulpar.app/challenges/zhei.jpg","https://cdn.tulpar.app/challenges/kel.jpg","https://cdn.tulpar.app/challenges/bar.jpg"]'),
(CURRENT_DATE + 31, 'оқы',      '["о","қ","ы","ж","е","к","л"]',    '["https://cdn.tulpar.app/challenges/oqy.jpg","https://cdn.tulpar.app/challenges/zhei.jpg","https://cdn.tulpar.app/challenges/kel.jpg"]'),
(CURRENT_DATE + 32, 'жаз',      '["ж","а","з","о","қ","ы","к"]',    '["https://cdn.tulpar.app/challenges/zhaz.jpg","https://cdn.tulpar.app/challenges/oqy.jpg","https://cdn.tulpar.app/challenges/kel.jpg"]'),
(CURRENT_DATE + 33, 'айт',      '["а","й","т","ж","о","қ","ы"]',    '["https://cdn.tulpar.app/challenges/ait.jpg","https://cdn.tulpar.app/challenges/zhaz.jpg","https://cdn.tulpar.app/challenges/oqy.jpg"]'),
(CURRENT_DATE + 34, 'тыңда',    '["т","ы","ң","д","а","ж","о","қ"]','["https://cdn.tulpar.app/challenges/tyngda.jpg","https://cdn.tulpar.app/challenges/ait.jpg","https://cdn.tulpar.app/challenges/oqy.jpg"]'),

-- Week 6: Greetings and common phrases
(CURRENT_DATE + 35, 'сәлем',    '["с","ə","л","е","м","к","а"]',    '["https://cdn.tulpar.app/challenges/salem.jpg","https://cdn.tulpar.app/challenges/rahmet.jpg","https://cdn.tulpar.app/challenges/kesu.jpg"]'),
(CURRENT_DATE + 36, 'рахмет',   '["р","а","х","м","е","т","с","ə"]','["https://cdn.tulpar.app/challenges/rahmet.jpg","https://cdn.tulpar.app/challenges/salem.jpg","https://cdn.tulpar.app/challenges/kesu.jpg"]'),
(CURRENT_DATE + 37, 'иə',       '["и","ə","ж","о","қ","б"]',        '["https://cdn.tulpar.app/challenges/ie.jpg","https://cdn.tulpar.app/challenges/zhoq.jpg","https://cdn.tulpar.app/challenges/bar.jpg"]'),
(CURRENT_DATE + 38, 'жоқ',      '["ж","о","қ","и","ə","б","а"]',    '["https://cdn.tulpar.app/challenges/zhoq.jpg","https://cdn.tulpar.app/challenges/ie.jpg","https://cdn.tulpar.app/challenges/bar.jpg"]'),
(CURRENT_DATE + 39, 'кеш',      '["к","е","ш","с","ə","л","м"]',    '["https://cdn.tulpar.app/challenges/kesh.jpg","https://cdn.tulpar.app/challenges/salem.jpg","https://cdn.tulpar.app/challenges/rahmet.jpg"]'),
(CURRENT_DATE + 40, 'болды',    '["б","о","л","д","ы","қ","е","ш"]','["https://cdn.tulpar.app/challenges/boldy.jpg","https://cdn.tulpar.app/challenges/kesh.jpg","https://cdn.tulpar.app/challenges/rahmet.jpg"]'),
(CURRENT_DATE + 41, 'сол',      '["с","о","л","б","ұ","л","а","м"]','["https://cdn.tulpar.app/challenges/sol.jpg","https://cdn.tulpar.app/challenges/bul.jpg","https://cdn.tulpar.app/challenges/boldy.jpg"]'),

-- Week 7: Nature
(CURRENT_DATE + 42, 'күн',      '["к","ү","н","а","й","ж","ұ"]',    '["https://cdn.tulpar.app/challenges/kun.jpg","https://cdn.tulpar.app/challenges/ai.jpg","https://cdn.tulpar.app/challenges/zhuldyz.jpg"]'),
(CURRENT_DATE + 43, 'ай',       '["а","й","к","ү","н","ж","ұ"]',    '["https://cdn.tulpar.app/challenges/ai.jpg","https://cdn.tulpar.app/challenges/kun.jpg","https://cdn.tulpar.app/challenges/zhuldyz.jpg"]'),
(CURRENT_DATE + 44, 'жұлдыз',   '["ж","ұ","л","д","ы","з","к","ү","н"]','["https://cdn.tulpar.app/challenges/zhuldyz.jpg","https://cdn.tulpar.app/challenges/kun.jpg","https://cdn.tulpar.app/challenges/ai.jpg"]'),
(CURRENT_DATE + 45, 'тау',      '["т","а","у","ж","ұ","л","к"]',    '["https://cdn.tulpar.app/challenges/tau.jpg","https://cdn.tulpar.app/challenges/zhuldyz.jpg","https://cdn.tulpar.app/challenges/kun.jpg"]'),
(CURRENT_DATE + 46, 'өзен',     '["ө","з","е","н","т","а","у"]',    '["https://cdn.tulpar.app/challenges/ozen.jpg","https://cdn.tulpar.app/challenges/tau.jpg","https://cdn.tulpar.app/challenges/su.jpg"]'),
(CURRENT_DATE + 47, 'орман',    '["о","р","м","а","н","ж","ұ","л"]','["https://cdn.tulpar.app/challenges/orman.jpg","https://cdn.tulpar.app/challenges/tau.jpg","https://cdn.tulpar.app/challenges/ozen.jpg"]'),
(CURRENT_DATE + 48, 'дала',     '["д","а","л","а","т","о","р"]',    '["https://cdn.tulpar.app/challenges/dala.jpg","https://cdn.tulpar.app/challenges/orman.jpg","https://cdn.tulpar.app/challenges/tau.jpg"]'),

-- Week 8: Food
(CURRENT_DATE + 49, 'ет',       '["е","т","б","а","л","ы","қ"]',    '["https://cdn.tulpar.app/challenges/et.jpg","https://cdn.tulpar.app/challenges/balyk.jpg","https://cdn.tulpar.app/challenges/nan.jpg"]'),
(CURRENT_DATE + 50, 'балық',    '["б","а","л","ы","қ","е","т"]',    '["https://cdn.tulpar.app/challenges/balyk.jpg","https://cdn.tulpar.app/challenges/et.jpg","https://cdn.tulpar.app/challenges/nan.jpg"]'),
(CURRENT_DATE + 51, 'сүт',      '["с","ү","т","ш","а","й","к"]',    '["https://cdn.tulpar.app/challenges/sut.jpg","https://cdn.tulpar.app/challenges/shay.jpg","https://cdn.tulpar.app/challenges/nan.jpg"]'),
(CURRENT_DATE + 52, 'шай',      '["ш","а","й","с","ү","т","к"]',    '["https://cdn.tulpar.app/challenges/shay.jpg","https://cdn.tulpar.app/challenges/sut.jpg","https://cdn.tulpar.app/challenges/su.jpg"]'),
(CURRENT_DATE + 53, 'қант',     '["қ","а","н","т","ш","й","с"]',    '["https://cdn.tulpar.app/challenges/qant.jpg","https://cdn.tulpar.app/challenges/shay.jpg","https://cdn.tulpar.app/challenges/sut.jpg"]'),
(CURRENT_DATE + 54, 'май',      '["м","а","й","с","ү","т","қ"]',    '["https://cdn.tulpar.app/challenges/mai.jpg","https://cdn.tulpar.app/challenges/sut.jpg","https://cdn.tulpar.app/challenges/qant.jpg"]'),
(CURRENT_DATE + 55, 'жұмыртқа', '["ж","ұ","м","ы","р","т","қ","а","с","ү"]','["https://cdn.tulpar.app/challenges/zhumyrtqa.jpg","https://cdn.tulpar.app/challenges/et.jpg","https://cdn.tulpar.app/challenges/balyk.jpg"]'),

-- Week 9-10: Body parts
(CURRENT_DATE + 56, 'бас',      '["б","а","с","қ","о","л","к"]',    '["https://cdn.tulpar.app/challenges/bas.jpg","https://cdn.tulpar.app/challenges/qol.jpg","https://cdn.tulpar.app/challenges/ayaq.jpg"]'),
(CURRENT_DATE + 57, 'қол',      '["қ","о","л","б","а","с","а"]',    '["https://cdn.tulpar.app/challenges/qol.jpg","https://cdn.tulpar.app/challenges/bas.jpg","https://cdn.tulpar.app/challenges/ayaq.jpg"]'),
(CURRENT_DATE + 58, 'аяқ',      '["а","я","қ","б","а","с","қ","о"]','["https://cdn.tulpar.app/challenges/ayaq.jpg","https://cdn.tulpar.app/challenges/qol.jpg","https://cdn.tulpar.app/challenges/bas.jpg"]'),
(CURRENT_DATE + 59, 'көз',      '["к","ө","з","қ","а","с","б"]',    '["https://cdn.tulpar.app/challenges/koz.jpg","https://cdn.tulpar.app/challenges/qulaq.jpg","https://cdn.tulpar.app/challenges/bas.jpg"]')

ON CONFLICT (challenge_date) DO NOTHING;
