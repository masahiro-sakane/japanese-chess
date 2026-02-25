package com.japanesechess.command;

import com.japanesechess.domain.AiDifficulty;

import java.util.UUID;

public class CreateGameCommand {
    private final UUID gameId;
    private final UUID blackPlayerId;
    private final UUID whitePlayerId;
    private final boolean aiGame;
    private final AiDifficulty aiDifficulty;

    public CreateGameCommand(UUID gameId, UUID blackPlayerId, UUID whitePlayerId) {
        this.gameId = gameId;
        this.blackPlayerId = blackPlayerId;
        this.whitePlayerId = whitePlayerId;
        this.aiGame = false;
        this.aiDifficulty = null;
    }

    public CreateGameCommand(UUID gameId, UUID blackPlayerId, UUID whitePlayerId, AiDifficulty aiDifficulty) {
        this.gameId = gameId;
        this.blackPlayerId = blackPlayerId;
        this.whitePlayerId = whitePlayerId;
        this.aiGame = true;
        this.aiDifficulty = aiDifficulty;
    }

    public UUID getGameId() { return gameId; }
    public UUID getBlackPlayerId() { return blackPlayerId; }
    public UUID getWhitePlayerId() { return whitePlayerId; }
    public boolean isAiGame() { return aiGame; }
    public AiDifficulty getAiDifficulty() { return aiDifficulty; }
}
