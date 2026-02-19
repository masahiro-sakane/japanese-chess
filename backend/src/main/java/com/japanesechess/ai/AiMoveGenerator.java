package com.japanesechess.ai;

import com.japanesechess.domain.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AiMoveGenerator {

    private final MoveValidator moveValidator = new MoveValidator();

    public List<Move> generateAllLegalMoves(Board board, PlayerColor player) {
        List<Move> legalMoves = new ArrayList<>();

        for (Piece piece : board.getPiecesForPlayer(player)) {
            legalMoves.addAll(generateMovesForPiece(board, piece));
        }

        legalMoves.addAll(generateDropMoves(board, player));

        return legalMoves;
    }

    private List<Move> generateMovesForPiece(Board board, Piece piece) {
        List<Move> moves = new ArrayList<>();
        Position from = piece.getPosition();
        PlayerColor player = piece.getOwner();

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

        return moves;
    }

    private List<Move> generateDropMoves(Board board, PlayerColor player) {
        List<Move> moves = new ArrayList<>();
        Set<PieceType> capturedPieces = new HashSet<>(board.getCapturedPieces(player));

        for (PieceType pieceType : capturedPieces) {
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
