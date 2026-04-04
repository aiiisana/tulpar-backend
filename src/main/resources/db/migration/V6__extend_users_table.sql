-- Extend users table with onboarding, profile and learning preference fields
ALTER TABLE users ADD COLUMN username              VARCHAR(100);
ALTER TABLE users ADD COLUMN avatar_url            VARCHAR(1024);
ALTER TABLE users ADD COLUMN notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN level                 VARCHAR(32);
ALTER TABLE users ADD COLUMN daily_goal            VARCHAR(32);
ALTER TABLE users ADD COLUMN onboarding_completed  BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE users ADD CONSTRAINT chk_users_level
    CHECK (level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED') OR level IS NULL);

ALTER TABLE users ADD CONSTRAINT chk_users_daily_goal
    CHECK (daily_goal IN ('CASUAL', 'REGULAR', 'SERIOUS', 'INTENSE') OR daily_goal IS NULL);
