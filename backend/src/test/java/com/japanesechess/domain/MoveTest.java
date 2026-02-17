package com.japanesechess.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Move - 移動を表すクラス")
class MoveTest {

    @Nested
    @DisplayName("normalMoveファクトリメソッドのテスト")
    class NormalMoveTests {

        @Test
        @DisplayName("通常の移動を作成できる")
        void shouldCreateNormalMove() {
            Position from = new Position(6, 4);
            Position to = new Position(5, 4);

            Move move = Move.normalMove(from, to, PieceType.PAWN, PlayerColor.BLACK);

            assertEquals(from, move.getFrom());
            assertEquals(to, move.getTo());
            assertEquals(PieceType.PAWN, move.getPieceType());
            assertEquals(PlayerColor.BLACK, move.getPlayer());
            assertFalse(move.isPromote());
            assertFalse(move.isDrop());
            assertNull(move.getCapturedPiece());
        }

        @Test
        @DisplayName("通常の移動はdropではない")
        void shouldNotBeDropMove() {
            Move move = Move.normalMove(
                new Position(6, 4),
                new Position(5, 4),
                PieceType.PAWN,
                PlayerColor.BLACK
            );

            assertFalse(move.isDrop());
        }

        @Test
        @DisplayName("通常の移動はpromoteフラグがfalse")
        void shouldNotBePromoteMove() {
            Move move = Move.normalMove(
                new Position(6, 4),
                new Position(5, 4),
                PieceType.PAWN,
                PlayerColor.BLACK
            );

            assertFalse(move.isPromote());
        }
    }

    @Nested
    @DisplayName("promoteMoveファクトリメソッドのテスト")
    class PromoteMoveTests {

        @Test
        @DisplayName("成り移動を作成できる")
        void shouldCreatePromoteMove() {
            Position from = new Position(3, 4);
            Position to = new Position(2, 4);

            Move move = Move.promoteMove(from, to, PieceType.PAWN, PlayerColor.BLACK);

            assertEquals(from, move.getFrom());
            assertEquals(to, move.getTo());
            assertEquals(PieceType.PAWN, move.getPieceType());
            assertEquals(PlayerColor.BLACK, move.getPlayer());
            assertTrue(move.isPromote());
            assertFalse(move.isDrop());
        }

        @Test
        @DisplayName("成り移動はpromoteフラグがtrue")
        void shouldHavePromoteFlagTrue() {
            Move move = Move.promoteMove(
                new Position(3, 4),
                new Position(2, 4),
                PieceType.PAWN,
                PlayerColor.BLACK
            );

            assertTrue(move.isPromote());
        }
    }

    @Nested
    @DisplayName("dropMoveファクトリメソッドのテスト")
    class DropMoveTests {

        @Test
        @DisplayName("打つ移動を作成できる")
        void shouldCreateDropMove() {
            Position to = new Position(4, 4);

            Move move = Move.dropMove(to, PieceType.PAWN, PlayerColor.BLACK);

            assertNull(move.getFrom());
            assertEquals(to, move.getTo());
            assertEquals(PieceType.PAWN, move.getPieceType());
            assertEquals(PlayerColor.BLACK, move.getPlayer());
            assertFalse(move.isPromote());
            assertTrue(move.isDrop());
        }

        @Test
        @DisplayName("打つ移動のfromはnull")
        void shouldHaveNullFrom() {
            Move move = Move.dropMove(new Position(4, 4), PieceType.PAWN, PlayerColor.BLACK);

            assertNull(move.getFrom());
        }

        @Test
        @DisplayName("打つ移動はdropフラグがtrue")
        void shouldHaveDropFlagTrue() {
            Move move = Move.dropMove(new Position(4, 4), PieceType.PAWN, PlayerColor.BLACK);

            assertTrue(move.isDrop());
        }

        @Test
        @DisplayName("打つ移動はpromoteフラグがfalse")
        void shouldHavePromoteFlagFalse() {
            Move move = Move.dropMove(new Position(4, 4), PieceType.PAWN, PlayerColor.BLACK);

            assertFalse(move.isPromote());
        }
    }

