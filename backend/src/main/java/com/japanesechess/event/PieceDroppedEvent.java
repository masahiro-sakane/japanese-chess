package com.japanesechess.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.japanesechess.domain.Move;
import com.japanesechess.domain.PieceType;
import com.japanesechess.domain.PlayerColor;
import com.japanesechess.domain.Position;
import lombok.Getter;

import java.util.UUID;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PieceDroppedEvent extends DomainEvent {
    private final Position position;
    private final PieceType pieceType;
    private final PlayerColor player;

    public PieceDroppedEvent(UUID gameId, Move move) {
        super(gameId, "PieceDropped");
        this.position = move.getTo();
        this.pieceType = move.getPieceType();
        this.player = move.getPlayer();
    }

    // Factory method for tests
    public static PieceDroppedEvent create(UUID gameId, Move move) {
        return new PieceDroppedEvent(gameId, move);
    }

    // Reconstruct Move object from event fields
    public Move getMove() {
        return Move.dropMove(position, pieceType, player);
    }

    @JsonCreator
    private PieceDroppedEvent(
        @JsonProperty("eventId") UUID eventId,
        @JsonProperty("aggregateId") UUID gameId,
        @JsonProperty("occurredAt") java.time.Instant occurredAt,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("position") Position position,
        @JsonProperty("pieceType") PieceType pieceType,
        @JsonProperty("player") PlayerColor player
    ) {
        super(eventId, gameId, occurredAt, eventType);
        this.position = position;
        this.pieceType = pieceType;
        this.player = player;
    }
}
