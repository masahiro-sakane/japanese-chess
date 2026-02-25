package com.japanesechess.ai;

import com.japanesechess.domain.Board;
import com.japanesechess.domain.PlayerColor;

public interface BoardEvaluator {
    /**
     * Evaluates the board state from the perspective of the given player.
     * Positive values are good for the player, negative values are bad.
     */
    int evaluate(Board board, PlayerColor player);
}
