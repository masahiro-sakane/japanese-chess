package com.japanesechess.ai;

import com.japanesechess.domain.*;

import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {

    private final MoveValidator moveValidator = new MoveValidator();

    public List<Move> generateAllMoves(Board board, PlayerColor player) {
        List<Move> moves = new ArrayList<>();
        moves.addAll(generatePieceMoves(board, player));
        moves.addAll(generateDropMoves(board, player));
        return moves;
    }

    private List<Move> generatePieceMoves(Board board, PlayerColor player) {
        List<Move> moves = new ArrayList<>();
        List<Piece> pieces = board.getPiecesForPlayer(player);

        for (Piece piece : pieces) {
            Position from = piece.getPosition();
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    Position to = new Position(row, col);

                    Move normalMove = Move.normalMove(from, to, piece.getType(), player);
                    if (moveValidator.isValidMove(board, normalMove)) {
                        moves.add(normalMove);
                    }

                    if (piece.canPromoteAt(to)) {
                        Move promoteMove = Move.promoteMove(from, to, piece.getType(), player);
                        if (moveValidator.isValidMove(board, promoteMove)) {
                            moves.add(promoteMove);
                        }
                    }
                }
            }
        }
        return moves;
    }

    private List<Move> generateDropMoves(Board board, PlayerColor player) {
        List<Move> moves = new ArrayList<>();
        List<PieceType> capturedPieces = board.getCapturedPieces(player);
        List<PieceType> uniqueTypes = capturedPieces.stream().distinct().toList();

        for (PieceType pieceType : uniqueTypes) {
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    Position to = new Position(row, col);
                    Move dropMove = Move.dropMove(to, pieceType, player);
                    if (moveValidator.isValidMove(board, dropMove)) {
                        moves.add(dropMove);
                    }
                }
            }
        }
        return moves;
    }
}
