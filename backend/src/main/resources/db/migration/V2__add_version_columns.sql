-- Add version columns for optimistic locking

ALTER TABLE games
ADD COLUMN version BIGINT DEFAULT 0;

ALTER TABLE game_views
ADD COLUMN version BIGINT DEFAULT 0;

-- Set initial version for existing rows
UPDATE games SET version = 0 WHERE version IS NULL;
UPDATE game_views SET version = 0 WHERE version IS NULL;

-- Make version columns NOT NULL
ALTER TABLE games
ALTER COLUMN version SET NOT NULL;

ALTER TABLE game_views
ALTER COLUMN version SET NOT NULL;
