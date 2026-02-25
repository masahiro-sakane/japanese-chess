package com.japanesechess.ai;

import com.japanesechess.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BoardEvaluator - 盤面評価クラス")
class BoardEvaluatorTest {

    private BoardEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new BoardEvaluator();
    }

    @Test
    @DisplayName("初期盤面の評価値は対称（双方0点）")
    void shouldReturnZeroScoreForInitialBoard() {
        Board board = Board.createInitialBoard();

        int blackScore = evaluator.evaluate(board, PlayerColor.BLACK);
        int whiteScore = evaluator.evaluate(board, PlayerColor.WHITE);

        assertEquals(0, blackScore, "Black's score should be 0 on initial board");
        assertEquals(0, whiteScore, "White's score should be 0 on initial board");
    }

    @Test
    @DisplayName("飛車を持つ側の評価が高い")
    void shouldEvaluateHigherWhenHavingRook() {
        Board board = new Board();
        board.placePiece(new Piece(PieceType.KING, PlayerColor.BLACK, new Position(8, 4)));
        board.placePiece(new Piece(PieceType.KING, PlayerColor.WHITE, new Position(0, 4)));
        board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(7, 1)));

        int blackScore = evaluator.evaluate(board, PlayerColor.BLACK);
        int whiteScore = evaluator.evaluate(board, PlayerColor.WHITE);

        assertTrue(blackScore > 0, "Black with rook should have positive score");
        assertTrue(whiteScore < 0, "White without rook should have negative score");
        assertEquals(-blackScore, whiteScore);
    }

    @Test
    @DisplayName("評価は対称性を持つ（視点が変われば符号が反転する）")
    void shouldBeSymmetric() {
        Board board = Board.createInitialBoard();

        board = board.applyMove(Move.normalMove(
            new Position(6, 4), new Position(5, 4), PieceType.PAWN, PlayerColor.BLACK
        ));

        int blackPerspective = evaluator.evaluate(board, PlayerColor.BLACK);
        int whitePerspective = evaluator.evaluate(board, PlayerColor.WHITE);

        assertEquals(blackPerspective, -whitePerspective,
            "Evaluation should be symmetric (opposite signs for opposite perspectives)");
    }

    @Test
    @DisplayName("駒得した場合の評価が高い")
    void shouldEvaluateHigherAfterCapture() {
        Board boardBefore = new Board();
        boardBefore.placePiece(new Piece(PieceType.KING, PlayerColor.BLACK, new Position(8, 4)));
        boardBefore.placePiece(new Piece(PieceType.KING, PlayerColor.WHITE, new Position(0, 4)));
        boardBefore.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(4, 4)));
        boardBefore.placePiece(new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(3, 4)));

        int scoreBefore = evaluator.evaluate(boardBefore, PlayerColor.BLACK);

        Board boardAfter = boardBefore.applyMove(
            Move.normalMove(new Position(4, 4), new Position(3, 4), PieceType.ROOK, PlayerColor.BLACK)
        );
        int scoreAfter = evaluator.evaluate(boardAfter, PlayerColor.BLACK);

        assertTrue(scoreAfter > scoreBefore,
            "Score should increase after capturing opponent's piece");
    }
}
