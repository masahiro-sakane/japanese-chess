package com.japanesechess.ai;

import com.japanesechess.domain.Board;
import com.japanesechess.domain.Move;
import com.japanesechess.domain.PlayerColor;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class RandomStrategy implements AiStrategy {

    private final MoveGenerator moveGenerator = new MoveGenerator();
    private final Random random = new Random();

    @Override
    public Optional<Move> selectMove(Board board, PlayerColor player) {
        List<Move> moves = moveGenerator.generateAllMoves(board, player);
        if (moves.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(moves.get(random.nextInt(moves.size())));
    }
}
