package com.japanesechess.ai;

import com.japanesechess.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class MinimaxEngine {

    private static final int POSITIVE_INFINITY = Integer.MAX_VALUE / 2;
    private static final int NEGATIVE_INFINITY = Integer.MIN_VALUE / 2;
    private static final long MAX_SEARCH_TIME_MS = 10_000;

    private final AiMoveGenerator moveGenerator = new AiMoveGenerator();
    private final BoardEvaluator evaluator = new BoardEvaluator();
    private final Random random = new Random();

    private long searchStartTime;

    public Optional<Move> findBestMove(Board board, PlayerColor player, AiDifficulty difficulty) {
        int depth = difficulty.getSearchDepth();

        if (depth == 0) {
            return findRandomMove(board, player);
        }

        searchStartTime = System.currentTimeMillis();

        List<Move> legalMoves = moveGenerator.generateAllLegalMoves(board, player);
        if (legalMoves.isEmpty()) {
            return Optional.empty();
        }

        Move bestMove = legalMoves.get(0);
        int bestScore = NEGATIVE_INFINITY;

        for (Move move : legalMoves) {
            if (isTimeExceeded()) {
                break;
            }

            Board nextBoard = board.applyMove(move);
            int score = minimax(nextBoard, depth - 1, NEGATIVE_INFINITY, POSITIVE_INFINITY, false, player);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return Optional.of(bestMove);
    }

    private boolean isTimeExceeded() {
        return System.currentTimeMillis() - searchStartTime > MAX_SEARCH_TIME_MS;
    }

    private Optional<Move> findRandomMove(Board board, PlayerColor player) {
        List<Move> legalMoves = moveGenerator.generateAllLegalMoves(board, player);
        if (legalMoves.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(legalMoves.get(random.nextInt(legalMoves.size())));
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizing, PlayerColor aiPlayer) {
        if (depth == 0 || isTimeExceeded()) {
            return evaluator.evaluate(board, aiPlayer);
        }

        PlayerColor currentPlayer = isMaximizing ? aiPlayer : aiPlayer.opposite();
        List<Move> legalMoves = moveGenerator.generateAllLegalMoves(board, currentPlayer);

        if (legalMoves.isEmpty()) {
            return isMaximizing ? NEGATIVE_INFINITY : POSITIVE_INFINITY;
        }

        if (isMaximizing) {
            int maxScore = NEGATIVE_INFINITY;
            for (Move move : legalMoves) {
                if (isTimeExceeded()) {
                    break;
                }
                Board nextBoard = board.applyMove(move);
                int score = minimax(nextBoard, depth - 1, alpha, beta, false, aiPlayer);
                maxScore = Math.max(maxScore, score);
                alpha = Math.max(alpha, score);
                if (beta <= alpha) {
                    break;
                }
            }
            return maxScore;
        } else {
            int minScore = POSITIVE_INFINITY;
            for (Move move : legalMoves) {
                if (isTimeExceeded()) {
                    break;
                }
                Board nextBoard = board.applyMove(move);
                int score = minimax(nextBoard, depth - 1, alpha, beta, true, aiPlayer);
                minScore = Math.min(minScore, score);
                beta = Math.min(beta, score);
                if (beta <= alpha) {
                    break;
                }
            }
            return minScore;
        }
    }
}
