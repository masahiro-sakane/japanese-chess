package com.japanesechess.ai;

import com.japanesechess.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MinimaxEngine - ミニマックスAIエンジン")
class MinimaxEngineTest {

    private MinimaxEngine engine;

    @BeforeEach
    void setUp() {
        engine = new MinimaxEngine();
    }

    @Test
    @DisplayName("初期配置から初級AIが手を返す")
    void shouldReturnMoveFromInitialPositionBeginnerDifficulty() {
        Board board = Board.createInitialBoard();

        Optional<Move> move = engine.findBestMove(board, PlayerColor.BLACK, AiDifficulty.BEGINNER);

        assertTrue(move.isPresent(), "Beginner AI should return a move from initial position");
        assertEquals(PlayerColor.BLACK, move.get().getPlayer());
    }

    @Test
    @DisplayName("初期配置から中級AIが手を返す")
    void shouldReturnMoveFromInitialPositionIntermediateDifficulty() {
        Board board = Board.createInitialBoard();

        Optional<Move> move = engine.findBestMove(board, PlayerColor.BLACK, AiDifficulty.INTERMEDIATE);

        assertTrue(move.isPresent(), "Intermediate AI should return a move from initial position");
        assertEquals(PlayerColor.BLACK, move.get().getPlayer());
    }

    @Test
    @DisplayName("AIの手は合法手")
    void shouldReturnLegalMove() {
        Board board = Board.createInitialBoard();
        MoveValidator validator = new MoveValidator();

        Optional<Move> move = engine.findBestMove(board, PlayerColor.BLACK, AiDifficulty.BEGINNER);

        assertTrue(move.isPresent());
        assertTrue(validator.isValidMove(board, move.get()),
            "AI move should be a valid move");
    }

    @Test
    @DisplayName("AIは明らかに有利な手（駒を取る手）を選ぶ（中級以上）")
    void shouldPreferCaptureMoveAtIntermediateLevel() {
        Board board = new Board();
        board.placePiece(new Piece(PieceType.KING, PlayerColor.BLACK, new Position(8, 4)));
        board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(4, 4)));
        board.placePiece(new Piece(PieceType.KING, PlayerColor.WHITE, new Position(0, 4)));
        board.placePiece(new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(3, 4)));

        Optional<Move> move = engine.findBestMove(board, PlayerColor.BLACK, AiDifficulty.INTERMEDIATE);

        assertTrue(move.isPresent());
        Move aiMove = move.get();
        assertTrue(
            aiMove.getTo().getRow() == 3 && aiMove.getTo().getColumn() == 4,
            "Intermediate AI should capture the pawn with rook"
        );
    }

    @Test
    @DisplayName("合法手がない場合はemptyを返す")
    void shouldReturnEmptyWhenNoLegalMoves() {
        Board board = new Board();

        Optional<Move> move = engine.findBestMove(board, PlayerColor.BLACK, AiDifficulty.BEGINNER);

        assertFalse(move.isPresent(), "Should return empty when no pieces on board");
    }

    @Test
    @DisplayName("初級はランダム、中級・上級はミニマックスで手を選ぶ")
    void shouldUseDifferentStrategyByDifficulty() {
        assertEquals(0, AiDifficulty.BEGINNER.getSearchDepth());
        assertEquals(2, AiDifficulty.INTERMEDIATE.getSearchDepth());
        assertEquals(4, AiDifficulty.ADVANCED.getSearchDepth());
    }
}
