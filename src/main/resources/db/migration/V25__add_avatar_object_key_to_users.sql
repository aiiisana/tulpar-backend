-- Stores the R2 object key for the user's avatar so we can always
-- generate a fresh (presigned or public) URL at query time instead of
-- caching a URL that may expire or point to a wrong host.
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_object_key VARCHAR(1024);
