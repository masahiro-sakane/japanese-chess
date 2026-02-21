package com.japanesechess.ai;

import com.japanesechess.domain.AiDifficulty;
import com.japanesechess.readmodel.GameEntity;
import com.japanesechess.readmodel.GameEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiGameRegistryInitializer {

    private final GameEntityRepository gameEntityRepository;
    private final AiGameRegistry aiGameRegistry;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeRegistry() {
        try {
            List<GameEntity> aiGames = gameEntityRepository.findAll().stream()
                .filter(g -> Boolean.TRUE.equals(g.getIsAiGame())
                    && "IN_PROGRESS".equals(g.getStatus())
                    && g.getAiDifficulty() != null)
                .toList();

            for (GameEntity game : aiGames) {
                try {
                    AiDifficulty difficulty = AiDifficulty.valueOf(game.getAiDifficulty());
                    aiGameRegistry.register(game.getGameId(), difficulty);
                    log.info("Restored AI game {} with difficulty {}", game.getGameId(), difficulty);
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown AI difficulty '{}' for game {}", game.getAiDifficulty(), game.getGameId());
                }
            }
            log.info("AI game registry initialized with {} games", aiGames.size());
        } catch (Exception e) {
            log.error("Failed to initialize AI game registry", e);
        }
    }
}
