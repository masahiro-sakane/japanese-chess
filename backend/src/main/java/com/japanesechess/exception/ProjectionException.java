package com.japanesechess.exception;

import java.util.UUID;

public class ProjectionException extends RuntimeException {
    private final UUID gameId;
    private final String eventType;

    public ProjectionException(UUID gameId, String eventType, String message, Throwable cause) {
        super(
            String.format(
                "Projection failed for game %s, event %s: %s",
                gameId,
                eventType,
                message
            ),
            cause
        );
        this.gameId = gameId;
        this.eventType = eventType;
    }

    public ProjectionException(UUID gameId, String eventType, String message) {
        this(gameId, eventType, message, null);
    }

    public UUID getGameId() {
        return gameId;
    }

    public String getEventType() {
        return eventType;
    }
}
