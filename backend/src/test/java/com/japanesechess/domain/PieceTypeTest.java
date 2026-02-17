package com.japanesechess.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PieceType - 駒の種類を表すクラス")
class PieceTypeTest {

    @Nested
    @DisplayName("基本プロパティのテスト")
    class BasicPropertiesTests {

        @Test
        @DisplayName("玉将の日本語名とシンボルが正しい")
        void shouldHaveCorrectPropertiesForKing() {
            assertEquals("玉将", PieceType.KING.getJapaneseName());
            assertEquals("K", PieceType.KING.getSymbol());
        }

        @Test
        @DisplayName("飛車の日本語名とシンボルが正しい")
        void shouldHaveCorrectPropertiesForRook() {
            assertEquals("飛車", PieceType.ROOK.getJapaneseName());
            assertEquals("R", PieceType.ROOK.getSymbol());
        }

        @Test
        @DisplayName("角行の日本語名とシンボルが正しい")
        void shouldHaveCorrectPropertiesForBishop() {
            assertEquals("角行", PieceType.BISHOP.getJapaneseName());
            assertEquals("B", PieceType.BISHOP.getSymbol());
        }

        @Test
        @DisplayName("金将の日本語名とシンボルが正しい")
        void shouldHaveCorrectPropertiesForGold() {
            assertEquals("金将", PieceType.GOLD.getJapaneseName());
            assertEquals("G", PieceType.GOLD.getSymbol());
        }

        @Test
        @DisplayName("銀将の日本語名とシンボルが正しい")
        void shouldHaveCorrectPropertiesForSilver() {
            assertEquals("銀将", PieceType.SILVER.getJapaneseName());
            assertEquals("S", PieceType.SILVER.getSymbol());
        }

        @Test
        @DisplayName("桂馬の日本語名とシンボルが正しい")
        void shouldHaveCorrectPropertiesForKnight() {
            assertEquals("桂馬", PieceType.KNIGHT.getJapaneseName());
            assertEquals("N", PieceType.KNIGHT.getSymbol());
        }

        @Test
        @DisplayName("香車の日本語名とシンボルが正しい")
        void shouldHaveCorrectPropertiesForLance() {
            assertEquals("香車", PieceType.LANCE.getJapaneseName());
            assertEquals("L", PieceType.LANCE.getSymbol());
        }

        @Test
        @DisplayName("歩兵の日本語名とシンボルが正しい")
        void shouldHaveCorrectPropertiesForPawn() {
            assertEquals("歩兵", PieceType.PAWN.getJapaneseName());
            assertEquals("P", PieceType.PAWN.getSymbol());
        }
    }

    @Nested
    @DisplayName("成り駒のプロパティテスト")
    class PromotedPiecePropertiesTests {

        @Test
        @DisplayName("龍王の日本語名とシンボルが正しい")
        void shouldHaveCorrectPropertiesForPromotedRook() {
            assertEquals("龍王", PieceType.PROMOTED_ROOK.getJapaneseName());
            assertEquals("+R", PieceType.PROMOTED_ROOK.getSymbol());
        }

        @Test
        @DisplayName("龍馬の日本語名とシンボルが正しい")
        void shouldHaveCorrectPropertiesForPromotedBishop() {
            assertEquals("龍馬", PieceType.PROMOTED_BISHOP.getJapaneseName());
            assertEquals("+B", PieceType.PROMOTED_BISHOP.getSymbol());
        }

        @Test
        @DisplayName("成銀の日本語名とシンボルが正しい")
        void shouldHaveCorrectPropertiesForPromotedSilver() {
            assertEquals("成銀", PieceType.PROMOTED_SILVER.getJapaneseName());
            assertEquals("+S", PieceType.PROMOTED_SILVER.getSymbol());
        }

        @Test
        @DisplayName("成桂の日本語名とシンボルが正しい")
        void shouldHaveCorrectPropertiesForPromotedKnight() {
            assertEquals("成桂", PieceType.PROMOTED_KNIGHT.getJapaneseName());
            assertEquals("+N", PieceType.PROMOTED_KNIGHT.getSymbol());
        }

        @Test
        @DisplayName("成香の日本語名とシンボルが正しい")
        void shouldHaveCorrectPropertiesForPromotedLance() {
            assertEquals("成香", PieceType.PROMOTED_LANCE.getJapaneseName());
            assertEquals("+L", PieceType.PROMOTED_LANCE.getSymbol());
        }

