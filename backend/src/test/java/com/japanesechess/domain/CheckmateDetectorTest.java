package com.japanesechess.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class CheckmateDetectorTest {

    private CheckDetector checkDetector;

    @BeforeEach
    void setUp() {
        checkDetector = new CheckDetector();
    }

    @Test
    @DisplayName("Not checkmate - king not in check")
    void testNotCheckmateWhenNotInCheck() {
        Board board = Board.createInitialBoard();

        assertFalse(checkDetector.isCheckmate(board, PlayerColor.BLACK));
        assertFalse(checkDetector.isCheckmate(board, PlayerColor.WHITE));
    }

    @Test
    @DisplayName("Not checkmate - king in check but can escape")
    void testNotCheckmateWhenKingCanEscape() {
        Board board = new Board();

        // Black king at (4, 4)
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(4, 4));
        board.placePiece(blackKing);

        // White rook at (4, 7) - attacking horizontally
        Piece whiteRook = new Piece(PieceType.ROOK, PlayerColor.WHITE, new Position(4, 7));
        board.placePiece(whiteRook);

        // King is in check but can move to (3, 4), (5, 4), (3, 3), etc.
        assertTrue(checkDetector.isInCheck(board, PlayerColor.BLACK));
        assertFalse(checkDetector.isCheckmate(board, PlayerColor.BLACK));
    }

    @Test
    @DisplayName("Not checkmate - can block the attack")
    void testNotCheckmateWhenCanBlock() {
        Board board = new Board();

        // Black king at (4, 4)
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(4, 4));
        board.placePiece(blackKing);

        // White rook at (4, 7) - attacking horizontally
        Piece whiteRook = new Piece(PieceType.ROOK, PlayerColor.WHITE, new Position(4, 7));
        board.placePiece(whiteRook);

        // Black gold that can block at (4, 5)
        Piece blackGold = new Piece(PieceType.GOLD, PlayerColor.BLACK, new Position(3, 5));
        board.placePiece(blackGold);

        assertTrue(checkDetector.isInCheck(board, PlayerColor.BLACK));
        assertFalse(checkDetector.isCheckmate(board, PlayerColor.BLACK));
    }

    @Test
    @DisplayName("Not checkmate - can capture attacking piece")
    void testNotCheckmateWhenCanCaptureAttacker() {
        Board board = new Board();

        // Black king at (4, 4)
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(4, 4));
        board.placePiece(blackKing);

        // White pawn at (3, 4) - attacking king
        Piece whitePawn = new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(3, 4));
        board.placePiece(whitePawn);

        // King can capture the pawn
        assertTrue(checkDetector.isInCheck(board, PlayerColor.BLACK));
        assertFalse(checkDetector.isCheckmate(board, PlayerColor.BLACK));
    }

    @Test
    @DisplayName("Checkmate - back rank mate with rook")
    void testCheckmateBackRankMate() {
        Board board = new Board();

        // Black king at (8, 4) - back rank
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(8, 4));
        board.placePiece(blackKing);

        // Black pawns blocking escape at (7, 3), (7, 4), (7, 5)
        board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(7, 3)));
        board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(7, 4)));
        board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(7, 5)));

        // White rook at (8, 0) - delivering checkmate
        Piece whiteRook = new Piece(PieceType.ROOK, PlayerColor.WHITE, new Position(8, 0));
        board.placePiece(whiteRook);

        assertTrue(checkDetector.isInCheck(board, PlayerColor.BLACK));
        assertTrue(checkDetector.isCheckmate(board, PlayerColor.BLACK));
    }

    @Test
    @DisplayName("Checkmate - surrounded king")
    void testCheckmateSurroundedKing() {
        Board board = new Board();

        // Black king at (4, 4)
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(4, 4));
        board.placePiece(blackKing);

        // Surround king with WHITE pieces (not own pieces)
        board.placePiece(new Piece(PieceType.GOLD, PlayerColor.WHITE, new Position(3, 3)));
        board.placePiece(new Piece(PieceType.GOLD, PlayerColor.WHITE, new Position(3, 4)));
        board.placePiece(new Piece(PieceType.GOLD, PlayerColor.WHITE, new Position(3, 5)));
        board.placePiece(new Piece(PieceType.GOLD, PlayerColor.WHITE, new Position(4, 3)));
        board.placePiece(new Piece(PieceType.GOLD, PlayerColor.WHITE, new Position(4, 5)));
        board.placePiece(new Piece(PieceType.GOLD, PlayerColor.WHITE, new Position(5, 3)));
        board.placePiece(new Piece(PieceType.GOLD, PlayerColor.WHITE, new Position(5, 4)));
        board.placePiece(new Piece(PieceType.GOLD, PlayerColor.WHITE, new Position(5, 5)));

        // White rook attacking - but actually the golds already cover all squares
        // The gold at (4,5) is giving check
        assertTrue(checkDetector.isInCheck(board, PlayerColor.BLACK));
        assertTrue(checkDetector.isCheckmate(board, PlayerColor.BLACK));
    }

    @Test
    @DisplayName("Checkmate - ladder mate")
    void testCheckmateLadderMate() {
        Board board = new Board();

        // Black king at (8, 4) - edge of board
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(8, 4));
        board.placePiece(blackKing);

        // White rooks creating ladder mate
        // Rook 1 at (7, 0) - attacks entire row 7
        Piece whiteRook1 = new Piece(PieceType.ROOK, PlayerColor.WHITE, new Position(7, 0));
        board.placePiece(whiteRook1);

        // Rook 2 at (6, 1) - attacks entire row 6 and gives check via column
        Piece whiteRook2 = new Piece(PieceType.ROOK, PlayerColor.WHITE, new Position(8, 8));
        board.placePiece(whiteRook2);

        assertTrue(checkDetector.isInCheck(board, PlayerColor.BLACK));
        assertTrue(checkDetector.isCheckmate(board, PlayerColor.BLACK));
    }

    @Test
    @DisplayName("Not checkmate - can drop piece to block")
    void testNotCheckmateWhenCanDropToBlock() {
        Board board = new Board();

        // Black king at (4, 4)
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(4, 4));
        board.placePiece(blackKing);

        // Surround king except one square
        board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(3, 3)));
        board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(3, 4)));
        board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(3, 5)));
        board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(4, 3)));
        board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(5, 3)));
        board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(5, 4)));
        board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(5, 5)));

        // White rook attacking
        Piece whiteRook = new Piece(PieceType.ROOK, PlayerColor.WHITE, new Position(4, 8));
        board.placePiece(whiteRook);

        // Black has a gold in hand that can be dropped to block
        board.getCapturedPieces(PlayerColor.BLACK).add(PieceType.GOLD);

        assertTrue(checkDetector.isInCheck(board, PlayerColor.BLACK));
        assertFalse(checkDetector.isCheckmate(board, PlayerColor.BLACK));
    }

    @Test
    @DisplayName("Checkmate - knight checkmate")
    void testCheckmateKnightMate() {
        Board board = new Board();

        // Black king at (8, 4) - back corner
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(8, 4));
        board.placePiece(blackKing);

        // White knight at (6, 3) - delivering checkmate
        Piece whiteKnight = new Piece(PieceType.KNIGHT, PlayerColor.WHITE, new Position(6, 3));
        board.placePiece(whiteKnight);

        // White gold at (7, 3) - covering escape square
        Piece whiteGold = new Piece(PieceType.GOLD, PlayerColor.WHITE, new Position(7, 3));
        board.placePiece(whiteGold);

        // White pieces covering other escape squares
        board.placePiece(new Piece(PieceType.GOLD, PlayerColor.WHITE, new Position(7, 4)));
        board.placePiece(new Piece(PieceType.SILVER, PlayerColor.WHITE, new Position(7, 5)));

        assertTrue(checkDetector.isInCheck(board, PlayerColor.BLACK));
        assertTrue(checkDetector.isCheckmate(board, PlayerColor.BLACK));
    }

    @Test
    @DisplayName("Performance test - checkmate detection should be fast")
    void testCheckmatePerformance() {
        Board board = new Board();

        // Complex position with many pieces
        board.placePiece(new Piece(PieceType.KING, PlayerColor.BLACK, new Position(8, 4)));
        board.placePiece(new Piece(PieceType.ROOK, PlayerColor.WHITE, new Position(8, 0)));

        // Add more pieces to increase complexity
        for (int i = 0; i < 5; i++) {
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(7, i)));
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(2, i)));
        }

        long startTime = System.currentTimeMillis();
        boolean result = checkDetector.isCheckmate(board, PlayerColor.BLACK);
        long endTime = System.currentTimeMillis();

        // Should complete in reasonable time (< 1 second)
        assertTrue(endTime - startTime < 1000, "Checkmate detection took too long: " + (endTime - startTime) + "ms");
    }
}
