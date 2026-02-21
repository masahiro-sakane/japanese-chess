package com.japanesechess.ai;

import com.japanesechess.domain.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StandardBoardEvaluatorTest {

    private final StandardBoardEvaluator evaluator = new StandardBoardEvaluator();

    @Test
    void evaluate_ShouldReturnZeroForInitialBoard() {
        Board board = Board.createInitialBoard();

        int blackScore = evaluator.evaluate(board, PlayerColor.BLACK);
        int whiteScore = evaluator.evaluate(board, PlayerColor.WHITE);

        assertEquals(0, blackScore, "Initial position should be balanced for BLACK");
        assertEquals(0, whiteScore, "Initial position should be balanced for WHITE");
    }

    @Test
    void evaluate_ShouldReturnPositive_WhenPlayerHasMorePieces() {
        Board board = new Board();

        // BLACK has king and rook, WHITE has only king
        board.placePiece(new Piece(PieceType.KING, PlayerColor.BLACK, new Position(8, 4)));
        board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(7, 1)));
        board.placePiece(new Piece(PieceType.KING, PlayerColor.WHITE, new Position(0, 4)));

        int blackScore = evaluator.evaluate(board, PlayerColor.BLACK);

        assertTrue(blackScore > 0, "BLACK with extra rook should have positive score");
    }

    @Test
    void evaluate_ShouldReturnNegative_WhenOpponentHasMorePieces() {
        Board board = new Board();

        // WHITE has king and rook, BLACK has only king
        board.placePiece(new Piece(PieceType.KING, PlayerColor.BLACK, new Position(8, 4)));
        board.placePiece(new Piece(PieceType.KING, PlayerColor.WHITE, new Position(0, 4)));
        board.placePiece(new Piece(PieceType.ROOK, PlayerColor.WHITE, new Position(1, 7)));

        int blackScore = evaluator.evaluate(board, PlayerColor.BLACK);

        assertTrue(blackScore < 0, "BLACK disadvantaged should have negative score");
    }
}
