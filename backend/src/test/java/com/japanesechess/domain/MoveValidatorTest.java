package com.japanesechess.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MoveValidator - 移動ルール検証クラス")
class MoveValidatorTest {

    private MoveValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MoveValidator();
    }

    @Nested
    @DisplayName("基本的なバリデーションのテスト")
    class BasicValidationTests {

        @Test
        @DisplayName("移動元に駒がない場合は無効")
        void shouldReturnFalseWhenNoPieceAtFrom() {
            Board board = new Board();
            Move move = Move.normalMove(new Position(4, 4), new Position(3, 4), PieceType.PAWN, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("自分の駒でない場合は無効")
        void shouldReturnFalseWhenPieceOwnerDoesNotMatch() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(3, 4), PieceType.PAWN, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("移動先に自分の駒がある場合は無効")
        void shouldReturnFalseWhenTargetHasOwnPiece() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(4, 4)));
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(3, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(3, 4), PieceType.ROOK, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("移動先に相手の駒がある場合は有効(駒を取れる)")
        void shouldReturnTrueWhenTargetHasOpponentPiece() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(4, 4)));
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(3, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(3, 4), PieceType.ROOK, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }
    }

    @Nested
    @DisplayName("歩兵の移動テスト")
    class PawnMoveTests {

        @Test
        @DisplayName("先手の歩は前に1マス進める")
        void shouldAllowBlackPawnToMoveForwardOneSquare() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(6, 4)));
            Move move = Move.normalMove(new Position(6, 4), new Position(5, 4), PieceType.PAWN, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("後手の歩は前に1マス進める")
        void shouldAllowWhitePawnToMoveForwardOneSquare() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(2, 4)));
            Move move = Move.normalMove(new Position(2, 4), new Position(3, 4), PieceType.PAWN, PlayerColor.WHITE);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("歩は横に移動できない")
        void shouldNotAllowPawnToMoveSideways() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(6, 4)));
            Move move = Move.normalMove(new Position(6, 4), new Position(6, 5), PieceType.PAWN, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("歩は後ろに移動できない")
        void shouldNotAllowPawnToMoveBackward() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(5, 4)));
            Move move = Move.normalMove(new Position(5, 4), new Position(6, 4), PieceType.PAWN, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("歩は2マス進めない")
        void shouldNotAllowPawnToMoveTwoSquares() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(6, 4)));
            Move move = Move.normalMove(new Position(6, 4), new Position(4, 4), PieceType.PAWN, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }
    }

    @Nested
    @DisplayName("金将の移動テスト")
    class GoldMoveTests {

        @Test
        @DisplayName("金将は前に1マス進める")
        void shouldAllowGoldToMoveForward() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.GOLD, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(3, 4), PieceType.GOLD, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("金将は斜め前に1マス進める")
        void shouldAllowGoldToMoveDiagonalForward() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.GOLD, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(3, 5), PieceType.GOLD, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("金将は横に1マス進める")
        void shouldAllowGoldToMoveSideways() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.GOLD, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(4, 5), PieceType.GOLD, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("金将は後ろに1マス進める")
        void shouldAllowGoldToMoveBackward() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.GOLD, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(5, 4), PieceType.GOLD, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("金将は斜め後ろに進めない")
        void shouldNotAllowGoldToMoveDiagonalBackward() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.GOLD, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(5, 5), PieceType.GOLD, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }
    }

    @Nested
    @DisplayName("銀将の移動テスト")
    class SilverMoveTests {

        @Test
        @DisplayName("銀将は前に1マス進める")
        void shouldAllowSilverToMoveForward() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.SILVER, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(3, 4), PieceType.SILVER, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("銀将は斜め前に1マス進める")
        void shouldAllowSilverToMoveDiagonalForward() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.SILVER, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(3, 5), PieceType.SILVER, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("銀将は斜め後ろに1マス進める")
        void shouldAllowSilverToMoveDiagonalBackward() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.SILVER, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(5, 5), PieceType.SILVER, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("銀将は横に進めない")
        void shouldNotAllowSilverToMoveSideways() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.SILVER, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(4, 5), PieceType.SILVER, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("銀将は真後ろに進めない")
        void shouldNotAllowSilverToMoveDirectlyBackward() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.SILVER, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(5, 4), PieceType.SILVER, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }
    }

    @Nested
    @DisplayName("桂馬の移動テスト")
    class KnightMoveTests {

        @Test
        @DisplayName("先手の桂馬は前方にL字型に移動できる")
        void shouldAllowBlackKnightToJumpForward() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.KNIGHT, PlayerColor.BLACK, new Position(6, 4)));
            Move move = Move.normalMove(new Position(6, 4), new Position(4, 5), PieceType.KNIGHT, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("先手の桂馬は前方左にも移動できる")
        void shouldAllowBlackKnightToJumpForwardLeft() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.KNIGHT, PlayerColor.BLACK, new Position(6, 4)));
            Move move = Move.normalMove(new Position(6, 4), new Position(4, 3), PieceType.KNIGHT, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("後手の桂馬は前方(後手から見て)にL字型に移動できる")
        void shouldAllowWhiteKnightToJumpForward() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.KNIGHT, PlayerColor.WHITE, new Position(2, 4)));
            Move move = Move.normalMove(new Position(2, 4), new Position(4, 5), PieceType.KNIGHT, PlayerColor.WHITE);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("桂馬は後ろに跳べない")
        void shouldNotAllowKnightToJumpBackward() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.KNIGHT, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(6, 5), PieceType.KNIGHT, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("桂馬は駒を飛び越えられる")
        void shouldAllowKnightToJumpOverPieces() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.KNIGHT, PlayerColor.BLACK, new Position(6, 4)));
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(5, 4)));
            Move move = Move.normalMove(new Position(6, 4), new Position(4, 5), PieceType.KNIGHT, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }
    }

    @Nested
    @DisplayName("香車の移動テスト")
    class LanceMoveTests {

        @Test
        @DisplayName("先手の香車は前方に何マスでも進める")
        void shouldAllowBlackLanceToMoveForwardAnyDistance() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.LANCE, PlayerColor.BLACK, new Position(8, 0)));
            Move move = Move.normalMove(new Position(8, 0), new Position(3, 0), PieceType.LANCE, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("後手の香車は前方(後手から見て)に何マスでも進める")
        void shouldAllowWhiteLanceToMoveForwardAnyDistance() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.LANCE, PlayerColor.WHITE, new Position(0, 0)));
            Move move = Move.normalMove(new Position(0, 0), new Position(5, 0), PieceType.LANCE, PlayerColor.WHITE);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("香車は横に進めない")
        void shouldNotAllowLanceToMoveSideways() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.LANCE, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(4, 5), PieceType.LANCE, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("香車は後ろに進めない")
        void shouldNotAllowLanceToMoveBackward() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.LANCE, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(5, 4), PieceType.LANCE, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("香車は駒を飛び越えられない")
        void shouldNotAllowLanceToJumpOverPieces() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.LANCE, PlayerColor.BLACK, new Position(8, 0)));
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(6, 0)));
            Move move = Move.normalMove(new Position(8, 0), new Position(4, 0), PieceType.LANCE, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("香車は途中の駒まで進める")
        void shouldAllowLanceToMoveUpToBlockingPiece() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.LANCE, PlayerColor.BLACK, new Position(8, 0)));
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(5, 0)));
            Move move = Move.normalMove(new Position(8, 0), new Position(5, 0), PieceType.LANCE, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }
    }

    @Nested
    @DisplayName("飛車の移動テスト")
    class RookMoveTests {

        @Test
        @DisplayName("飛車は縦に何マスでも進める")
        void shouldAllowRookToMoveVerticallyAnyDistance() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(7, 1)));
            Move move = Move.normalMove(new Position(7, 1), new Position(2, 1), PieceType.ROOK, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("飛車は横に何マスでも進める")
        void shouldAllowRookToMoveHorizontallyAnyDistance() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(4, 1)));
            Move move = Move.normalMove(new Position(4, 1), new Position(4, 8), PieceType.ROOK, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("飛車は斜めに進めない")
        void shouldNotAllowRookToMoveDiagonally() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(2, 6), PieceType.ROOK, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("飛車は駒を飛び越えられない")
        void shouldNotAllowRookToJumpOverPieces() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(7, 1)));
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(5, 1)));
            Move move = Move.normalMove(new Position(7, 1), new Position(3, 1), PieceType.ROOK, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }
    }

    @Nested
    @DisplayName("角行の移動テスト")
    class BishopMoveTests {

        @Test
        @DisplayName("角行は斜めに何マスでも進める")
        void shouldAllowBishopToMoveDiagonallyAnyDistance() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.BISHOP, PlayerColor.BLACK, new Position(7, 7)));
            Move move = Move.normalMove(new Position(7, 7), new Position(3, 3), PieceType.BISHOP, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("角行は逆斜めにも進める")
        void shouldAllowBishopToMoveOppositeDiagonally() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.BISHOP, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(2, 6), PieceType.BISHOP, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("角行は縦に進めない")
        void shouldNotAllowBishopToMoveVertically() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.BISHOP, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(2, 4), PieceType.BISHOP, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("角行は横に進めない")
        void shouldNotAllowBishopToMoveHorizontally() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.BISHOP, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(4, 7), PieceType.BISHOP, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("角行は駒を飛び越えられない")
        void shouldNotAllowBishopToJumpOverPieces() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.BISHOP, PlayerColor.BLACK, new Position(7, 7)));
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(5, 5)));
            Move move = Move.normalMove(new Position(7, 7), new Position(3, 3), PieceType.BISHOP, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }
    }

    @Nested
    @DisplayName("玉将の移動テスト")
    class KingMoveTests {

        @Test
        @DisplayName("玉将は全方向に1マス進める")
        void shouldAllowKingToMoveOneSquareInAnyDirection() {
            Board board = new Board();
            Position kingPosition = new Position(4, 4);
            board.placePiece(new Piece(PieceType.KING, PlayerColor.BLACK, kingPosition));

            int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1}, {0, 1},
                {1, -1}, {1, 0}, {1, 1}
            };

            for (int[] dir : directions) {
                Position target = new Position(4 + dir[0], 4 + dir[1]);
                Move move = Move.normalMove(kingPosition, target, PieceType.KING, PlayerColor.BLACK);
                assertTrue(validator.isValidMove(board, move),
                    "King should be able to move to " + target);
            }
        }

        @Test
        @DisplayName("玉将は2マス以上進めない")
        void shouldNotAllowKingToMoveTwoSquares() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.KING, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(2, 4), PieceType.KING, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }
    }

    @Nested
    @DisplayName("龍王の移動テスト")
    class PromotedRookMoveTests {

        @Test
        @DisplayName("龍王は飛車の動きができる")
        void shouldAllowPromotedRookToMoveAsRook() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PROMOTED_ROOK, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(4, 8), PieceType.PROMOTED_ROOK, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("龍王は斜め1マスも進める")
        void shouldAllowPromotedRookToMoveDiagonallyOneSquare() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PROMOTED_ROOK, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(3, 5), PieceType.PROMOTED_ROOK, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("龍王は斜め2マス以上は進めない")
        void shouldNotAllowPromotedRookToMoveDiagonallyTwoOrMore() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PROMOTED_ROOK, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(2, 6), PieceType.PROMOTED_ROOK, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }
    }

    @Nested
    @DisplayName("龍馬の移動テスト")
    class PromotedBishopMoveTests {

        @Test
        @DisplayName("龍馬は角行の動きができる")
        void shouldAllowPromotedBishopToMoveAsBishop() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PROMOTED_BISHOP, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(1, 7), PieceType.PROMOTED_BISHOP, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("龍馬は縦横1マスも進める")
        void shouldAllowPromotedBishopToMoveVerticallyOrHorizontallyOneSquare() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PROMOTED_BISHOP, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(3, 4), PieceType.PROMOTED_BISHOP, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("龍馬は縦横2マス以上は進めない")
        void shouldNotAllowPromotedBishopToMoveVerticallyOrHorizontallyTwoOrMore() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PROMOTED_BISHOP, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(2, 4), PieceType.PROMOTED_BISHOP, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }
    }

    @Nested
    @DisplayName("成り駒(金将の動き)のテスト")
    class PromotedPiecesGoldMoveTests {

        @Test
        @DisplayName("と金は金将と同じ動きができる")
        void shouldAllowPromotedPawnToMoveAsGold() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PROMOTED_PAWN, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(3, 5), PieceType.PROMOTED_PAWN, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("成桂は金将と同じ動きができる")
        void shouldAllowPromotedKnightToMoveAsGold() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PROMOTED_KNIGHT, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(4, 5), PieceType.PROMOTED_KNIGHT, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("成香は金将と同じ動きができる")
        void shouldAllowPromotedLanceToMoveAsGold() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PROMOTED_LANCE, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(5, 4), PieceType.PROMOTED_LANCE, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("成銀は金将と同じ動きができる")
        void shouldAllowPromotedSilverToMoveAsGold() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PROMOTED_SILVER, PlayerColor.BLACK, new Position(4, 4)));
            Move move = Move.normalMove(new Position(4, 4), new Position(3, 4), PieceType.PROMOTED_SILVER, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }
    }

    @Nested
    @DisplayName("成りのバリデーションテスト")
    class PromotionValidationTests {

        @Test
        @DisplayName("敵陣で成りフラグが立っている場合は有効")
        void shouldAllowPromotionInEnemyTerritory() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(3, 4)));
            Move move = Move.promoteMove(new Position(3, 4), new Position(2, 4), PieceType.PAWN, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("敵陣外で成りフラグが立っている場合は無効")
        void shouldNotAllowPromotionOutsideEnemyTerritory() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(5, 4)));
            Move move = Move.promoteMove(new Position(5, 4), new Position(4, 4), PieceType.PAWN, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("成らなければならない位置で成らない場合は無効")
        void shouldNotAllowNotPromotingWhenMustPromote() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(1, 4)));
            Move move = Move.normalMove(new Position(1, 4), new Position(0, 4), PieceType.PAWN, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("成らなければならない位置で成る場合は有効")
        void shouldAllowPromotingWhenMustPromote() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(1, 4)));
            Move move = Move.promoteMove(new Position(1, 4), new Position(0, 4), PieceType.PAWN, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(board, move));
        }
    }

    @Nested
    @DisplayName("駒を打つ(ドロップ)のバリデーションテスト")
    class DropValidationTests {

        @Test
        @DisplayName("持ち駒がない場合は打てない")
        void shouldNotAllowDropWithoutCapturedPiece() {
            Board board = new Board();
            Move move = Move.dropMove(new Position(4, 4), PieceType.PAWN, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(board, move));
        }

        @Test
        @DisplayName("駒がある位置には打てない")
        void shouldNotAllowDropOnOccupiedSquare() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(5, 5)));
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(4, 5)));
            Move captureMove = Move.normalMove(new Position(5, 5), new Position(4, 5), PieceType.ROOK, PlayerColor.BLACK);
            Board boardWithCaptured = board.applyMove(captureMove);

            board.placePiece(new Piece(PieceType.GOLD, PlayerColor.WHITE, new Position(4, 4)));
            Board boardWithOccupied = new Board();
            boardWithOccupied.placePiece(new Piece(PieceType.GOLD, PlayerColor.WHITE, new Position(4, 4)));
            boardWithOccupied.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(4, 5)));

            Move dropMove = Move.dropMove(new Position(4, 4), PieceType.PAWN, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(boardWithOccupied, dropMove));
        }

        @Test
        @DisplayName("先手の歩は最奥(row 0)に打てない")
        void shouldNotAllowBlackPawnDropAtRow0() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(5, 5)));
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(4, 5)));
            Move captureMove = Move.normalMove(new Position(5, 5), new Position(4, 5), PieceType.ROOK, PlayerColor.BLACK);
            Board boardWithCaptured = board.applyMove(captureMove);

            Move dropMove = Move.dropMove(new Position(0, 4), PieceType.PAWN, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(boardWithCaptured, dropMove));
        }

        @Test
        @DisplayName("後手の歩は最奥(row 8)に打てない")
        void shouldNotAllowWhitePawnDropAtRow8() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.ROOK, PlayerColor.WHITE, new Position(5, 5)));
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(4, 5)));
            Move captureMove = Move.normalMove(new Position(5, 5), new Position(4, 5), PieceType.ROOK, PlayerColor.WHITE);
            Board boardWithCaptured = board.applyMove(captureMove);

            Move dropMove = Move.dropMove(new Position(8, 4), PieceType.PAWN, PlayerColor.WHITE);

            assertFalse(validator.isValidMove(boardWithCaptured, dropMove));
        }

        @Test
        @DisplayName("先手の香車は最奥(row 0)に打てない")
        void shouldNotAllowBlackLanceDropAtRow0() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(5, 5)));
            board.placePiece(new Piece(PieceType.LANCE, PlayerColor.WHITE, new Position(4, 5)));
            Move captureMove = Move.normalMove(new Position(5, 5), new Position(4, 5), PieceType.ROOK, PlayerColor.BLACK);
            Board boardWithCaptured = board.applyMove(captureMove);

            Move dropMove = Move.dropMove(new Position(0, 4), PieceType.LANCE, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(boardWithCaptured, dropMove));
        }

        @Test
        @DisplayName("先手の桂馬はrow 0, 1には打てない")
        void shouldNotAllowBlackKnightDropAtRow0or1() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(5, 5)));
            board.placePiece(new Piece(PieceType.KNIGHT, PlayerColor.WHITE, new Position(4, 5)));
            Move captureMove = Move.normalMove(new Position(5, 5), new Position(4, 5), PieceType.ROOK, PlayerColor.BLACK);
            Board boardWithCaptured = board.applyMove(captureMove);

            Move dropMoveRow0 = Move.dropMove(new Position(0, 4), PieceType.KNIGHT, PlayerColor.BLACK);
            Move dropMoveRow1 = Move.dropMove(new Position(1, 4), PieceType.KNIGHT, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(boardWithCaptured, dropMoveRow0));
            assertFalse(validator.isValidMove(boardWithCaptured, dropMoveRow1));
        }

        @Test
        @DisplayName("二歩は打てない - 同じ列に歩がある場合")
        void shouldNotAllowNifuDropPawnInSameColumn() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(5, 4)));

            board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(7, 7)));
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(6, 7)));
            Move captureMove = Move.normalMove(new Position(7, 7), new Position(6, 7), PieceType.ROOK, PlayerColor.BLACK);
            Board boardWithCaptured = board.applyMove(captureMove);

            Move dropMove = Move.dropMove(new Position(3, 4), PieceType.PAWN, PlayerColor.BLACK);

            assertFalse(validator.isValidMove(boardWithCaptured, dropMove));
        }

        @Test
        @DisplayName("別の列には歩を打てる")
        void shouldAllowPawnDropInDifferentColumn() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(5, 4)));

            board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(7, 7)));
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(6, 7)));
            Move captureMove = Move.normalMove(new Position(7, 7), new Position(6, 7), PieceType.ROOK, PlayerColor.BLACK);
            Board boardWithCaptured = board.applyMove(captureMove);

            Move dropMove = Move.dropMove(new Position(3, 5), PieceType.PAWN, PlayerColor.BLACK);

            assertTrue(validator.isValidMove(boardWithCaptured, dropMove));
        }
    }
}
