package com.japanesechess.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Board - 将棋盤を表すクラス")
class BoardTest {

    @Nested
    @DisplayName("コンストラクタのテスト")
    class ConstructorTests {

        @Test
        @DisplayName("空の盤面を作成できる")
        void shouldCreateEmptyBoard() {
            Board board = new Board();

            assertTrue(board.getAllPieces().isEmpty());
            assertEquals(0, board.getCapturedPieces(PlayerColor.BLACK).size());
            assertEquals(0, board.getCapturedPieces(PlayerColor.WHITE).size());
        }
    }

    @Nested
    @DisplayName("createInitialBoardメソッドのテスト")
    class CreateInitialBoardTests {

        private Board initialBoard;

        @BeforeEach
        void setUp() {
            initialBoard = Board.createInitialBoard();
        }

        @Test
        @DisplayName("初期配置で40枚の駒が配置される")
        void shouldCreate40Pieces() {
            assertEquals(40, initialBoard.getAllPieces().size());
        }

        @Test
        @DisplayName("先手(BLACK)は20枚の駒を持つ")
        void shouldHave20BlackPieces() {
            List<Piece> blackPieces = initialBoard.getPiecesForPlayer(PlayerColor.BLACK);
            assertEquals(20, blackPieces.size());
        }

        @Test
        @DisplayName("後手(WHITE)は20枚の駒を持つ")
        void shouldHave20WhitePieces() {
            List<Piece> whitePieces = initialBoard.getPiecesForPlayer(PlayerColor.WHITE);
            assertEquals(20, whitePieces.size());
        }

        @Test
        @DisplayName("先手の玉将は(8,4)に配置される")
        void shouldPlaceBlackKingAt8_4() {
            Optional<Piece> king = initialBoard.findKing(PlayerColor.BLACK);

            assertTrue(king.isPresent());
            assertEquals(new Position(8, 4), king.get().getPosition());
        }

        @Test
        @DisplayName("後手の玉将は(0,4)に配置される")
        void shouldPlaceWhiteKingAt0_4() {
            Optional<Piece> king = initialBoard.findKing(PlayerColor.WHITE);

            assertTrue(king.isPresent());
            assertEquals(new Position(0, 4), king.get().getPosition());
        }

        @Test
        @DisplayName("先手の飛車は(7,1)に配置される")
        void shouldPlaceBlackRookAt7_1() {
            Optional<Piece> piece = initialBoard.getPieceAt(new Position(7, 1));

            assertTrue(piece.isPresent());
            assertEquals(PieceType.ROOK, piece.get().getType());
            assertEquals(PlayerColor.BLACK, piece.get().getOwner());
        }

        @Test
        @DisplayName("先手の角行は(7,7)に配置される")
        void shouldPlaceBlackBishopAt7_7() {
            Optional<Piece> piece = initialBoard.getPieceAt(new Position(7, 7));

            assertTrue(piece.isPresent());
            assertEquals(PieceType.BISHOP, piece.get().getType());
            assertEquals(PlayerColor.BLACK, piece.get().getOwner());
        }

        @Test
        @DisplayName("後手の飛車は(1,7)に配置される")
        void shouldPlaceWhiteRookAt1_7() {
            Optional<Piece> piece = initialBoard.getPieceAt(new Position(1, 7));

            assertTrue(piece.isPresent());
            assertEquals(PieceType.ROOK, piece.get().getType());
            assertEquals(PlayerColor.WHITE, piece.get().getOwner());
        }

        @Test
        @DisplayName("後手の角行は(1,1)に配置される")
        void shouldPlaceWhiteBishopAt1_1() {
            Optional<Piece> piece = initialBoard.getPieceAt(new Position(1, 1));

            assertTrue(piece.isPresent());
            assertEquals(PieceType.BISHOP, piece.get().getType());
            assertEquals(PlayerColor.WHITE, piece.get().getOwner());
        }

