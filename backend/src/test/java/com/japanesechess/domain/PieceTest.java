package com.japanesechess.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Piece - 駒オブジェクトを表すクラス")
class PieceTest {

    @Nested
    @DisplayName("コンストラクタと基本プロパティのテスト")
    class ConstructorAndBasicPropertiesTests {

        @Test
        @DisplayName("駒を正しく生成できる")
        void shouldCreatePieceWithCorrectProperties() {
            Position position = new Position(4, 4);
            Piece piece = new Piece(PieceType.PAWN, PlayerColor.BLACK, position);

            assertEquals(PieceType.PAWN, piece.getType());
            assertEquals(PlayerColor.BLACK, piece.getOwner());
            assertEquals(position, piece.getPosition());
        }

        @Test
        @DisplayName("先手の玉将を作成できる")
        void shouldCreateBlackKing() {
            Position position = new Position(8, 4);
            Piece piece = new Piece(PieceType.KING, PlayerColor.BLACK, position);

            assertEquals(PieceType.KING, piece.getType());
            assertEquals(PlayerColor.BLACK, piece.getOwner());
        }

        @Test
        @DisplayName("後手の飛車を作成できる")
        void shouldCreateWhiteRook() {
            Position position = new Position(1, 7);
            Piece piece = new Piece(PieceType.ROOK, PlayerColor.WHITE, position);

            assertEquals(PieceType.ROOK, piece.getType());
            assertEquals(PlayerColor.WHITE, piece.getOwner());
        }
    }

    @Nested
    @DisplayName("promoteメソッドのテスト")
    class PromoteTests {

        @Test
        @DisplayName("歩兵をと金に成ることができる")
        void shouldPromotePawnToPromotedPawn() {
            Position position = new Position(2, 4);
            Piece pawn = new Piece(PieceType.PAWN, PlayerColor.BLACK, position);

            Piece promotedPawn = pawn.promote();

            assertEquals(PieceType.PROMOTED_PAWN, promotedPawn.getType());
            assertEquals(PlayerColor.BLACK, promotedPawn.getOwner());
            assertEquals(position, promotedPawn.getPosition());
        }

        @Test
        @DisplayName("飛車を龍王に成ることができる")
        void shouldPromoteRookToPromotedRook() {
            Position position = new Position(0, 7);
            Piece rook = new Piece(PieceType.ROOK, PlayerColor.BLACK, position);

            Piece promotedRook = rook.promote();

            assertEquals(PieceType.PROMOTED_ROOK, promotedRook.getType());
        }

        @Test
        @DisplayName("角行を龍馬に成ることができる")
        void shouldPromoteBishopToPromotedBishop() {
            Position position = new Position(1, 1);
            Piece bishop = new Piece(PieceType.BISHOP, PlayerColor.WHITE, position);

            Piece promotedBishop = bishop.promote();

            assertEquals(PieceType.PROMOTED_BISHOP, promotedBishop.getType());
        }

        @Test
        @DisplayName("成ることができない駒を成ろうとするとIllegalStateExceptionをスローする")
        void shouldThrowExceptionWhenPromotingKing() {
            Position position = new Position(8, 4);
            Piece king = new Piece(PieceType.KING, PlayerColor.BLACK, position);

            assertThrows(IllegalStateException.class, king::promote);
        }

        @Test
        @DisplayName("金将を成ろうとするとIllegalStateExceptionをスローする")
        void shouldThrowExceptionWhenPromotingGold() {
            Position position = new Position(8, 3);
            Piece gold = new Piece(PieceType.GOLD, PlayerColor.BLACK, position);

            assertThrows(IllegalStateException.class, gold::promote);
        }

        @Test
        @DisplayName("成った後も所有者は変わらない")
        void shouldKeepOwnerAfterPromotion() {
            Position position = new Position(2, 4);
            Piece piece = new Piece(PieceType.SILVER, PlayerColor.WHITE, position);

            Piece promoted = piece.promote();

            assertEquals(PlayerColor.WHITE, promoted.getOwner());
        }

        @Test
        @DisplayName("成った後も位置は変わらない")
        void shouldKeepPositionAfterPromotion() {
            Position position = new Position(2, 4);
            Piece piece = new Piece(PieceType.KNIGHT, PlayerColor.BLACK, position);

            Piece promoted = piece.promote();

            assertEquals(position, promoted.getPosition());
        }
    }

