package com.japanesechess.ai;

import com.japanesechess.domain.Move;
import com.japanesechess.domain.PlayerColor;

public class AiMoveResult {
    private final Move move;
    private final PlayerColor aiColor;
    private final AiDifficulty difficulty;

    public AiMoveResult(Move move, PlayerColor aiColor, AiDifficulty difficulty) {
        this.move = move;
        this.aiColor = aiColor;
        this.difficulty = difficulty;
    }

    public Move getMove() {
        return move;
    }

    public PlayerColor getAiColor() {
        return aiColor;
    }

    public AiDifficulty getDifficulty() {
        return difficulty;
    }

    public boolean isDrop() {
        return move.isDrop();
    }

    public String getMoveDescription() {
        return move.toString();
    }
}
