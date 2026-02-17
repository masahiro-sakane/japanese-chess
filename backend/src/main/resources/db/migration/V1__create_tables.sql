-- Games table
CREATE TABLE games (
    game_id UUID PRIMARY KEY,
    black_player_id UUID NOT NULL,
    white_player_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    current_turn VARCHAR(10),
    winner VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Game views table (denormalized board state)
CREATE TABLE game_views (
    game_id UUID PRIMARY KEY,
    board_state JSONB NOT NULL,
    black_captured_pieces JSONB NOT NULL DEFAULT '[]',
    white_captured_pieces JSONB NOT NULL DEFAULT '[]',
    move_count INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (game_id) REFERENCES games(game_id) ON DELETE CASCADE
);

-- Move history table
CREATE TABLE move_history (
    id BIGSERIAL PRIMARY KEY,
    game_id UUID NOT NULL,
    move_number INTEGER NOT NULL,
    player VARCHAR(10) NOT NULL,
    from_row INTEGER,
    from_column INTEGER,
    to_row INTEGER NOT NULL,
    to_column INTEGER NOT NULL,
    piece_type VARCHAR(20) NOT NULL,
    promoted BOOLEAN NOT NULL DEFAULT FALSE,
    is_drop BOOLEAN NOT NULL DEFAULT FALSE,
    captured_piece VARCHAR(20),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (game_id) REFERENCES games(game_id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_games_status ON games(status);
CREATE INDEX idx_games_created_at ON games(created_at);
CREATE INDEX idx_move_history_game_id ON move_history(game_id);
CREATE INDEX idx_move_history_move_number ON move_history(game_id, move_number);
