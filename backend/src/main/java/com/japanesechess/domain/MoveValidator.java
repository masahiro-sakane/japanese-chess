package com.japanesechess.domain;

import java.util.List;
import java.util.Optional;

public class MoveValidator {

    private final CheckDetector checkDetector = new CheckDetector();

    public boolean isValidMove(Board board, Move move) {
        if (move.isDrop()) {
            return isValidDrop(board, move);
        }

        Optional<Piece> pieceOpt = board.getPieceAt(move.getFrom());
        if (pieceOpt.isEmpty()) {
            return false;
        }

        Piece piece = pieceOpt.get();
        if (piece.getOwner() != move.getPlayer()) {
            return false;
        }

        Optional<Piece> targetPieceOpt = board.getPieceAt(move.getTo());
        if (targetPieceOpt.isPresent() && targetPieceOpt.get().getOwner() == move.getPlayer()) {
            return false;
        }

        if (!canPieceMoveTo(piece, move.getTo(), board)) {
            return false;
        }

        if (move.isPromote() && !piece.canPromoteAt(move.getTo())) {
            return false;
        }

        if (!move.isPromote() && piece.mustPromoteAt(move.getTo())) {
            return false;
        }

        // Check: Move must not leave own king in check
        if (checkDetector.wouldBeInCheckAfterMove(board, move)) {
            return false;
        }

        return true;
    }

    private boolean isValidDrop(Board board, Move move) {
        if (board.getPieceAt(move.getTo()).isPresent()) {
            return false;
        }

        List<PieceType> captured = board.getCapturedPieces(move.getPlayer());
        if (!captured.contains(move.getPieceType())) {
            return false;
        }

        if (move.getPieceType() == PieceType.PAWN) {
            return isValidPawnDrop(board, move);
        }

        if (move.getPieceType() == PieceType.LANCE) {
            if (move.getPlayer() == PlayerColor.BLACK && move.getTo().getRow() == 0) {
                return false;
            }
            if (move.getPlayer() == PlayerColor.WHITE && move.getTo().getRow() == 8) {
                return false;
            }
        }

        if (move.getPieceType() == PieceType.KNIGHT) {
            if (move.getPlayer() == PlayerColor.BLACK && move.getTo().getRow() <= 1) {
                return false;
            }
            if (move.getPlayer() == PlayerColor.WHITE && move.getTo().getRow() >= 7) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidPawnDrop(Board board, Move move) {
        if (move.getPlayer() == PlayerColor.BLACK && move.getTo().getRow() == 0) {
            return false;
        }
        if (move.getPlayer() == PlayerColor.WHITE && move.getTo().getRow() == 8) {
            return false;
        }

        int column = move.getTo().getColumn();
        boolean hasPawnInColumn = board.getPiecesForPlayer(move.getPlayer()).stream()
            .anyMatch(p -> p.getType() == PieceType.PAWN && p.getPosition().getColumn() == column);

        return !hasPawnInColumn;
    }

    private boolean canPieceMoveTo(Piece piece, Position to, Board board) {
        Position from = piece.getPosition();
        int rowDiff = to.getRow() - from.getRow();
        int colDiff = to.getColumn() - from.getColumn();

        if (piece.getOwner() == PlayerColor.WHITE) {
            rowDiff = -rowDiff;
        }

        return switch (piece.getType()) {
            case KING -> Math.abs(rowDiff) <= 1 && Math.abs(colDiff) <= 1;
            case GOLD, PROMOTED_SILVER, PROMOTED_KNIGHT, PROMOTED_LANCE, PROMOTED_PAWN ->
                isGoldGeneralMove(rowDiff, colDiff);
            case SILVER -> isSilverMove(rowDiff, colDiff);
            case KNIGHT -> isKnightMove(rowDiff, colDiff);
            case PAWN -> rowDiff == -1 && colDiff == 0;
            case LANCE -> isLanceMove(from, to, board, piece.getOwner());
            case ROOK -> isRookMove(from, to, board);
            case BISHOP -> isBishopMove(from, to, board);
            case PROMOTED_ROOK -> isRookMove(from, to, board) || (Math.abs(rowDiff) <= 1 && Math.abs(colDiff) <= 1);
            case PROMOTED_BISHOP -> isBishopMove(from, to, board) || (Math.abs(rowDiff) <= 1 && Math.abs(colDiff) <= 1);
        };
    }

    private boolean isGoldGeneralMove(int rowDiff, int colDiff) {
        if (rowDiff == -1 && Math.abs(colDiff) <= 1) return true;
        if (rowDiff == 0 && Math.abs(colDiff) == 1) return true;
        return rowDiff == 1 && colDiff == 0;
    }

    private boolean isSilverMove(int rowDiff, int colDiff) {
        if (rowDiff == -1 && Math.abs(colDiff) <= 1) return true;
        return rowDiff == 1 && Math.abs(colDiff) == 1;
    }

    private boolean isKnightMove(int rowDiff, int colDiff) {
        return rowDiff == -2 && Math.abs(colDiff) == 1;
    }

    private boolean isLanceMove(Position from, Position to, Board board, PlayerColor owner) {
        if (from.getColumn() != to.getColumn()) {
            return false;
        }

        int direction = owner == PlayerColor.BLACK ? -1 : 1;
        int rowDiff = (to.getRow() - from.getRow()) * direction;

        if (rowDiff <= 0) {
            return false;
        }

        return isPathClear(from, to, board);
    }

    private boolean isRookMove(Position from, Position to, Board board) {
        if (from.getRow() != to.getRow() && from.getColumn() != to.getColumn()) {
            return false;
        }
        return isPathClear(from, to, board);
    }

    private boolean isBishopMove(Position from, Position to, Board board) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getColumn() - from.getColumn());

        if (rowDiff != colDiff) {
            return false;
        }

        return isPathClear(from, to, board);
    }

    private boolean isPathClear(Position from, Position to, Board board) {
        int rowStep = Integer.compare(to.getRow(), from.getRow());
        int colStep = Integer.compare(to.getColumn(), from.getColumn());

        int currentRow = from.getRow() + rowStep;
        int currentCol = from.getColumn() + colStep;

        while (currentRow != to.getRow() || currentCol != to.getColumn()) {
            if (board.getPieceAt(new Position(currentRow, currentCol)).isPresent()) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        return true;
    }
}