        @Test
        @DisplayName("先手の歩は行6に9枚配置される")
        void shouldPlace9BlackPawnsAtRow6() {
            long pawnCount = initialBoard.getAllPieces().stream()
                .filter(p -> p.getType() == PieceType.PAWN)
                .filter(p -> p.getOwner() == PlayerColor.BLACK)
                .filter(p -> p.getPosition().getRow() == 6)
                .count();

            assertEquals(9, pawnCount);
        }

        @Test
        @DisplayName("後手の歩は行2に9枚配置される")
        void shouldPlace9WhitePawnsAtRow2() {
            long pawnCount = initialBoard.getAllPieces().stream()
                .filter(p -> p.getType() == PieceType.PAWN)
                .filter(p -> p.getOwner() == PlayerColor.WHITE)
                .filter(p -> p.getPosition().getRow() == 2)
                .count();

            assertEquals(9, pawnCount);
        }

        @Test
        @DisplayName("先手の後列(行8)に9枚の駒が配置される")
        void shouldPlace9PiecesAtBlackBackRow() {
            long pieceCount = initialBoard.getAllPieces().stream()
                .filter(p -> p.getOwner() == PlayerColor.BLACK)
                .filter(p -> p.getPosition().getRow() == 8)
                .count();

            assertEquals(9, pieceCount);
        }

        @Test
        @DisplayName("後手の後列(行0)に9枚の駒が配置される")
        void shouldPlace9PiecesAtWhiteBackRow() {
            long pieceCount = initialBoard.getAllPieces().stream()
                .filter(p -> p.getOwner() == PlayerColor.WHITE)
                .filter(p -> p.getPosition().getRow() == 0)
                .count();

            assertEquals(9, pieceCount);
        }

        @Test
        @DisplayName("初期配置で持ち駒は空")
        void shouldHaveNoCapturedPieces() {
            assertTrue(initialBoard.getCapturedPieces(PlayerColor.BLACK).isEmpty());
            assertTrue(initialBoard.getCapturedPieces(PlayerColor.WHITE).isEmpty());
        }
    }

    @Nested
    @DisplayName("placePieceとgetPieceAtメソッドのテスト")
    class PlaceAndGetPieceTests {

        @Test
        @DisplayName("駒を配置して取得できる")
        void shouldPlaceAndGetPiece() {
            Board board = new Board();
            Position position = new Position(4, 4);
            Piece piece = new Piece(PieceType.PAWN, PlayerColor.BLACK, position);

            board.placePiece(piece);
            Optional<Piece> retrievedPiece = board.getPieceAt(position);

            assertTrue(retrievedPiece.isPresent());
            assertEquals(piece, retrievedPiece.get());
        }

        @Test
        @DisplayName("駒がない位置からは空のOptionalを返す")
        void shouldReturnEmptyOptionalForEmptyPosition() {
            Board board = new Board();
            Position emptyPosition = new Position(4, 4);

            Optional<Piece> result = board.getPieceAt(emptyPosition);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("同じ位置に駒を配置すると上書きされる")
        void shouldOverwritePieceAtSamePosition() {
            Board board = new Board();
            Position position = new Position(4, 4);
            Piece firstPiece = new Piece(PieceType.PAWN, PlayerColor.BLACK, position);
            Piece secondPiece = new Piece(PieceType.GOLD, PlayerColor.WHITE, position);

            board.placePiece(firstPiece);
            board.placePiece(secondPiece);
            Optional<Piece> result = board.getPieceAt(position);

            assertTrue(result.isPresent());
            assertEquals(PieceType.GOLD, result.get().getType());
            assertEquals(PlayerColor.WHITE, result.get().getOwner());
        }
    }

    @Nested
    @DisplayName("removePieceメソッドのテスト")
    class RemovePieceTests {

