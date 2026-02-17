package com.japanesechess.domain;

import java.util.List;
import java.util.Optional;

/**
 * Detects check (王手) and checkmate (詰み) conditions in Japanese Chess (Shogi).
 * A player is in check when their king can be captured by an opponent's piece.
 * A player is in checkmate when they are in check and have no legal moves to escape.
 */
public class CheckDetector {

    private MoveValidator moveValidator;

    /**
     * Checks if the specified player's king is in check.
     *
     * @param board The current board state
     * @param player The player to check
     * @return true if the player's king is in check, false otherwise
     */
    public boolean isInCheck(Board board, PlayerColor player) {
        Optional<Position> kingPos = findKingPosition(board, player);
        if (kingPos.isEmpty()) {
            return false; // No king on board
        }

        PlayerColor opponent = player.opposite();
        return isPositionUnderAttack(board, kingPos.get(), opponent);
    }

    /**
     * Determines if a move would leave the player's own king in check.
     * This is used to validate if a move is legal.
     *
     * @param board The current board state
     * @param move The move to validate
     * @return true if the move would leave the player in check (illegal), false otherwise
     */
    public boolean wouldBeInCheckAfterMove(Board board, Move move) {
        // Simulate the move on a copy of the board
        Board boardAfterMove = board.applyMove(move);
        return isInCheck(boardAfterMove, move.getPlayer());
    }

    /**
     * Checks if the specified player is in checkmate (詰み).
     * Checkmate occurs when:
     * 1. The player's king is in check
     * 2. No legal move can resolve the check
     *
     * @param board The current board state
     * @param player The player to check
     * @return true if the player is in checkmate, false otherwise
     */
    public boolean isCheckmate(Board board, PlayerColor player) {
        // Must be in check to be in checkmate
        if (!isInCheck(board, player)) {
            return false;
        }

        // Check if any legal move can escape check
        return !hasLegalMove(board, player);
    }

    /**
     * Determines if the player has any legal move available.
     *
     * @param board The current board state
     * @param player The player to check
     * @return true if the player has at least one legal move
     */
    private boolean hasLegalMove(Board board, PlayerColor player) {
        // Get MoveValidator instance (lazy initialization to avoid circular dependency)
        if (moveValidator == null) {
            moveValidator = new MoveValidator();
        }

        List<Piece> playerPieces = board.getPiecesForPlayer(player);

        // Try all possible moves for all pieces
        for (Piece piece : playerPieces) {
            Position from = piece.getPosition();

            // Try all possible destination squares
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    Position to = new Position(row, col);

                    // Try normal move
                    Move normalMove = Move.normalMove(from, to, piece.getType(), player);
                    if (moveValidator.isValidMove(board, normalMove)) {
                        return true; // Found a legal move
                    }

                    // Try promotion move if piece can promote
                    if (piece.canPromoteAt(to)) {
                        Move promoteMove = Move.promoteMove(from, to, piece.getType(), player);
                        if (moveValidator.isValidMove(board, promoteMove)) {
                            return true; // Found a legal move
                        }
                    }
                }
            }
        }

        // Try drop moves for captured pieces
        List<PieceType> capturedPieces = board.getCapturedPieces(player);
        for (PieceType pieceType : capturedPieces) {
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    Position to = new Position(row, col);
                    Move dropMove = Move.dropMove(to, pieceType, player);
                    if (moveValidator.isValidMove(board, dropMove)) {
                        return true; // Found a legal drop
                    }
                }
            }
        }

        return false; // No legal moves available
    }

    /**
     * Finds the position of the king for the specified player.
     *
     * @param board The current board state
     * @param player The player whose king to find
     * @return The position of the king, or empty if not found
     */
    private Optional<Position> findKingPosition(Board board, PlayerColor player) {
        List<Piece> playerPieces = board.getPiecesForPlayer(player);
        return playerPieces.stream()
            .filter(piece -> piece.getType() == PieceType.KING)
            .map(Piece::getPosition)
            .findFirst();
    }

    /**
     * Checks if any opponent piece can attack the given position.
     *
     * @param board The current board state
     * @param position The position to check
     * @param opponent The attacking player
     * @return true if any opponent piece can attack the position
     */
    private boolean isPositionUnderAttack(Board board, Position position, PlayerColor opponent) {
        List<Piece> opponentPieces = board.getPiecesForPlayer(opponent);

        for (Piece piece : opponentPieces) {
            // Create a hypothetical move from the opponent piece to the target position
            Move attackMove = Move.normalMove(
                piece.getPosition(),
                position,
                piece.getType(),
                opponent
            );

            // Check if this piece can move to the target position
            // (which would mean it's attacking that position)
            if (canPieceMoveTo(piece, position, board)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a piece can move to a target position based on movement rules.
     * This is similar to MoveValidator logic but doesn't check for check conditions
     * to avoid circular dependencies.
     */
    private boolean canPieceMoveTo(Piece piece, Position to, Board board) {
        Position from = piece.getPosition();
        int actualRowDiff = to.getRow() - from.getRow();
        int colDiff = to.getColumn() - from.getColumn();

        // For movement validation, normalize direction based on player
        // BLACK moves forward toward row 0 (decreasing row numbers)
        // WHITE moves forward toward row 8 (increasing row numbers)
        // But movement patterns are defined from BLACK's perspective
        int normalizedRowDiff = piece.getOwner() == PlayerColor.WHITE
            ? -actualRowDiff  // WHITE: flip the direction
            : actualRowDiff;  // BLACK: use as-is

        return switch (piece.getType()) {
            case KING -> Math.abs(actualRowDiff) <= 1 && Math.abs(colDiff) <= 1;
            case GOLD, PROMOTED_SILVER, PROMOTED_KNIGHT, PROMOTED_LANCE, PROMOTED_PAWN ->
                isGoldGeneralMove(normalizedRowDiff, colDiff);
            case SILVER -> isSilverMove(normalizedRowDiff, colDiff);
            case KNIGHT -> isKnightMove(normalizedRowDiff, colDiff);
            case PAWN -> normalizedRowDiff == -1 && colDiff == 0;
            case LANCE -> isLanceMove(from, to, board, piece.getOwner());
            case ROOK -> isRookMove(from, to, board);
            case BISHOP -> isBishopMove(from, to, board);
            case PROMOTED_ROOK -> isRookMove(from, to, board) || (Math.abs(actualRowDiff) <= 1 && Math.abs(colDiff) <= 1);
            case PROMOTED_BISHOP -> isBishopMove(from, to, board) || (Math.abs(actualRowDiff) <= 1 && Math.abs(colDiff) <= 1);
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
