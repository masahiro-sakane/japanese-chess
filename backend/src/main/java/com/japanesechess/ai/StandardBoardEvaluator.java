package com.japanesechess.ai;

import com.japanesechess.domain.*;

import java.util.List;

public class StandardBoardEvaluator implements BoardEvaluator {

    private static final int PAWN_VALUE = 100;
    private static final int LANCE_VALUE = 300;
    private static final int KNIGHT_VALUE = 300;
    private static final int SILVER_VALUE = 500;
    private static final int GOLD_VALUE = 600;
    private static final int BISHOP_VALUE = 900;
    private static final int ROOK_VALUE = 1000;
    private static final int KING_VALUE = 10000;
    private static final int PROMOTED_PAWN_VALUE = 550;
    private static final int PROMOTED_LANCE_VALUE = 550;
    private static final int PROMOTED_KNIGHT_VALUE = 550;
    private static final int PROMOTED_SILVER_VALUE = 550;
    private static final int PROMOTED_BISHOP_VALUE = 1300;
    private static final int PROMOTED_ROOK_VALUE = 1400;

    @Override
    public int evaluate(Board board, PlayerColor player) {
        int score = 0;
        score += evaluatePieces(board, player);
        score += evaluateCapturedPieces(board, player);
        return score;
    }

    private int evaluatePieces(Board board, PlayerColor player) {
        int playerScore = 0;
        int opponentScore = 0;
        PlayerColor opponent = player.opposite();

        for (Piece piece : board.getAllPieces()) {
            int value = getPieceValue(piece.getType());
            if (piece.getOwner() == player) {
                playerScore += value;
            } else if (piece.getOwner() == opponent) {
                opponentScore += value;
            }
        }
        return playerScore - opponentScore;
    }

    private int evaluateCapturedPieces(Board board, PlayerColor player) {
        int playerScore = 0;
        int opponentScore = 0;
        PlayerColor opponent = player.opposite();

        for (PieceType type : board.getCapturedPieces(player)) {
            playerScore += getPieceValue(type) / 2;
        }
        for (PieceType type : board.getCapturedPieces(opponent)) {
            opponentScore += getPieceValue(type) / 2;
        }
        return playerScore - opponentScore;
    }

    private int getPieceValue(PieceType type) {
        return switch (type) {
            case PAWN -> PAWN_VALUE;
            case LANCE -> LANCE_VALUE;
            case KNIGHT -> KNIGHT_VALUE;
            case SILVER -> SILVER_VALUE;
            case GOLD -> GOLD_VALUE;
            case BISHOP -> BISHOP_VALUE;
            case ROOK -> ROOK_VALUE;
            case KING -> KING_VALUE;
            case PROMOTED_PAWN -> PROMOTED_PAWN_VALUE;
            case PROMOTED_LANCE -> PROMOTED_LANCE_VALUE;
            case PROMOTED_KNIGHT -> PROMOTED_KNIGHT_VALUE;
            case PROMOTED_SILVER -> PROMOTED_SILVER_VALUE;
            case PROMOTED_BISHOP -> PROMOTED_BISHOP_VALUE;
            case PROMOTED_ROOK -> PROMOTED_ROOK_VALUE;
        };
    }
}