        @Test
        @DisplayName("配置した駒を削除できる")
        void shouldRemovePiece() {
            Board board = new Board();
            Position position = new Position(4, 4);
            Piece piece = new Piece(PieceType.PAWN, PlayerColor.BLACK, position);

            board.placePiece(piece);
            board.removePiece(position);
            Optional<Piece> result = board.getPieceAt(position);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("駒がない位置からの削除は何も起きない")
        void shouldDoNothingWhenRemovingFromEmptyPosition() {
            Board board = new Board();
            Position emptyPosition = new Position(4, 4);

            assertDoesNotThrow(() -> board.removePiece(emptyPosition));
        }
    }

    @Nested
    @DisplayName("applyMoveメソッドのテスト - 通常移動")
    class ApplyMoveNormalTests {

        @Test
        @DisplayName("駒を移動すると新しい盤面が返される")
        void shouldReturnNewBoardAfterMove() {
            Board board = Board.createInitialBoard();
            Position from = new Position(6, 4);
            Position to = new Position(5, 4);
            Move move = Move.normalMove(from, to, PieceType.PAWN, PlayerColor.BLACK);

            Board newBoard = board.applyMove(move);

            assertNotSame(board, newBoard);
        }

        @Test
        @DisplayName("移動後、移動元に駒がない")
        void shouldRemovePieceFromOriginalPosition() {
            Board board = Board.createInitialBoard();
            Position from = new Position(6, 4);
            Position to = new Position(5, 4);
            Move move = Move.normalMove(from, to, PieceType.PAWN, PlayerColor.BLACK);

            Board newBoard = board.applyMove(move);

            assertTrue(newBoard.getPieceAt(from).isEmpty());
        }

        @Test
        @DisplayName("移動後、移動先に駒がある")
        void shouldPlacePieceAtTargetPosition() {
            Board board = Board.createInitialBoard();
            Position from = new Position(6, 4);
            Position to = new Position(5, 4);
            Move move = Move.normalMove(from, to, PieceType.PAWN, PlayerColor.BLACK);

            Board newBoard = board.applyMove(move);

            Optional<Piece> movedPiece = newBoard.getPieceAt(to);
            assertTrue(movedPiece.isPresent());
            assertEquals(PieceType.PAWN, movedPiece.get().getType());
            assertEquals(PlayerColor.BLACK, movedPiece.get().getOwner());
        }

        @Test
        @DisplayName("元の盤面は変更されない(イミュータブル)")
        void shouldNotMutateOriginalBoard() {
            Board board = Board.createInitialBoard();
            Position from = new Position(6, 4);
            Position to = new Position(5, 4);
            Move move = Move.normalMove(from, to, PieceType.PAWN, PlayerColor.BLACK);

            board.applyMove(move);

            assertTrue(board.getPieceAt(from).isPresent());
        }

        @Test
        @DisplayName("移動元に駒がない場合は例外をスローする")
        void shouldThrowExceptionWhenNoPieceAtFrom() {
            Board board = new Board();
            Position from = new Position(4, 4);
            Position to = new Position(3, 4);
            Move move = Move.normalMove(from, to, PieceType.PAWN, PlayerColor.BLACK);

            assertThrows(IllegalStateException.class, () -> board.applyMove(move));
        }
    }

    @Nested
    @DisplayName("applyMoveメソッドのテスト - 駒を取る")
    class ApplyMoveCaptureTests {

        @Test
        @DisplayName("相手の駒を取ると持ち駒に追加される")
        void shouldAddCapturedPieceToHand() {
            Board board = new Board();
            Position blackPawnPosition = new Position(4, 4);
            Position whitePawnPosition = new Position(3, 4);

            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, blackPawnPosition));
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.WHITE, whitePawnPosition));

            Move move = Move.normalMove(blackPawnPosition, whitePawnPosition, PieceType.PAWN, PlayerColor.BLACK);
            Board newBoard = board.applyMove(move);