        @Test
        @DisplayName("と金の日本語名とシンボルが正しい")
        void shouldHaveCorrectPropertiesForPromotedPawn() {
            assertEquals("と金", PieceType.PROMOTED_PAWN.getJapaneseName());
            assertEquals("+P", PieceType.PROMOTED_PAWN.getSymbol());
        }
    }

    @Nested
    @DisplayName("canPromoteメソッドのテスト")
    class CanPromoteTests {

        @Test
        @DisplayName("飛車は成ることができる")
        void shouldReturnTrueForRook() {
            assertTrue(PieceType.ROOK.canPromote());
        }

        @Test
        @DisplayName("角行は成ることができる")
        void shouldReturnTrueForBishop() {
            assertTrue(PieceType.BISHOP.canPromote());
        }

        @Test
        @DisplayName("銀将は成ることができる")
        void shouldReturnTrueForSilver() {
            assertTrue(PieceType.SILVER.canPromote());
        }

        @Test
        @DisplayName("桂馬は成ることができる")
        void shouldReturnTrueForKnight() {
            assertTrue(PieceType.KNIGHT.canPromote());
        }

        @Test
        @DisplayName("香車は成ることができる")
        void shouldReturnTrueForLance() {
            assertTrue(PieceType.LANCE.canPromote());
        }

        @Test
        @DisplayName("歩兵は成ることができる")
        void shouldReturnTrueForPawn() {
            assertTrue(PieceType.PAWN.canPromote());
        }

        @Test
        @DisplayName("玉将は成ることができない")
        void shouldReturnFalseForKing() {
            assertFalse(PieceType.KING.canPromote());
        }

        @Test
        @DisplayName("金将は成ることができない")
        void shouldReturnFalseForGold() {
            assertFalse(PieceType.GOLD.canPromote());
        }

        @ParameterizedTest
        @DisplayName("既に成っている駒は更に成ることができない")
        @EnumSource(value = PieceType.class, names = {
            "PROMOTED_ROOK", "PROMOTED_BISHOP", "PROMOTED_SILVER",
            "PROMOTED_KNIGHT", "PROMOTED_LANCE", "PROMOTED_PAWN"
        })
        void shouldReturnFalseForPromotedPieces(PieceType promotedType) {
            assertFalse(promotedType.canPromote());
        }
    }

    @Nested
    @DisplayName("promoteメソッドのテスト")
    class PromoteTests {

        @Test
        @DisplayName("飛車は龍王に成る")
        void shouldPromoteRookToPromotedRook() {
            assertEquals(PieceType.PROMOTED_ROOK, PieceType.ROOK.promote());
        }

        @Test
        @DisplayName("角行は龍馬に成る")
        void shouldPromoteBishopToPromotedBishop() {
            assertEquals(PieceType.PROMOTED_BISHOP, PieceType.BISHOP.promote());
        }

        @Test
        @DisplayName("銀将は成銀に成る")
        void shouldPromoteSilverToPromotedSilver() {
            assertEquals(PieceType.PROMOTED_SILVER, PieceType.SILVER.promote());
        }

        @Test
        @DisplayName("桂馬は成桂に成る")
        void shouldPromoteKnightToPromotedKnight() {
            assertEquals(PieceType.PROMOTED_KNIGHT, PieceType.KNIGHT.promote());
        }

        @Test
        @DisplayName("香車は成香に成る")
        void shouldPromoteLanceToPromotedLance() {
            assertEquals(PieceType.PROMOTED_LANCE, PieceType.LANCE.promote());
        }

        @Test
        @DisplayName("歩兵はと金に成る")
        void shouldPromotePawnToPromotedPawn() {
            assertEquals(PieceType.PROMOTED_PAWN, PieceType.PAWN.promote());
        }

        @Test
        @DisplayName("玉将を成ろうとするとIllegalStateExceptionをスローする")
        void shouldThrowExceptionWhenPromotingKing() {
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> PieceType.KING.promote()
            );

            assertTrue(exception.getMessage().contains("Cannot promote"));
            assertTrue(exception.getMessage().contains("KING"));
        }

        @Test
        @DisplayName("金将を成ろうとするとIllegalStateExceptionをスローする")
        void shouldThrowExceptionWhenPromotingGold() {
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> PieceType.GOLD.promote()
            );

