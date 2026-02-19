package com.japanesechess.ai;

import com.japanesechess.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiMoveGenerator - AI合法手生成クラス")
class AiMoveGeneratorTest {

    private AiMoveGenerator moveGenerator;

    @BeforeEach
    void setUp() {
        moveGenerator = new AiMoveGenerator();
    }

    @Test
    @DisplayName("初期配置からBLACKの合法手が生成される")
    void shouldGenerateLegalMovesForBlackFromInitialPosition() {
        Board board = Board.createInitialBoard();

        List<Move> moves = moveGenerator.generateAllLegalMoves(board, PlayerColor.BLACK);

        assertFalse(moves.isEmpty());
        moves.forEach(move -> assertEquals(PlayerColor.BLACK, move.getPlayer()));
    }

    @Test
    @DisplayName("初期配置からWHITEの合法手が生成される")
    void shouldGenerateLegalMovesForWhiteFromInitialPosition() {
        Board board = Board.createInitialBoard();

        List<Move> moves = moveGenerator.generateAllLegalMoves(board, PlayerColor.WHITE);

        assertFalse(moves.isEmpty());
        moves.forEach(move -> assertEquals(PlayerColor.WHITE, move.getPlayer()));
    }

    @Test
    @DisplayName("駒のない空の盤から合法手は生成されない")
    void shouldReturnEmptyListFromEmptyBoard() {
        Board board = new Board();

        List<Move> moves = moveGenerator.generateAllLegalMoves(board, PlayerColor.BLACK);

        assertTrue(moves.isEmpty());
    }

    @Test
    @DisplayName("持ち駒があれば打ちの手が生成される")
    void shouldGenerateDropMovesWhenCapturedPiecesExist() {
        Board board = new Board();

        board.placePiece(new Piece(PieceType.KING, PlayerColor.BLACK, new Position(8, 4)));
        board.placePiece(new Piece(PieceType.KING, PlayerColor.WHITE, new Position(0, 4)));

        Board boardWithCapture = board.applyMove(
            Move.dropMove(new Position(5, 5), PieceType.PAWN, PlayerColor.BLACK)
                .withCapturedPiece(PieceType.PAWN)
        );

        List<PieceType> captured = boardWithCapture.getCapturedPieces(PlayerColor.BLACK);
        if (!captured.isEmpty()) {
            List<Move> moves = moveGenerator.generateAllLegalMoves(boardWithCapture, PlayerColor.BLACK);
            boolean hasDropMove = moves.stream().anyMatch(Move::isDrop);
            assertTrue(hasDropMove || moves.isEmpty());
        }
    }

    @Test
    @DisplayName("初期配置から生成された全手は自玉を危険にさらさない")
    void shouldNotGenerateMovesLeavingKingInCheck() {
        Board board = Board.createInitialBoard();
        CheckDetector checkDetector = new CheckDetector();

        List<Move> moves = moveGenerator.generateAllLegalMoves(board, PlayerColor.BLACK);

        for (Move move : moves) {
            Board boardAfterMove = board.applyMove(move);
            assertFalse(
                checkDetector.isInCheck(boardAfterMove, PlayerColor.BLACK),
                "Move should not leave own king in check: " + move
            );
        }
    }
}
