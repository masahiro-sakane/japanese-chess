package com.japanesechess.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.japanesechess.domain.PlayerColor;
import lombok.Getter;

import java.util.UUID;

@Getter
public class GameCreatedEvent extends DomainEvent {
    private final UUID blackPlayerId;
    private final UUID whitePlayerId;
    private final PlayerColor firstTurn;

    public GameCreatedEvent(UUID gameId, UUID blackPlayerId, UUID whitePlayerId) {
        super(gameId, "GameCreated");
        this.blackPlayerId = blackPlayerId;
        this.whitePlayerId = whitePlayerId;
        this.firstTurn = PlayerColor.BLACK;
    }

    // Factory method for tests
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
        @JsonProperty("firstTurn") PlayerColor firstTurn
    ) {
        super(eventId, gameId, occurredAt, eventType);
        this.blackPlayerId = blackPlayerId;
        this.whitePlayerId = whitePlayerId;
        this.firstTurn = firstTurn;
    }
}
