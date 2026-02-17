package com.japanesechess.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Position - 将棋盤の位置を表すクラス")
class PositionTest {

    @Nested
    @DisplayName("コンストラクタのテスト")
    class ConstructorTests {

        @Test
        @DisplayName("有効な位置(0,0)でインスタンスを生成できる")
        void shouldCreatePositionWithValidCoordinates_topLeft() {
            Position position = new Position(0, 0);

            assertEquals(0, position.getRow());
            assertEquals(0, position.getColumn());
        }

        @Test
        @DisplayName("有効な位置(8,8)でインスタンスを生成できる")
        void shouldCreatePositionWithValidCoordinates_bottomRight() {
            Position position = new Position(8, 8);

            assertEquals(8, position.getRow());
            assertEquals(8, position.getColumn());
        }

        @Test
        @DisplayName("有効な位置(4,4)でインスタンスを生成できる - 中央")
        void shouldCreatePositionWithValidCoordinates_center() {
            Position position = new Position(4, 4);

            assertEquals(4, position.getRow());
            assertEquals(4, position.getColumn());
        }

        @ParameterizedTest
        @DisplayName("全ての有効な行と列の組み合わせでインスタンスを生成できる")
        @CsvSource({
            "0, 0", "0, 8", "8, 0", "8, 8",
            "3, 5", "7, 2", "1, 6"
        })
        void shouldCreatePositionWithAllValidCoordinates(int row, int column) {
            Position position = new Position(row, column);

            assertEquals(row, position.getRow());
            assertEquals(column, position.getColumn());
        }
    }

    @Nested
    @DisplayName("境界値のテスト - 無効な位置")
    class BoundaryTests {

        @Test
        @DisplayName("行が負の値の場合、IllegalArgumentExceptionをスローする")
        void shouldThrowExceptionWhenRowIsNegative() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Position(-1, 0)
            );

            assertTrue(exception.getMessage().contains("Invalid position"));
            assertTrue(exception.getMessage().contains("row=-1"));
        }

        @Test
        @DisplayName("列が負の値の場合、IllegalArgumentExceptionをスローする")
        void shouldThrowExceptionWhenColumnIsNegative() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Position(0, -1)
            );

            assertTrue(exception.getMessage().contains("Invalid position"));
            assertTrue(exception.getMessage().contains("column=-1"));
        }

        @Test
        @DisplayName("行が9以上の場合、IllegalArgumentExceptionをスローする")
        void shouldThrowExceptionWhenRowIsGreaterThanOrEqual9() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Position(9, 0)
            );

            assertTrue(exception.getMessage().contains("Invalid position"));
            assertTrue(exception.getMessage().contains("row=9"));
        }

        @Test
        @DisplayName("列が9以上の場合、IllegalArgumentExceptionをスローする")
        void shouldThrowExceptionWhenColumnIsGreaterThanOrEqual9() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Position(0, 9)
            );

            assertTrue(exception.getMessage().contains("Invalid position"));
            assertTrue(exception.getMessage().contains("column=9"));
        }

        @ParameterizedTest
        @DisplayName("様々な無効な行値でIllegalArgumentExceptionをスローする")
        @ValueSource(ints = {-100, -10, -1, 9, 10, 100})
        void shouldThrowExceptionForVariousInvalidRowValues(int row) {
            assertThrows(
                IllegalArgumentException.class,
                () -> new Position(row, 4)
            );
        }

        @ParameterizedTest
        @DisplayName("様々な無効な列値でIllegalArgumentExceptionをスローする")
        @ValueSource(ints = {-100, -10, -1, 9, 10, 100})
        void shouldThrowExceptionForVariousInvalidColumnValues(int column) {
            assertThrows(
                IllegalArgumentException.class,
                () -> new Position(4, column)
            );
        }
    }

    @Nested
    @DisplayName("isValidメソッドのテスト")
    class IsValidTests {

        @Test
        @DisplayName("有効な位置に対してtrueを返す")
        void shouldReturnTrueForValidPosition() {
            Position position = new Position(4, 4);

            assertTrue(position.isValid());
        }

        @ParameterizedTest
        @DisplayName("盤面の四隅の位置は全て有効")
        @CsvSource({"0, 0", "0, 8", "8, 0", "8, 8"})
        void shouldReturnTrueForCornerPositions(int row, int column) {
            Position position = new Position(row, column);

            assertTrue(position.isValid());
        }
    }

    @Nested
    @DisplayName("toStringメソッドのテスト")
    class ToStringTests {

        @Test
        @DisplayName("正しいフォーマットで文字列を返す")
        void shouldReturnCorrectlyFormattedString() {
            Position position = new Position(3, 5);

            assertEquals("(3, 5)", position.toString());
        }

        @Test
        @DisplayName("原点の位置を正しくフォーマットする")
        void shouldFormatOriginPosition() {
            Position position = new Position(0, 0);

            assertEquals("(0, 0)", position.toString());
        }

        @Test
        @DisplayName("最大位置を正しくフォーマットする")
        void shouldFormatMaxPosition() {
            Position position = new Position(8, 8);

            assertEquals("(8, 8)", position.toString());
        }
    }

    @Nested
    @DisplayName("等価性のテスト")
    class EqualityTests {

        @Test
        @DisplayName("同じ座標を持つPositionは等しい")
        void shouldBeEqualWhenCoordinatesAreSame() {
            Position position1 = new Position(3, 5);
            Position position2 = new Position(3, 5);

            assertEquals(position1, position2);
        }

        @Test
        @DisplayName("異なる座標を持つPositionは等しくない")
        void shouldNotBeEqualWhenCoordinatesDiffer() {
            Position position1 = new Position(3, 5);
            Position position2 = new Position(3, 6);

            assertNotEquals(position1, position2);
        }

        @Test
        @DisplayName("同じ座標を持つPositionは同じハッシュコードを持つ")
        void shouldHaveSameHashCodeForEqualPositions() {
            Position position1 = new Position(3, 5);
            Position position2 = new Position(3, 5);

            assertEquals(position1.hashCode(), position2.hashCode());
        }
    }
}