            List<PieceType> captured = newBoard.getCapturedPieces(PlayerColor.BLACK);
            assertEquals(1, captured.size());
            assertEquals(PieceType.PAWN, captured.get(0));
        }

        @Test
        @DisplayName("成り駒を取ると成る前の状態で持ち駒に追加される")
        void shouldUnpromoteCapturedPromotedPiece() {
            Board board = new Board();
            Position blackRookPosition = new Position(4, 4);
            Position whitePromotedPawnPosition = new Position(3, 4);

            board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, blackRookPosition));
            board.placePiece(new Piece(PieceType.PROMOTED_PAWN, PlayerColor.WHITE, whitePromotedPawnPosition));

            Move move = Move.normalMove(blackRookPosition, whitePromotedPawnPosition, PieceType.ROOK, PlayerColor.BLACK);
            Board newBoard = board.applyMove(move);

            List<PieceType> captured = newBoard.getCapturedPieces(PlayerColor.BLACK);
            assertEquals(1, captured.size());
            assertEquals(PieceType.PAWN, captured.get(0));
        }

        @Test
        @DisplayName("龍王を取ると飛車として持ち駒になる")
        void shouldUnpromotePromotedRookToCaptured() {
            Board board = new Board();
            Position blackPawnPosition = new Position(4, 4);
            Position whitePromotedRookPosition = new Position(3, 4);

            board.placePiece(new Piece(PieceType.GOLD, PlayerColor.BLACK, blackPawnPosition));
            board.placePiece(new Piece(PieceType.PROMOTED_ROOK, PlayerColor.WHITE, whitePromotedRookPosition));

            Move move = Move.normalMove(blackPawnPosition, whitePromotedRookPosition, PieceType.GOLD, PlayerColor.BLACK);
            Board newBoard = board.applyMove(move);

            List<PieceType> captured = newBoard.getCapturedPieces(PlayerColor.BLACK);
            assertTrue(captured.contains(PieceType.ROOK));
        }
    }

    @Nested
    @DisplayName("applyMoveメソッドのテスト - 成り")
    class ApplyMovePromoteTests {

        @Test
        @DisplayName("成りフラグがtrueの場合、駒が成る")
        void shouldPromotePieceWhenPromoteFlagIsTrue() {
            Board board = new Board();
            Position from = new Position(3, 4);
            Position to = new Position(2, 4);
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, from));

            Move move = Move.promoteMove(from, to, PieceType.PAWN, PlayerColor.BLACK);
            Board newBoard = board.applyMove(move);

            Optional<Piece> promotedPiece = newBoard.getPieceAt(to);
            assertTrue(promotedPiece.isPresent());
            assertEquals(PieceType.PROMOTED_PAWN, promotedPiece.get().getType());
        }

        @Test
        @DisplayName("成りフラグがfalseの場合、駒は成らない")
        void shouldNotPromotePieceWhenPromoteFlagIsFalse() {
            Board board = new Board();
            Position from = new Position(3, 4);
            Position to = new Position(2, 4);
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, from));

            Move move = Move.normalMove(from, to, PieceType.PAWN, PlayerColor.BLACK);
            Board newBoard = board.applyMove(move);

            Optional<Piece> movedPiece = newBoard.getPieceAt(to);
            assertTrue(movedPiece.isPresent());
            assertEquals(PieceType.PAWN, movedPiece.get().getType());
        }
    }

    @Nested
    @DisplayName("applyMoveメソッドのテスト - 駒を打つ")
    class ApplyMoveDropTests {

        @Test
        @DisplayName("持ち駒を打つことができる")
        void shouldDropPieceFromHand() {
            Board board = new Board();
            Position to = new Position(4, 4);

            Piece capturingPiece = new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(5, 5));
            Piece targetPiece = new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(4, 5));
            board.placePiece(capturingPiece);
            board.placePiece(targetPiece);
            Move captureMove = Move.normalMove(new Position(5, 5), new Position(4, 5), PieceType.ROOK, PlayerColor.BLACK);
            Board boardWithCaptured = board.applyMove(captureMove);

            Move dropMove = Move.dropMove(to, PieceType.PAWN, PlayerColor.BLACK);
            Board newBoard = boardWithCaptured.applyMove(dropMove);

            Optional<Piece> droppedPiece = newBoard.getPieceAt(to);
            assertTrue(droppedPiece.isPresent());
            assertEquals(PieceType.PAWN, droppedPiece.get().getType());
            assertEquals(PlayerColor.BLACK, droppedPiece.get().getOwner());
        }

        @Test
        @DisplayName("駒を打つと持ち駒から減る")
        void shouldRemovePieceFromHandAfterDrop() {
            Board board = new Board();
            Position to = new Position(4, 4);

            Piece capturingPiece = new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(5, 5));
            Piece targetPiece = new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(4, 5));
            board.placePiece(capturingPiece);
            board.placePiece(targetPiece);
            Move captureMove = Move.normalMove(new Position(5, 5), new Position(4, 5), PieceType.ROOK, PlayerColor.BLACK);
            Board boardWithCaptured = board.applyMove(captureMove);

            assertEquals(1, boardWithCaptured.getCapturedPieces(PlayerColor.BLACK).size());

            Move dropMove = Move.dropMove(to, PieceType.PAWN, PlayerColor.BLACK);
            Board newBoard = boardWithCaptured.applyMove(dropMove);

            assertEquals(0, newBoard.getCapturedPieces(PlayerColor.BLACK).size());
        }
    }

    @Nested
    @DisplayName("copyメソッドのテスト")
    class CopyTests {

        @Test
        @DisplayName("盤面のコピーが作成される")
        void shouldCreateCopyOfBoard() {
            Board original = Board.createInitialBoard();

            Board copy = original.copy();

            assertNotSame(original, copy);
            assertEquals(original.getAllPieces().size(), copy.getAllPieces().size());
        }

        @Test
        @DisplayName("コピーへの変更は元の盤面に影響しない")
        void shouldNotAffectOriginalWhenCopyIsModified() {
            Board original = Board.createInitialBoard();
            Board copy = original.copy();

            Position position = new Position(6, 4);
            copy.removePiece(position);

            assertTrue(original.getPieceAt(position).isPresent());
            assertTrue(copy.getPieceAt(position).isEmpty());
        }
    }

    @Nested
    @DisplayName("findKingメソッドのテスト")
    class FindKingTests {

        @Test
        @DisplayName("先手の玉将を見つけられる")
        void shouldFindBlackKing() {
            Board board = Board.createInitialBoard();

            Optional<Piece> king = board.findKing(PlayerColor.BLACK);

            assertTrue(king.isPresent());
            assertEquals(PieceType.KING, king.get().getType());
            assertEquals(PlayerColor.BLACK, king.get().getOwner());
        }

        @Test
        @DisplayName("後手の玉将を見つけられる")
        void shouldFindWhiteKing() {
            Board board = Board.createInitialBoard();

            Optional<Piece> king = board.findKing(PlayerColor.WHITE);

            assertTrue(king.isPresent());
            assertEquals(PieceType.KING, king.get().getType());
            assertEquals(PlayerColor.WHITE, king.get().getOwner());
        }

        @Test
        @DisplayName("玉将がない場合は空のOptionalを返す")
        void shouldReturnEmptyWhenKingNotFound() {
            Board board = new Board();

            Optional<Piece> king = board.findKing(PlayerColor.BLACK);

            assertTrue(king.isEmpty());
        }
    }

    @Nested
    @DisplayName("getCapturedPiecesメソッドのテスト")
    class GetCapturedPiecesTests {

        @Test
        @DisplayName("持ち駒のリストのコピーを返す")
        void shouldReturnCopyOfCapturedPieces() {
            Board board = new Board();
            board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(4, 4)));
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(3, 4)));

            Move captureMove = Move.normalMove(new Position(4, 4), new Position(3, 4), PieceType.ROOK, PlayerColor.BLACK);
            Board newBoard = board.applyMove(captureMove);

            List<PieceType> captured = newBoard.getCapturedPieces(PlayerColor.BLACK);
            captured.clear();

            assertEquals(1, newBoard.getCapturedPieces(PlayerColor.BLACK).size());
        }
    }
}