    @Nested
    @DisplayName("isPromotedメソッドのテスト")
    class IsPromotedTests {

        @Test
        @DisplayName("成っていない駒はfalseを返す")
        void shouldReturnFalseForNonPromotedPiece() {
            Piece piece = new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(6, 4));

            assertFalse(piece.isPromoted());
        }

        @Test
        @DisplayName("成り駒はtrueを返す")
        void shouldReturnTrueForPromotedPiece() {
            Piece piece = new Piece(PieceType.PROMOTED_PAWN, PlayerColor.BLACK, new Position(2, 4));

            assertTrue(piece.isPromoted());
        }

        @Test
        @DisplayName("龍王はtrueを返す")
        void shouldReturnTrueForPromotedRook() {
            Piece piece = new Piece(PieceType.PROMOTED_ROOK, PlayerColor.WHITE, new Position(4, 4));

            assertTrue(piece.isPromoted());
        }
    }

    @Nested
    @DisplayName("canPromoteAtメソッドのテスト - 先手(BLACK)")
    class CanPromoteAtForBlackTests {

        @Test
        @DisplayName("先手の駒が敵陣(row 0-2)に入ると成ることができる")
        void shouldReturnTrueWhenBlackEntersEnemyTerritory() {
            Piece pawn = new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(3, 4));
            Position targetInEnemyZone = new Position(2, 4);

            assertTrue(pawn.canPromoteAt(targetInEnemyZone));
        }

        @Test
        @DisplayName("先手の駒が敵陣の最奥(row 0)に到達すると成ることができる")
        void shouldReturnTrueWhenBlackReachesDeepestRow() {
            Piece pawn = new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(1, 4));
            Position targetDeepest = new Position(0, 4);

            assertTrue(pawn.canPromoteAt(targetDeepest));
        }

        @Test
        @DisplayName("先手の駒が敵陣から出る時も成ることができる(元の位置が敵陣内)")
        void shouldReturnTrueWhenBlackLeavesEnemyTerritory() {
            Piece piece = new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(2, 4));
            Position targetOutsideEnemyZone = new Position(5, 4);

            assertTrue(piece.canPromoteAt(targetOutsideEnemyZone));
        }

        @Test
        @DisplayName("先手の駒が敵陣外から敵陣外に移動する場合は成ることができない")
        void shouldReturnFalseWhenBlackMovesOutsideEnemyTerritory() {
            Piece pawn = new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(5, 4));
            Position targetOutside = new Position(4, 4);

            assertFalse(pawn.canPromoteAt(targetOutside));
        }

        @Test
        @DisplayName("成ることができない駒(玉将)はfalseを返す")
        void shouldReturnFalseForKing() {
            Piece king = new Piece(PieceType.KING, PlayerColor.BLACK, new Position(3, 4));
            Position targetInEnemyZone = new Position(2, 4);

            assertFalse(king.canPromoteAt(targetInEnemyZone));
        }

        @Test
        @DisplayName("成ることができない駒(金将)はfalseを返す")
        void shouldReturnFalseForGold() {
            Piece gold = new Piece(PieceType.GOLD, PlayerColor.BLACK, new Position(3, 4));
            Position targetInEnemyZone = new Position(2, 4);

            assertFalse(gold.canPromoteAt(targetInEnemyZone));
        }
    }

    @Nested
    @DisplayName("canPromoteAtメソッドのテスト - 後手(WHITE)")
    class CanPromoteAtForWhiteTests {

        @Test
        @DisplayName("後手の駒が敵陣(row 6-8)に入ると成ることができる")
        void shouldReturnTrueWhenWhiteEntersEnemyTerritory() {
            Piece pawn = new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(5, 4));
            Position targetInEnemyZone = new Position(6, 4);

            assertTrue(pawn.canPromoteAt(targetInEnemyZone));
        }

        @Test
        @DisplayName("後手の駒が敵陣の最奥(row 8)に到達すると成ることができる")
        void shouldReturnTrueWhenWhiteReachesDeepestRow() {
            Piece pawn = new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(7, 4));
            Position targetDeepest = new Position(8, 4);

            assertTrue(pawn.canPromoteAt(targetDeepest));
        }

        @Test
        @DisplayName("後手の駒が敵陣から出る時も成ることができる(元の位置が敵陣内)")
        void shouldReturnTrueWhenWhiteLeavesEnemyTerritory() {
            Piece piece = new Piece(PieceType.ROOK, PlayerColor.WHITE, new Position(6, 4));
            Position targetOutsideEnemyZone = new Position(3, 4);

            assertTrue(piece.canPromoteAt(targetOutsideEnemyZone));
        }

        @Test
        @DisplayName("後手の駒が敵陣外から敵陣外に移動する場合は成ることができない")
        void shouldReturnFalseWhenWhiteMovesOutsideEnemyTerritory() {
            Piece pawn = new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(3, 4));
            Position targetOutside = new Position(4, 4);

            assertFalse(pawn.canPromoteAt(targetOutside));
        }
    }

    @Nested
    @DisplayName("mustPromoteAtメソッドのテスト - 先手(BLACK)")
    class MustPromoteAtForBlackTests {

        @Test
        @DisplayName("先手の歩が最奥(row 0)に到達したら必ず成らなければならない")
        void shouldReturnTrueWhenBlackPawnReachesRow0() {
            Piece pawn = new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(1, 4));
            Position target = new Position(0, 4);

            assertTrue(pawn.mustPromoteAt(target));
        }

        @Test
        @DisplayName("先手の香車が最奥(row 0)に到達したら必ず成らなければならない")
        void shouldReturnTrueWhenBlackLanceReachesRow0() {
            Piece lance = new Piece(PieceType.LANCE, PlayerColor.BLACK, new Position(1, 0));
            Position target = new Position(0, 0);

            assertTrue(lance.mustPromoteAt(target));
        }

        @Test
        @DisplayName("先手の桂馬がrow 0またはrow 1に到達したら必ず成らなければならない")
        void shouldReturnTrueWhenBlackKnightReachesRow0or1() {
            Piece knight = new Piece(PieceType.KNIGHT, PlayerColor.BLACK, new Position(3, 1));
            Position targetRow1 = new Position(1, 2);
            Position targetRow0 = new Position(0, 2);

            assertTrue(knight.mustPromoteAt(targetRow1));
            assertTrue(knight.mustPromoteAt(targetRow0));
        }

        @Test
        @DisplayName("先手の歩がrow 1に到達しても成る必要はない")
        void shouldReturnFalseWhenBlackPawnReachesRow1() {
            Piece pawn = new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(2, 4));
            Position target = new Position(1, 4);

            assertFalse(pawn.mustPromoteAt(target));
        }

        @Test
        @DisplayName("先手の銀将は敵陣で成る義務はない")
        void shouldReturnFalseForSilverInEnemyTerritory() {
            Piece silver = new Piece(PieceType.SILVER, PlayerColor.BLACK, new Position(1, 4));
            Position target = new Position(0, 4);

            assertFalse(silver.mustPromoteAt(target));
        }

        @Test
        @DisplayName("先手の飛車は敵陣で成る義務はない")
        void shouldReturnFalseForRookInEnemyTerritory() {
            Piece rook = new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(1, 4));
            Position target = new Position(0, 4);

            assertFalse(rook.mustPromoteAt(target));
        }
    }

    @Nested
    @DisplayName("mustPromoteAtメソッドのテスト - 後手(WHITE)")
    class MustPromoteAtForWhiteTests {

        @Test
        @DisplayName("後手の歩が最奥(row 8)に到達したら必ず成らなければならない")
        void shouldReturnTrueWhenWhitePawnReachesRow8() {
            Piece pawn = new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(7, 4));
            Position target = new Position(8, 4);

            assertTrue(pawn.mustPromoteAt(target));
        }

        @Test
        @DisplayName("後手の香車が最奥(row 8)に到達したら必ず成らなければならない")
        void shouldReturnTrueWhenWhiteLanceReachesRow8() {
            Piece lance = new Piece(PieceType.LANCE, PlayerColor.WHITE, new Position(7, 0));
            Position target = new Position(8, 0);

            assertTrue(lance.mustPromoteAt(target));
        }

        @Test
        @DisplayName("後手の桂馬がrow 7またはrow 8に到達したら必ず成らなければならない")
        void shouldReturnTrueWhenWhiteKnightReachesRow7or8() {
            Piece knight = new Piece(PieceType.KNIGHT, PlayerColor.WHITE, new Position(5, 1));
            Position targetRow7 = new Position(7, 2);
            Position targetRow8 = new Position(8, 2);

            assertTrue(knight.mustPromoteAt(targetRow7));
            assertTrue(knight.mustPromoteAt(targetRow8));
        }

        @Test
        @DisplayName("後手の歩がrow 7に到達しても成る必要はない")
        void shouldReturnFalseWhenWhitePawnReachesRow7() {
            Piece pawn = new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(6, 4));
            Position target = new Position(7, 4);

            assertFalse(pawn.mustPromoteAt(target));
        }
    }

    @Nested
    @DisplayName("withPositionメソッドのテスト")
    class WithPositionTests {

        @Test
        @DisplayName("withPositionで新しい位置の駒を作成できる")
        void shouldCreateNewPieceWithNewPosition() {
            Position originalPosition = new Position(6, 4);
            Position newPosition = new Position(5, 4);
            Piece piece = new Piece(PieceType.PAWN, PlayerColor.BLACK, originalPosition);

            Piece movedPiece = piece.withPosition(newPosition);

            assertEquals(newPosition, movedPiece.getPosition());
            assertEquals(PieceType.PAWN, movedPiece.getType());
            assertEquals(PlayerColor.BLACK, movedPiece.getOwner());
        }

        @Test
        @DisplayName("withPositionは元の駒を変更しない(イミュータブル)")
        void shouldNotMutateOriginalPiece() {
            Position originalPosition = new Position(6, 4);
            Position newPosition = new Position(5, 4);
            Piece piece = new Piece(PieceType.PAWN, PlayerColor.BLACK, originalPosition);

            piece.withPosition(newPosition);

            assertEquals(originalPosition, piece.getPosition());
        }
    }

    @Nested
    @DisplayName("toStringメソッドのテスト")
    class ToStringTests {

        @Test
        @DisplayName("先手の歩兵を正しく文字列化する")
        void shouldFormatBlackPawnCorrectly() {
            Piece piece = new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(6, 4));
            String result = piece.toString();

            assertTrue(result.contains("先手"));
            assertTrue(result.contains("歩兵"));
            assertTrue(result.contains("(6, 4)"));
        }

        @Test
        @DisplayName("後手の飛車を正しく文字列化する")
        void shouldFormatWhiteRookCorrectly() {
            Piece piece = new Piece(PieceType.ROOK, PlayerColor.WHITE, new Position(1, 7));
            String result = piece.toString();

            assertTrue(result.contains("後手"));
            assertTrue(result.contains("飛車"));
            assertTrue(result.contains("(1, 7)"));
        }
    }

    @Nested
    @DisplayName("等価性のテスト")
    class EqualityTests {

        @Test
        @DisplayName("同じプロパティを持つ駒は等しい")
        void shouldBeEqualWhenPropertiesAreSame() {
            Position position = new Position(4, 4);
            Piece piece1 = new Piece(PieceType.PAWN, PlayerColor.BLACK, position);
            Piece piece2 = new Piece(PieceType.PAWN, PlayerColor.BLACK, position);

            assertEquals(piece1, piece2);
        }

        @Test
        @DisplayName("異なる種類の駒は等しくない")
        void shouldNotBeEqualWhenTypeDiffers() {
            Position position = new Position(4, 4);
            Piece pawn = new Piece(PieceType.PAWN, PlayerColor.BLACK, position);
            Piece gold = new Piece(PieceType.GOLD, PlayerColor.BLACK, position);

            assertNotEquals(pawn, gold);
        }

        @Test
        @DisplayName("異なる所有者の駒は等しくない")
        void shouldNotBeEqualWhenOwnerDiffers() {
            Position position = new Position(4, 4);
            Piece blackPawn = new Piece(PieceType.PAWN, PlayerColor.BLACK, position);
            Piece whitePawn = new Piece(PieceType.PAWN, PlayerColor.WHITE, position);

            assertNotEquals(blackPawn, whitePawn);
        }

        @Test
        @DisplayName("異なる位置の駒は等しくない")
        void shouldNotBeEqualWhenPositionDiffers() {
            Piece piece1 = new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(4, 4));
            Piece piece2 = new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(5, 4));

            assertNotEquals(piece1, piece2);
        }
    }
}
