-- V23: Migrate all stored media URLs to Cloudflare R2 public URL format.
--
-- R2 public base: https://pub-cfcd1f8d9d334d638e5c522acf05a60b.r2.dev
--
-- Tables affected:
--   media_files          — url rebuilt from object_key
--   image_exercises      — image_url: /assets/... → R2
--   listening_exercises  — audio_url: /assets/... → R2
--   daily_challenges     — image_urls (JSON TEXT array): two patterns
--       /assets/...                         → R2/assets/...
--       https://cdn.tulpar.app/challenges/  → R2/challenges/
--
-- Idempotent: every UPDATE is guarded by a WHERE that skips already-migrated rows.

-- ── 1. media_files ────────────────────────────────────────────────────────────
-- URL is reconstructed directly from object_key (e.g. 'audio/uuid.mp3').
-- Rows already pointing at R2 are left untouched.
UPDATE media_files
SET    url = 'https://pub-cfcd1f8d9d334d638e5c522acf05a60b.r2.dev/' || object_key
WHERE  url NOT LIKE 'https://pub-cfcd1f8d9d334d638e5c522acf05a60b.r2.dev/%';

-- ── 2. image_exercises ───────────────────────────────────────────────────────
-- Relative path /assets/foo.png  →  https://<r2>/assets/foo.png
-- LTRIM(col, '/') strips the leading slash.
UPDATE image_exercises
SET    image_url = 'https://pub-cfcd1f8d9d334d638e5c522acf05a60b.r2.dev/' || LTRIM(image_url, '/')
WHERE  image_url LIKE '/%';

-- ── 3. listening_exercises ───────────────────────────────────────────────────
-- Same pattern as image_exercises.
UPDATE listening_exercises
SET    audio_url = 'https://pub-cfcd1f8d9d334d638e5c522acf05a60b.r2.dev/' || LTRIM(audio_url, '/')
WHERE  audio_url LIKE '/%';

-- ── 4. daily_challenges ──────────────────────────────────────────────────────
-- image_urls is stored as a JSON array in a TEXT column, e.g.:
--   ["/assets/alma.jpeg", "/assets/water.jpeg"]
--   ["https://cdn.tulpar.app/challenges/qyzyl.jpg", ...]
--
-- We replace the URL prefix inside the JSON string with REPLACE().

-- 4a. Relative /assets/ paths (week 1 challenges, already normalised by V17)
UPDATE daily_challenges
SET    image_urls = REPLACE(
           image_urls,
           '"/assets/',
           '"https://pub-cfcd1f8d9d334d638e5c522acf05a60b.r2.dev/assets/'
       )
WHERE  image_urls LIKE '%"/assets/%';

-- 4b. Old CDN URLs  https://cdn.tulpar.app/challenges/  → R2/challenges/
UPDATE daily_challenges
SET    image_urls = REPLACE(
           image_urls,
           'https://cdn.tulpar.app/challenges/',
           'https://pub-cfcd1f8d9d334d638e5c522acf05a60b.r2.dev/challenges/'
       )
WHERE  image_urls LIKE '%https://cdn.tulpar.app/challenges/%';
