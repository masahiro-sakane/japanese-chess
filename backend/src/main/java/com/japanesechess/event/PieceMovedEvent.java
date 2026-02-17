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
public class PieceMovedEvent extends DomainEvent {
    private final Position from;
    private final Position to;
    private final PieceType pieceType;
    private final PlayerColor player;
    private final boolean promoted;
    private final PieceType capturedPiece;

    public PieceMovedEvent(UUID gameId, Move move) {
        super(gameId, "PieceMoved");
        this.from = move.getFrom();
        this.to = move.getTo();
        this.pieceType = move.getPieceType();
        this.player = move.getPlayer();
        this.promoted = move.isPromote();
        this.capturedPiece = move.getCapturedPiece();
    }

    // Factory method for tests
    public static PieceMovedEvent create(UUID gameId, Move move) {
        return new PieceMovedEvent(gameId, move);
    }

    // Reconstruct Move object from event fields
    public Move getMove() {
        Move baseMove = promoted
            ? Move.promoteMove(from, to, pieceType, player)
            : Move.normalMove(from, to, pieceType, player);

        return capturedPiece != null
            ? baseMove.withCapturedPiece(capturedPiece)
            : baseMove;
    }

    @JsonCreator
    private PieceMovedEvent(
        @JsonProperty("eventId") UUID eventId,
        @JsonProperty("aggregateId") UUID gameId,
        @JsonProperty("occurredAt") java.time.Instant occurredAt,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("from") Position from,
        @JsonProperty("to") Position to,
        @JsonProperty("pieceType") PieceType pieceType,
        @JsonProperty("player") PlayerColor player,
        @JsonProperty("promoted") boolean promoted,
        @JsonProperty("capturedPiece") PieceType capturedPiece
    ) {
        super(eventId, gameId, occurredAt, eventType);
        this.from = from;
        this.to = to;
        this.pieceType = pieceType;
        this.player = player;
        this.promoted = promoted;
        this.capturedPiece = capturedPiece;
    }
}
