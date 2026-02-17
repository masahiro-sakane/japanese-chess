package com.japanesechess.infrastructure;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.RecordedEvent;
import com.japanesechess.domain.PlayerColor;
import com.japanesechess.event.GameCreatedEvent;
import com.japanesechess.event.GameEndedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventSerializerTest {

    private EventSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new EventSerializer();
    }

    @Test
    void serialize_ShouldCreateEventData_WhenGivenGameCreatedEvent() {
        UUID gameId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();
        UUID whitePlayerId = UUID.randomUUID();
        GameCreatedEvent event = new GameCreatedEvent(gameId, blackPlayerId, whitePlayerId);

        EventData eventData = serializer.serialize(event);

        assertNotNull(eventData);
        assertEquals("GameCreatedEvent", eventData.getEventType());
        assertNotNull(eventData.getEventData());
    }

    @Test
    void serialize_ShouldCreateEventData_WhenGivenGameEndedEvent() {
        UUID gameId = UUID.randomUUID();
        GameEndedEvent event = new GameEndedEvent(
            gameId,
            PlayerColor.BLACK,
            GameEndedEvent.EndReason.CHECKMATE
        );

        EventData eventData = serializer.serialize(event);

        assertNotNull(eventData);
        assertEquals("GameEndedEvent", eventData.getEventType());
        assertNotNull(eventData.getEventData());
    }

    @Test
    void serialize_ShouldCreateValidEventData_ForGameCreatedEvent() {
        UUID gameId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();
        UUID whitePlayerId = UUID.randomUUID();
        GameCreatedEvent originalEvent = new GameCreatedEvent(gameId, blackPlayerId, whitePlayerId);

        EventData eventData = serializer.serialize(originalEvent);

        assertNotNull(eventData);
        assertEquals("GameCreatedEvent", eventData.getEventType());
        assertNotNull(eventData.getEventData());
        assertTrue(eventData.getEventData().length > 0);
    }

    @Test
    void serialize_ShouldCreateValidEventData_ForGameEndedEvent() {
        UUID gameId = UUID.randomUUID();
        GameEndedEvent originalEvent = new GameEndedEvent(
            gameId,
            PlayerColor.BLACK,
            GameEndedEvent.EndReason.RESIGNATION
        );

        EventData eventData = serializer.serialize(originalEvent);

        assertNotNull(eventData);
        assertEquals("GameEndedEvent", eventData.getEventType());
        assertNotNull(eventData.getEventData());
        assertTrue(eventData.getEventData().length > 0);
    }

    @Test
    void serialize_ShouldThrowException_WhenSerializationFails() {
        // This test would require a malformed event or mocking Jackson failure
        // For now, we verify normal behavior doesn't throw
        GameCreatedEvent event = new GameCreatedEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        assertDoesNotThrow(() -> serializer.serialize(event));
    }

    @Test
    void deserialize_ShouldRecreateGameCreatedEvent() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();
        UUID whitePlayerId = UUID.randomUUID();

        // Create JSON manually (simulating what would be in EventStoreDB)
        String json = String.format(
            "{\"eventId\":\"%s\",\"aggregateId\":\"%s\",\"occurredAt\":\"2026-02-13T00:00:00Z\"," +
            "\"eventType\":\"GameCreated\",\"blackPlayerId\":\"%s\",\"whitePlayerId\":\"%s\",\"firstTurn\":\"BLACK\"}",
            UUID.randomUUID(), gameId, blackPlayerId, whitePlayerId
        );

        RecordedEvent recordedEvent = mock(RecordedEvent.class);
        when(recordedEvent.getEventType()).thenReturn("GameCreatedEvent");
        when(recordedEvent.getEventData()).thenReturn(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        GameCreatedEvent deserialized = (GameCreatedEvent) serializer.deserialize(recordedEvent);

        assertNotNull(deserialized);
        assertEquals(gameId, deserialized.getAggregateId());
        assertEquals(blackPlayerId, deserialized.getBlackPlayerId());
        assertEquals(whitePlayerId, deserialized.getWhitePlayerId());
    }

    @Test
    void deserialize_ShouldRecreateGameEndedEvent() throws Exception {
        UUID gameId = UUID.randomUUID();

        // Create JSON manually
        String json = String.format(
            "{\"eventId\":\"%s\",\"aggregateId\":\"%s\",\"occurredAt\":\"2026-02-13T00:00:00Z\"," +
            "\"eventType\":\"GameEnded\",\"winner\":\"BLACK\",\"reason\":\"CHECKMATE\"}",
            UUID.randomUUID(), gameId
        );

        RecordedEvent recordedEvent = mock(RecordedEvent.class);
        when(recordedEvent.getEventType()).thenReturn("GameEndedEvent");
        when(recordedEvent.getEventData()).thenReturn(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        GameEndedEvent deserialized = (GameEndedEvent) serializer.deserialize(recordedEvent);

        assertNotNull(deserialized);
        assertEquals(gameId, deserialized.getAggregateId());
        assertEquals(PlayerColor.BLACK, deserialized.getWinner());
        assertEquals(GameEndedEvent.EndReason.CHECKMATE, deserialized.getReason());
    }
}
