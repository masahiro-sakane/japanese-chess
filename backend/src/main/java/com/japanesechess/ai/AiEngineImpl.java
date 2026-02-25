package com.japanesechess.ai;

import com.japanesechess.domain.AiDifficulty;
import com.japanesechess.domain.Board;
import com.japanesechess.domain.Move;
import com.japanesechess.domain.PlayerColor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AiEngineImpl implements AiEngine {

    private static final int INTERMEDIATE_DEPTH = 2;
    private static final int ADVANCED_DEPTH = 4;

    private final AiStrategy randomStrategy = new RandomStrategy();
    private final AiStrategy intermediateStrategy = new MinimaxStrategy(INTERMEDIATE_DEPTH);
    private final AiStrategy advancedStrategy = new MinimaxStrategy(ADVANCED_DEPTH);

    @Override
    public Optional<Move> selectMove(Board board, PlayerColor player, AiDifficulty difficulty) {
        AiStrategy strategy = switch (difficulty) {
            case BEGINNER -> randomStrategy;
            case INTERMEDIATE -> intermediateStrategy;
            case ADVANCED -> advancedStrategy;
        };
        return strategy.selectMove(board, player);
    }
}
