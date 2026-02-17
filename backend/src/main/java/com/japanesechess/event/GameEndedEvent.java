package com.japanesechess.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.japanesechess.domain.PlayerColor;
import lombok.Getter;

import java.util.UUID;

@Getter
public class GameEndedEvent extends DomainEvent {
    private final PlayerColor winner;
    private final EndReason reason;

    public enum EndReason {
        CHECKMATE,
        RESIGNATION,
        TIMEOUT
    }

    public GameEndedEvent(UUID gameId, PlayerColor winner, EndReason reason) {
        super(gameId, "GameEnded");
        this.winner = winner;
        this.reason = reason;
    }

    // Factory method for tests
    public static GameEndedEvent create(UUID gameId, PlayerColor winner, String reasonStr) {
        EndReason reason = EndReason.valueOf(reasonStr.toUpperCase());
        return new GameEndedEvent(gameId, winner, reason);
    }

    @JsonCreator
    private GameEndedEvent(
        @JsonProperty("eventId") UUID eventId,
        @JsonProperty("aggregateId") UUID gameId,
        @JsonProperty("occurredAt") java.time.Instant occurredAt,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("winner") PlayerColor winner,
        @JsonProperty("reason") EndReason reason
    ) {
        super(eventId, gameId, occurredAt, eventType);
        this.winner = winner;
        this.reason = reason;
    }
}
