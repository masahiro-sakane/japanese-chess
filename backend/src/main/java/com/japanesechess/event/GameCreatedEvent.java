package com.japanesechess.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.japanesechess.domain.AiDifficulty;
import com.japanesechess.domain.PlayerColor;
import lombok.Getter;

import java.util.UUID;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameCreatedEvent extends DomainEvent {
    private final UUID blackPlayerId;
    private final UUID whitePlayerId;
    private final PlayerColor firstTurn;
    private final boolean aiGame;
    private final AiDifficulty aiDifficulty;

    public GameCreatedEvent(UUID gameId, UUID blackPlayerId, UUID whitePlayerId) {
        super(gameId, "GameCreated");
        this.blackPlayerId = blackPlayerId;
        this.whitePlayerId = whitePlayerId;
        this.firstTurn = PlayerColor.BLACK;
        this.aiGame = false;
        this.aiDifficulty = null;
    }

    public GameCreatedEvent(UUID gameId, UUID blackPlayerId, UUID whitePlayerId, AiDifficulty aiDifficulty) {
        super(gameId, "GameCreated");
        this.blackPlayerId = blackPlayerId;
        this.whitePlayerId = whitePlayerId;
        this.firstTurn = PlayerColor.BLACK;
        this.aiGame = true;
        this.aiDifficulty = aiDifficulty;
    }

    // Factory methods for tests
    public static GameCreatedEvent create(UUID gameId, UUID blackPlayerId, UUID whitePlayerId) {
        return new GameCreatedEvent(gameId, blackPlayerId, whitePlayerId);
    }

    @JsonCreator
    private GameCreatedEvent(
        @JsonProperty("eventId") UUID eventId,
        @JsonProperty("aggregateId") UUID gameId,
        @JsonProperty("occurredAt") java.time.Instant occurredAt,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("blackPlayerId") UUID blackPlayerId,
        @JsonProperty("whitePlayerId") UUID whitePlayerId,
        @JsonProperty("firstTurn") PlayerColor firstTurn,
        @JsonProperty("aiGame") boolean aiGame,
        @JsonProperty("aiDifficulty") AiDifficulty aiDifficulty
    ) {
        super(eventId, gameId, occurredAt, eventType);
        this.blackPlayerId = blackPlayerId;
        this.whitePlayerId = whitePlayerId;
        this.firstTurn = firstTurn != null ? firstTurn : PlayerColor.BLACK;
        this.aiGame = aiGame;
        this.aiDifficulty = aiDifficulty;
    }
}