    @Nested
    @DisplayName("withCapturedPieceメソッドのテスト")
    class WithCapturedPieceTests {

        @Test
        @DisplayName("捕獲した駒の情報を追加できる")
        void shouldAddCapturedPieceInfo() {
            Move move = Move.normalMove(
                new Position(4, 4),
                new Position(3, 4),
                PieceType.ROOK,
                PlayerColor.BLACK
            );

            Move moveWithCapture = move.withCapturedPiece(PieceType.PAWN);

            assertEquals(PieceType.PAWN, moveWithCapture.getCapturedPiece());
        }

        @Test
        @DisplayName("元の移動は変更されない(イミュータブル)")
        void shouldNotMutateOriginalMove() {
            Move move = Move.normalMove(
                new Position(4, 4),
                new Position(3, 4),
                PieceType.ROOK,
                PlayerColor.BLACK
            );

            move.withCapturedPiece(PieceType.PAWN);

            assertNull(move.getCapturedPiece());
        }

        @Test
        @DisplayName("他のプロパティは保持される")
        void shouldPreserveOtherProperties() {
            Position from = new Position(4, 4);
            Position to = new Position(3, 4);
            Move move = Move.promoteMove(from, to, PieceType.SILVER, PlayerColor.WHITE);

            Move moveWithCapture = move.withCapturedPiece(PieceType.GOLD);

            assertEquals(from, moveWithCapture.getFrom());
            assertEquals(to, moveWithCapture.getTo());
            assertEquals(PieceType.SILVER, moveWithCapture.getPieceType());
            assertEquals(PlayerColor.WHITE, moveWithCapture.getPlayer());
            assertTrue(moveWithCapture.isPromote());
            assertFalse(moveWithCapture.isDrop());
        }

        @Test
        @DisplayName("nullで捕獲情報をクリアできる")
        void shouldAllowNullCapturedPiece() {
            Move move = Move.normalMove(
                new Position(4, 4),
                new Position(3, 4),
                PieceType.ROOK,
                PlayerColor.BLACK
            ).withCapturedPiece(PieceType.PAWN);

            Move moveWithoutCapture = move.withCapturedPiece(null);

            assertNull(moveWithoutCapture.getCapturedPiece());
        }
    }

    @Nested
    @DisplayName("toStringメソッドのテスト")
    class ToStringTests {

        @Test
        @DisplayName("通常の移動を正しく文字列化する")
        void shouldFormatNormalMoveCorrectly() {
            Move move = Move.normalMove(
                new Position(6, 4),
                new Position(5, 4),
                PieceType.PAWN,
                PlayerColor.BLACK
            );

            String result = move.toString();

            assertTrue(result.contains("先手"));
            assertTrue(result.contains("歩兵"));
            assertTrue(result.contains("(6, 4)"));
            assertTrue(result.contains("(5, 4)"));
        }

        @Test
        @DisplayName("成り移動は(promote)が含まれる")
        void shouldIncludePromoteInString() {
            Move move = Move.promoteMove(
                new Position(3, 4),
                new Position(2, 4),
                PieceType.PAWN,
                PlayerColor.BLACK
            );

            String result = move.toString();

            assertTrue(result.contains("promote"));
        }

        @Test
        @DisplayName("打つ移動を正しく文字列化する")
        void shouldFormatDropMoveCorrectly() {
            Move move = Move.dropMove(
                new Position(4, 4),
                PieceType.PAWN,
                PlayerColor.BLACK
            );

            String result = move.toString();

            assertTrue(result.contains("先手"));
            assertTrue(result.contains("drops"));
            assertTrue(result.contains("歩兵"));
            assertTrue(result.contains("(4, 4)"));
        }

        @Test
        @DisplayName("捕獲がある場合はcapturingが含まれる")
        void shouldIncludeCapturingInString() {
            Move move = Move.normalMove(
                new Position(4, 4),
                new Position(3, 4),
                PieceType.ROOK,
                PlayerColor.BLACK
            ).withCapturedPiece(PieceType.GOLD);

            String result = move.toString();

            assertTrue(result.contains("capturing"));
            assertTrue(result.contains("金将"));
        }

