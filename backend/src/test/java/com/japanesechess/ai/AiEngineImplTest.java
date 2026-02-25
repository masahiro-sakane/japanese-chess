package com.japanesechess.ai;

import com.japanesechess.domain.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AiEngineImplTest {

    private final AiEngineImpl aiEngine = new AiEngineImpl();

    @Test
    void selectMove_Beginner_ShouldReturnMove() {
        Board board = Board.createInitialBoard();

        Optional<Move> move = aiEngine.selectMove(board, PlayerColor.BLACK, AiDifficulty.BEGINNER);

        assertTrue(move.isPresent(), "BEGINNER should select a move");
    }

    @Test
    void selectMove_Intermediate_ShouldReturnMove() {
        Board board = Board.createInitialBoard();

        Optional<Move> move = aiEngine.selectMove(board, PlayerColor.BLACK, AiDifficulty.INTERMEDIATE);

        assertTrue(move.isPresent(), "INTERMEDIATE should select a move");
    }

    @Test
    void selectMove_Advanced_ShouldReturnMove() {
        Board board = Board.createInitialBoard();

        Optional<Move> move = aiEngine.selectMove(board, PlayerColor.BLACK, AiDifficulty.ADVANCED);

        assertTrue(move.isPresent(), "ADVANCED should select a move");
    }

    @Test
    void selectMove_AllDifficulties_ShouldReturnValidMoves() {
        Board board = Board.createInitialBoard();
        MoveValidator validator = new MoveValidator();

        for (AiDifficulty difficulty : AiDifficulty.values()) {
            Optional<Move> move = aiEngine.selectMove(board, PlayerColor.WHITE, difficulty);
            assertTrue(move.isPresent(), "Should select move for " + difficulty);
            assertTrue(validator.isValidMove(board, move.get()), "Move should be valid for " + difficulty);
        }
    }
}
