package com.japanesechess.ai;

import com.japanesechess.domain.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RandomStrategyTest {

    private final RandomStrategy strategy = new RandomStrategy();

    @Test
    void selectMove_ShouldReturnMove_ForInitialPosition() {
        Board board = Board.createInitialBoard();

        Optional<Move> move = strategy.selectMove(board, PlayerColor.BLACK);

        assertTrue(move.isPresent(), "Should select a move from initial position");
    }

    @Test
    void selectMove_ShouldReturnEmpty_WhenNoMovesAvailable() {
        // Empty board has no pieces, so no moves
        Board board = new Board();

        Optional<Move> move = strategy.selectMove(board, PlayerColor.BLACK);

        assertTrue(move.isEmpty(), "Should return empty when no moves available");
    }

    @Test
    void selectMove_ShouldReturnValidMove() {
        Board board = Board.createInitialBoard();
        MoveValidator validator = new MoveValidator();

        Optional<Move> move = strategy.selectMove(board, PlayerColor.BLACK);

        assertTrue(move.isPresent());
        assertTrue(validator.isValidMove(board, move.get()), "Selected move should be valid");
    }
}
