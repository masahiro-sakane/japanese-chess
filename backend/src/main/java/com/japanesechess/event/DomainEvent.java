package com.japanesechess.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class DomainEvent {
    private final UUID eventId;
    private final UUID aggregateId;
    private final Instant occurredAt;
    private final String eventType;

    protected DomainEvent(UUID aggregateId, String eventType) {
        this.eventId = UUID.randomUUID();
        this.aggregateId = aggregateId;
        this.occurredAt = Instant.now();
        this.eventType = eventType;
    }

    protected DomainEvent(
        @JsonProperty("eventId") UUID eventId,
        @JsonProperty("aggregateId") UUID aggregateId,
        @JsonProperty("occurredAt") Instant occurredAt,
        @JsonProperty("eventType") String eventType
    ) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.occurredAt = occurredAt;
        this.eventType = eventType;
    }
}
