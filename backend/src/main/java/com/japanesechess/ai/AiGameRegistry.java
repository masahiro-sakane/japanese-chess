package com.japanesechess.ai;

import com.japanesechess.domain.AiDifficulty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiGameRegistry {

    private final Map<UUID, AiDifficulty> aiGames = new ConcurrentHashMap<>();

    public void register(UUID gameId, AiDifficulty difficulty) {
        aiGames.put(gameId, difficulty);
    }

    public void unregister(UUID gameId) {
        aiGames.remove(gameId);
    }

    public boolean isAiGame(UUID gameId) {
        return aiGames.containsKey(gameId);
    }

    public Optional<AiDifficulty> getDifficulty(UUID gameId) {
        return Optional.ofNullable(aiGames.get(gameId));
    }
}