        @Test
        @DisplayName("後手の移動は'後手'が含まれる")
        void shouldIncludeWhitePlayerName() {
            Move move = Move.normalMove(
                new Position(2, 4),
                new Position(3, 4),
                PieceType.PAWN,
                PlayerColor.WHITE
            );

            String result = move.toString();

            assertTrue(result.contains("後手"));
        }
    }

    @Nested
    @DisplayName("等価性のテスト")
    class EqualityTests {

        @Test
        @DisplayName("同じプロパティを持つ移動は等しい")
        void shouldBeEqualWhenPropertiesAreSame() {
            Move move1 = Move.normalMove(
                new Position(6, 4),
                new Position(5, 4),
                PieceType.PAWN,
                PlayerColor.BLACK
            );
            Move move2 = Move.normalMove(
                new Position(6, 4),
                new Position(5, 4),
                PieceType.PAWN,
                PlayerColor.BLACK
            );

            assertEquals(move1, move2);
        }

        @Test
        @DisplayName("異なる移動元を持つ移動は等しくない")
        void shouldNotBeEqualWhenFromDiffers() {
            Move move1 = Move.normalMove(
                new Position(6, 4),
                new Position(5, 4),
                PieceType.PAWN,
                PlayerColor.BLACK
            );
            Move move2 = Move.normalMove(
                new Position(6, 5),
                new Position(5, 4),
                PieceType.PAWN,
                PlayerColor.BLACK
            );

            assertNotEquals(move1, move2);
        }

        @Test
        @DisplayName("異なる移動先を持つ移動は等しくない")
        void shouldNotBeEqualWhenToDiffers() {
            Move move1 = Move.normalMove(
                new Position(6, 4),
                new Position(5, 4),
                PieceType.PAWN,
                PlayerColor.BLACK
            );
            Move move2 = Move.normalMove(
                new Position(6, 4),
                new Position(5, 5),
                PieceType.PAWN,
                PlayerColor.BLACK
            );

            assertNotEquals(move1, move2);
        }

        @Test
        @DisplayName("異なる駒種を持つ移動は等しくない")
        void shouldNotBeEqualWhenPieceTypeDiffers() {
            Move move1 = Move.normalMove(
                new Position(4, 4),
                new Position(3, 4),
                PieceType.ROOK,
                PlayerColor.BLACK
            );
            Move move2 = Move.normalMove(
                new Position(4, 4),
                new Position(3, 4),
                PieceType.BISHOP,
                PlayerColor.BLACK
            );

            assertNotEquals(move1, move2);
        }

        @Test
        @DisplayName("異なるプレイヤーを持つ移動は等しくない")
        void shouldNotBeEqualWhenPlayerDiffers() {
            Move move1 = Move.normalMove(
                new Position(4, 4),
                new Position(3, 4),
                PieceType.ROOK,
                PlayerColor.BLACK
            );
            Move move2 = Move.normalMove(
                new Position(4, 4),
                new Position(3, 4),
                PieceType.ROOK,
                PlayerColor.WHITE
            );

            assertNotEquals(move1, move2);
        }

        @Test
        @DisplayName("成りフラグが異なる移動は等しくない")
        void shouldNotBeEqualWhenPromoteFlagDiffers() {
            Move move1 = Move.normalMove(
                new Position(3, 4),
                new Position(2, 4),
                PieceType.PAWN,
                PlayerColor.BLACK
            );
            Move move2 = Move.promoteMove(
                new Position(3, 4),
                new Position(2, 4),
                PieceType.PAWN,
                PlayerColor.BLACK
            );

            assertNotEquals(move1, move2);
        }

        @Test
        @DisplayName("同じプロパティを持つ移動は同じハッシュコードを持つ")
        void shouldHaveSameHashCodeForEqualMoves() {
            Move move1 = Move.normalMove(
                new Position(6, 4),
                new Position(5, 4),
                PieceType.PAWN,
                PlayerColor.BLACK
            );
            Move move2 = Move.normalMove(
                new Position(6, 4),
                new Position(5, 4),
                PieceType.PAWN,
                PlayerColor.BLACK
            );

            assertEquals(move1.hashCode(), move2.hashCode());
        }
    }
}
