package com.japanesechess.ai;

import com.japanesechess.domain.*;

import java.util.List;

public class BoardEvaluator {

    private static final int KING_VALUE = 10000;
    private static final int ROOK_VALUE = 1040;
    private static final int BISHOP_VALUE = 890;
    private static final int GOLD_VALUE = 600;
    private static final int SILVER_VALUE = 560;
    private static final int KNIGHT_VALUE = 410;
    private static final int LANCE_VALUE = 370;
    private static final int PAWN_VALUE = 100;

    private static final int PROMOTED_ROOK_VALUE = 1200;
    private static final int PROMOTED_BISHOP_VALUE = 1050;
    private static final int PROMOTED_SILVER_VALUE = 640;
    private static final int PROMOTED_KNIGHT_VALUE = 470;
    private static final int PROMOTED_LANCE_VALUE = 430;
    private static final int PROMOTED_PAWN_VALUE = 400;

    private static final int CAPTURED_PIECE_BONUS = 115;

    public int evaluate(Board board, PlayerColor perspective) {
        int score = 0;

        for (Piece piece : board.getAllPieces()) {
            int value = getPieceValue(piece.getType());
            if (piece.getOwner() == perspective) {
                score += value;
            } else {
                score -= value;
            }
        }

        List<PieceType> myCaptured = board.getCapturedPieces(perspective);
        for (PieceType pieceType : myCaptured) {
            score += (int) (getPieceValue(pieceType) * CAPTURED_PIECE_BONUS / 100.0);
        }

        List<PieceType> opponentCaptured = board.getCapturedPieces(perspective.opposite());
        for (PieceType pieceType : opponentCaptured) {
            score -= (int) (getPieceValue(pieceType) * CAPTURED_PIECE_BONUS / 100.0);
        }

        return score;
    }

    private int getPieceValue(PieceType type) {
        return switch (type) {
            case KING -> KING_VALUE;
            case ROOK -> ROOK_VALUE;
            case BISHOP -> BISHOP_VALUE;
            case GOLD -> GOLD_VALUE;
            case SILVER -> SILVER_VALUE;
            case KNIGHT -> KNIGHT_VALUE;
            case LANCE -> LANCE_VALUE;
            case PAWN -> PAWN_VALUE;
            case PROMOTED_ROOK -> PROMOTED_ROOK_VALUE;
            case PROMOTED_BISHOP -> PROMOTED_BISHOP_VALUE;
            case PROMOTED_SILVER -> PROMOTED_SILVER_VALUE;
            case PROMOTED_KNIGHT -> PROMOTED_KNIGHT_VALUE;
            case PROMOTED_LANCE -> PROMOTED_LANCE_VALUE;
            case PROMOTED_PAWN -> PROMOTED_PAWN_VALUE;
        };
    }
}
