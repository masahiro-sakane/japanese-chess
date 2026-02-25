package com.japanesechess.ai;

import com.japanesechess.domain.Board;
import com.japanesechess.domain.CheckDetector;
import com.japanesechess.domain.Move;
import com.japanesechess.domain.PlayerColor;

import java.util.List;
import java.util.Optional;

public class MinimaxStrategy implements AiStrategy {

    private static final int CHECKMATE_SCORE = 100000;

    private final int depth;
    private final MoveGenerator moveGenerator = new MoveGenerator();
    private final BoardEvaluator evaluator = new StandardBoardEvaluator();
    private final CheckDetector checkDetector = new CheckDetector();

    public MinimaxStrategy(int depth) {
        this.depth = depth;
    }

    @Override
    public Optional<Move> selectMove(Board board, PlayerColor player) {
        List<Move> moves = moveGenerator.generateAllMoves(board, player);
        if (moves.isEmpty()) {
            return Optional.empty();
        }

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (Move move : moves) {
            Board newBoard = board.applyMove(move);
            int score = minimax(newBoard, depth - 1, alpha, beta, false, player);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, score);
        }

        return Optional.ofNullable(bestMove);
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizing, PlayerColor aiPlayer) {
        PlayerColor currentPlayer = isMaximizing ? aiPlayer : aiPlayer.opposite();

        if (checkDetector.isCheckmate(board, currentPlayer)) {
            return isMaximizing ? -CHECKMATE_SCORE : CHECKMATE_SCORE;
        }

        if (depth == 0) {
            return evaluator.evaluate(board, aiPlayer);
        }

        List<Move> moves = moveGenerator.generateAllMoves(board, currentPlayer);
        if (moves.isEmpty()) {
            return evaluator.evaluate(board, aiPlayer);
        }

        if (isMaximizing) {
            int maxScore = Integer.MIN_VALUE;
            for (Move move : moves) {
                Board newBoard = board.applyMove(move);
                int score = minimax(newBoard, depth - 1, alpha, beta, false, aiPlayer);
                maxScore = Math.max(maxScore, score);
                alpha = Math.max(alpha, score);
                if (beta <= alpha) {
                    break;
                }
            }
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            for (Move move : moves) {
                Board newBoard = board.applyMove(move);
                int score = minimax(newBoard, depth - 1, alpha, beta, true, aiPlayer);
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