            assertTrue(exception.getMessage().contains("Cannot promote"));
            assertTrue(exception.getMessage().contains("GOLD"));
        }

        @ParameterizedTest
        @DisplayName("既に成っている駒を更に成ろうとするとIllegalStateExceptionをスローする")
        @EnumSource(value = PieceType.class, names = {
            "PROMOTED_ROOK", "PROMOTED_BISHOP", "PROMOTED_SILVER",
            "PROMOTED_KNIGHT", "PROMOTED_LANCE", "PROMOTED_PAWN"
        })
        void shouldThrowExceptionWhenPromotingAlreadyPromotedPiece(PieceType promotedType) {
            assertThrows(
                IllegalStateException.class,
                promotedType::promote
            );
        }
    }

    @Nested
    @DisplayName("isPromotedメソッドのテスト")
    class IsPromotedTests {

        @ParameterizedTest
        @DisplayName("成り駒はtrueを返す")
        @EnumSource(value = PieceType.class, names = {
            "PROMOTED_ROOK", "PROMOTED_BISHOP", "PROMOTED_SILVER",
            "PROMOTED_KNIGHT", "PROMOTED_LANCE", "PROMOTED_PAWN"
        })
        void shouldReturnTrueForPromotedPieces(PieceType promotedType) {
            assertTrue(promotedType.isPromoted());
        }

        @ParameterizedTest
        @DisplayName("成っていない駒はfalseを返す")
        @EnumSource(value = PieceType.class, names = {
            "KING", "ROOK", "BISHOP", "GOLD", "SILVER", "KNIGHT", "LANCE", "PAWN"
        })
        void shouldReturnFalseForNonPromotedPieces(PieceType pieceType) {
            assertFalse(pieceType.isPromoted());
        }
    }

    @Nested
    @DisplayName("unpromoteメソッドのテスト")
    class UnpromoteTests {

        @Test
        @DisplayName("龍王は飛車に戻る")
        void shouldUnpromotePromotedRookToRook() {
            assertEquals(PieceType.ROOK, PieceType.PROMOTED_ROOK.unpromote());
        }

        @Test
        @DisplayName("龍馬は角行に戻る")
        void shouldUnpromotePromotedBishopToBishop() {
            assertEquals(PieceType.BISHOP, PieceType.PROMOTED_BISHOP.unpromote());
        }

        @Test
        @DisplayName("成銀は銀将に戻る")
        void shouldUnpromotePromotedSilverToSilver() {
            assertEquals(PieceType.SILVER, PieceType.PROMOTED_SILVER.unpromote());
        }

        @Test
        @DisplayName("成桂は桂馬に戻る")
        void shouldUnpromotePromotedKnightToKnight() {
            assertEquals(PieceType.KNIGHT, PieceType.PROMOTED_KNIGHT.unpromote());
        }

        @Test
        @DisplayName("成香は香車に戻る")
        void shouldUnpromotePromotedLanceToLance() {
            assertEquals(PieceType.LANCE, PieceType.PROMOTED_LANCE.unpromote());
        }

        @Test
        @DisplayName("と金は歩兵に戻る")
        void shouldUnpromotePromotedPawnToPawn() {
            assertEquals(PieceType.PAWN, PieceType.PROMOTED_PAWN.unpromote());
        }

        @ParameterizedTest
        @DisplayName("成っていない駒はそのまま返される")
        @EnumSource(value = PieceType.class, names = {
            "KING", "ROOK", "BISHOP", "GOLD", "SILVER", "KNIGHT", "LANCE", "PAWN"
        })
        void shouldReturnSameTypeForNonPromotedPieces(PieceType pieceType) {
            assertEquals(pieceType, pieceType.unpromote());
        }
    }

    @Nested
    @DisplayName("成りと成りなしの往復テスト")
    class PromoteUnpromoteRoundTripTests {

        @ParameterizedTest
        @DisplayName("成れる駒を成ってから戻すと元に戻る")
        @EnumSource(value = PieceType.class, names = {
            "ROOK", "BISHOP", "SILVER", "KNIGHT", "LANCE", "PAWN"
        })
        void shouldReturnToOriginalAfterPromoteAndUnpromote(PieceType pieceType) {
            PieceType promoted = pieceType.promote();
            PieceType unpromoted = promoted.unpromote();

            assertEquals(pieceType, unpromoted);
        }
    }
}
