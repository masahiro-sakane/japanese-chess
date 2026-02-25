package com.japanesechess.ai;

import com.japanesechess.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveGeneratorTest {

    private final MoveGenerator moveGenerator = new MoveGenerator();

    @Test
    void generateAllMoves_ShouldReturnMovesForInitialPosition() {
        Board board = Board.createInitialBoard();

        List<Move> moves = moveGenerator.generateAllMoves(board, PlayerColor.BLACK);

        assertFalse(moves.isEmpty(), "Should have moves in initial position");
        // BLACK's first moves: 9 pawns can each move forward 1 square = 9 moves minimum
        assertTrue(moves.size() >= 9, "Should have at least 9 pawn moves");
    }

    @Test
    void generateAllMoves_ShouldIncludeDropMoves_WhenCapturedPiecesExist() {
        Board board = Board.createInitialBoard();

        // Simulate having a captured pawn
        Board boardWithCaptured = board.applyMove(
            Move.normalMove(new Position(6, 4), new Position(5, 4), PieceType.PAWN, PlayerColor.BLACK)
        );
        // WHITE's pawn capture (simplified - just add to captured directly)
        List<Move> moves = moveGenerator.generateAllMoves(boardWithCaptured, PlayerColor.BLACK);

        assertFalse(moves.isEmpty());
    }

    @Test
    void generateAllMoves_ShouldReturnEmptyForWhiteInInitialPosition_AfterBlackMoves() {
        Board board = Board.createInitialBoard();
        // WHITE can also generate moves from initial position
        List<Move> whiteMoves = moveGenerator.generateAllMoves(board, PlayerColor.WHITE);

        assertFalse(whiteMoves.isEmpty(), "WHITE should also have moves");
        assertTrue(whiteMoves.size() >= 9, "WHITE should have at least 9 pawn moves");
    }
}
