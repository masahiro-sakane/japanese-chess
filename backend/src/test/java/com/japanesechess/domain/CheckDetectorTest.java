package com.japanesechess.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class CheckDetectorTest {

    private CheckDetector checkDetector;

    @BeforeEach
    void setUp() {
        checkDetector = new CheckDetector();
    }

    @Test
    @DisplayName("Initial board state - no check")
    void testInitialBoardNoCheck() {
        Board board = Board.createInitialBoard();

        assertFalse(checkDetector.isInCheck(board, PlayerColor.BLACK));
        assertFalse(checkDetector.isInCheck(board, PlayerColor.WHITE));
    }

    @Test
    @DisplayName("King in check by rook - horizontal attack")
    void testKingInCheckByRookHorizontal() {
        Board board = new Board();

        // Black king at (4, 4)
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(4, 4));
        board.placePiece(blackKing);

        // White rook at (4, 7) - same row, can attack horizontally
        Piece whiteRook = new Piece(PieceType.ROOK, PlayerColor.WHITE, new Position(4, 7));
        board.placePiece(whiteRook);

        assertTrue(checkDetector.isInCheck(board, PlayerColor.BLACK));
        assertFalse(checkDetector.isInCheck(board, PlayerColor.WHITE));
    }

    @Test
    @DisplayName("King in check by rook - vertical attack")
    void testKingInCheckByRookVertical() {
        Board board = new Board();

        // Black king at (4, 4)
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(4, 4));
        board.placePiece(blackKing);

        // White rook at (7, 4) - same column, can attack vertically
        Piece whiteRook = new Piece(PieceType.ROOK, PlayerColor.WHITE, new Position(7, 4));
        board.placePiece(whiteRook);

        assertTrue(checkDetector.isInCheck(board, PlayerColor.BLACK));
    }

    @Test
    @DisplayName("King in check by bishop - diagonal attack")
    void testKingInCheckByBishop() {
        Board board = new Board();

        // Black king at (4, 4)
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(4, 4));
        board.placePiece(blackKing);

        // White bishop at (6, 6) - diagonal attack
        Piece whiteBishop = new Piece(PieceType.BISHOP, PlayerColor.WHITE, new Position(6, 6));
        board.placePiece(whiteBishop);

        assertTrue(checkDetector.isInCheck(board, PlayerColor.BLACK));
    }

    @Test
    @DisplayName("King in check by gold general")
    void testKingInCheckByGoldGeneral() {
        Board board = new Board();

        // Black king at (4, 4)
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(4, 4));
        board.placePiece(blackKing);

        // White gold at (5, 4) - one square below, within gold's range
        Piece whiteGold = new Piece(PieceType.GOLD, PlayerColor.WHITE, new Position(5, 4));
        board.placePiece(whiteGold);

        assertTrue(checkDetector.isInCheck(board, PlayerColor.BLACK));
    }

    @Test
    @DisplayName("King in check by pawn")
    void testKingInCheckByPawn() {
        Board board = new Board();

        // Black king at (4, 4)
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(4, 4));
        board.placePiece(blackKing);

        // White pawn at (3, 4) - one square ABOVE (white moves forward toward higher rows)
        Piece whitePawn = new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(3, 4));
        board.placePiece(whitePawn);

        assertTrue(checkDetector.isInCheck(board, PlayerColor.BLACK));
    }

    @Test
    @DisplayName("King in check by knight")
    void testKingInCheckByKnight() {
        Board board = new Board();

        // Black king at (4, 4)
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(4, 4));
        board.placePiece(blackKing);

        // White knight at (2, 3) - knight moves 2 forward, 1 sideways
        // From (2,3), white knight can jump to (4,2) or (4,4) - the king position!
        Piece whiteKnight = new Piece(PieceType.KNIGHT, PlayerColor.WHITE, new Position(2, 3));
        board.placePiece(whiteKnight);

        assertTrue(checkDetector.isInCheck(board, PlayerColor.BLACK));
    }

    @Test
    @DisplayName("No check - pieces blocked")
    void testNoCheckWhenBlocked() {
        Board board = new Board();

        // Black king at (4, 4)
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(4, 4));
        board.placePiece(blackKing);

        // White rook at (4, 7) - same row
        Piece whiteRook = new Piece(PieceType.ROOK, PlayerColor.WHITE, new Position(4, 7));
        board.placePiece(whiteRook);

        // Black pawn blocking at (4, 5)
        Piece blackPawn = new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(4, 5));
        board.placePiece(blackPawn);

        assertFalse(checkDetector.isInCheck(board, PlayerColor.BLACK));
    }

    @Test
    @DisplayName("Move would leave king in check - illegal")
    void testMoveWouldLeaveKingInCheck() {
        Board board = new Board();

        // Black king at (4, 4)
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(4, 4));
        board.placePiece(blackKing);

        // Black pawn blocking check at (4, 5)
        Piece blackPawn = new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(4, 5));
        board.placePiece(blackPawn);

        // White rook at (4, 7) - would attack king if pawn moves
        Piece whiteRook = new Piece(PieceType.ROOK, PlayerColor.WHITE, new Position(4, 7));
        board.placePiece(whiteRook);

        // Try to move pawn forward (this would expose king to check)
        Move move = Move.normalMove(
            new Position(4, 5),
            new Position(3, 5), // Move pawn away from column 4
            PieceType.PAWN,
            PlayerColor.BLACK
        );

        // This should return true - the move would leave king in check
        assertTrue(checkDetector.wouldBeInCheckAfterMove(board, move));
    }

    @Test
    @DisplayName("Move resolves check - legal")
    void testMoveResolvesCheck() {
        Board board = new Board();

        // Black king at (4, 4) - in check
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(4, 4));
        board.placePiece(blackKing);

        // White rook at (4, 7) - attacking king
        Piece whiteRook = new Piece(PieceType.ROOK, PlayerColor.WHITE, new Position(4, 7));
        board.placePiece(whiteRook);

        // King in check initially
        assertTrue(checkDetector.isInCheck(board, PlayerColor.BLACK));

        // King moves to (3, 4) to escape check
        Move move = Move.normalMove(
            new Position(4, 4),
            new Position(3, 4),
            PieceType.KING,
            PlayerColor.BLACK
        );

        // This should return false - the move gets king out of check
        assertFalse(checkDetector.wouldBeInCheckAfterMove(board, move));
    }

    @Test
    @DisplayName("King not on board - should not crash")
    void testKingNotOnBoard() {
        Board board = new Board();

        // Empty board - no king
        assertFalse(checkDetector.isInCheck(board, PlayerColor.BLACK));
        assertFalse(checkDetector.isInCheck(board, PlayerColor.WHITE));
    }

    @Test
    @DisplayName("Promoted piece can give check")
    void testPromotedPieceCanGiveCheck() {
        Board board = new Board();

        // Black king at (4, 4)
        Piece blackKing = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(4, 4));
        board.placePiece(blackKing);

        // White promoted rook (龍王) at (5, 5) - can move one square diagonally
        Piece whitePromotedRook = new Piece(PieceType.PROMOTED_ROOK, PlayerColor.WHITE, new Position(5, 5));
        board.placePiece(whitePromotedRook);

        assertTrue(checkDetector.isInCheck(board, PlayerColor.BLACK));
    }
}
