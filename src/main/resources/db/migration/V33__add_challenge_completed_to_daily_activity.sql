-- V33: Separate "daily challenge completed" from generic "any activity today".
--      Previously, completing a lesson set completed=true, which incorrectly
--      caused the daily challenge to show as done for that user.
ALTER TABLE user_daily_activity
    ADD COLUMN IF NOT EXISTS challenge_completed BOOLEAN NOT NULL DEFAULT FALSE;
