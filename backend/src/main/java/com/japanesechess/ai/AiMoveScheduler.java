package com.japanesechess.ai;

import com.japanesechess.aggregate.Game;
import com.japanesechess.domain.AiDifficulty;
import com.japanesechess.domain.Move;
import com.japanesechess.domain.PlayerColor;
import com.japanesechess.repository.GameRepository;
import com.japanesechess.service.GameCommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class AiMoveScheduler {

    private final AiEngine aiEngine;
    private final AiGameRegistry aiGameRegistry;
    private final GameRepository gameRepository;

    @Lazy
    @Autowired
    private GameCommandHandler gameCommandHandler;

    public AiMoveScheduler(AiEngine aiEngine, AiGameRegistry aiGameRegistry, GameRepository gameRepository) {
        this.aiEngine = aiEngine;
        this.aiGameRegistry = aiGameRegistry;
        this.gameRepository = gameRepository;
    }

    @Async
    public void scheduleAiMove(UUID gameId) {
        if (!aiGameRegistry.isAiGame(gameId)) {
            return;
        }

        AiDifficulty difficulty = aiGameRegistry.getDifficulty(gameId)
            .orElse(AiDifficulty.BEGINNER);

        try {
            Game game = gameRepository.findById(gameId)
                .orElse(null);

            if (game == null) {
                log.warn("Game not found for AI move: {}", gameId);
                return;
            }

            if (game.getStatus() != Game.GameStatus.IN_PROGRESS) {
                return;
            }

            // AI always plays as WHITE
            if (game.getCurrentTurn() != PlayerColor.WHITE) {
                return;
            }

            Optional<Move> moveOpt = aiEngine.selectMove(game.getBoard(), PlayerColor.WHITE, difficulty);
            if (moveOpt.isEmpty()) {
                log.warn("AI could not find a move for game {}", gameId);
                return;
            }

            Move move = moveOpt.get();
            log.info("AI selected move for game {}: {}", gameId, move);
            gameCommandHandler.handleAiMove(gameId, move);
            // トランザクション完了後にWebSocket通知を送る
            gameCommandHandler.notifyAfterAiMove(gameId);

        } catch (Exception e) {
            log.error("AI move scheduling failed for game {}", gameId, e);
        }
    }
}
