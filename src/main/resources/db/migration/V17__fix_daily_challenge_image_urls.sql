-- V17: Fix daily challenge image URLs.
-- Spring Boot serves static resources from src/main/resources/static/ at the
-- root path (/), NOT at /static/. So /static/assets/foo.jpg → /assets/foo.jpg.
-- The image_urls column is TEXT (JSON array stored as string), so plain REPLACE works.

UPDATE daily_challenges
SET image_urls = REPLACE(image_urls, '/static/assets/', '/assets/')
WHERE image_urls LIKE '%/static/assets/%';
