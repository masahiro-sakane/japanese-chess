package com.japanesechess.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlayerColor - プレイヤーの色(先手/後手)")
class PlayerColorTest {

    @Nested
    @DisplayName("基本プロパティのテスト")
    class BasicPropertiesTests {

        @Test
        @DisplayName("先手(BLACK)の日本語名は'先手'")
        void shouldReturnCorrectJapaneseNameForBlack() {
            assertEquals("先手", PlayerColor.BLACK.getJapaneseName());
        }

        @Test
        @DisplayName("後手(WHITE)の日本語名は'後手'")
        void shouldReturnCorrectJapaneseNameForWhite() {
            assertEquals("後手", PlayerColor.WHITE.getJapaneseName());
        }
    }

    @Nested
    @DisplayName("oppositeメソッドのテスト")
    class OppositeTests {

        @Test
        @DisplayName("先手の反対は後手")
        void shouldReturnWhiteForBlack() {
            assertEquals(PlayerColor.WHITE, PlayerColor.BLACK.opposite());
        }

        @Test
        @DisplayName("後手の反対は先手")
        void shouldReturnBlackForWhite() {
            assertEquals(PlayerColor.BLACK, PlayerColor.WHITE.opposite());
        }

        @Test
        @DisplayName("oppositeを2回呼ぶと元に戻る")
        void shouldReturnOriginalAfterDoubleOpposite() {
            assertEquals(PlayerColor.BLACK, PlayerColor.BLACK.opposite().opposite());
            assertEquals(PlayerColor.WHITE, PlayerColor.WHITE.opposite().opposite());
        }
    }

    @Nested
    @DisplayName("Enum値のテスト")
    class EnumValuesTests {

        @Test
        @DisplayName("2つの値のみが存在する")
        void shouldHaveTwoValues() {
            assertEquals(2, PlayerColor.values().length);
        }

        @Test
        @DisplayName("BLACKとWHITEが存在する")
        void shouldContainBlackAndWhite() {
            PlayerColor[] values = PlayerColor.values();

            assertTrue(java.util.Arrays.asList(values).contains(PlayerColor.BLACK));
            assertTrue(java.util.Arrays.asList(values).contains(PlayerColor.WHITE));
        }

        @Test
        @DisplayName("文字列からEnumを取得できる")
        void shouldGetEnumFromString() {
            assertEquals(PlayerColor.BLACK, PlayerColor.valueOf("BLACK"));
            assertEquals(PlayerColor.WHITE, PlayerColor.valueOf("WHITE"));
        }
    }
}
