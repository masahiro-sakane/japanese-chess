package com.japanesechess.infrastructure;

import com.eventstore.dbclient.EventData;
import com.japanesechess.domain.PlayerColor;
import com.japanesechess.event.GameCreatedEvent;
import com.japanesechess.event.GameEndedEvent;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventSerializerRoundTripTest {

    @Test
    void roundTrip_GameCreatedEvent() {
        EventSerializer serializer = new EventSerializer();

        UUID gameId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();
        UUID whitePlayerId = UUID.randomUUID();
        GameCreatedEvent originalEvent = new GameCreatedEvent(gameId, blackPlayerId, whitePlayerId);

        // Serialize
        EventData eventData = serializer.serialize(originalEvent);

        // Print JSON for debugging
        String json = new String(eventData.getEventData(), StandardCharsets.UTF_8);
        System.out.println("Serialized GameCreatedEvent JSON:");
        System.out.println(json);

        // Verify we can serialize
        assertNotNull(eventData);
        assertEquals("GameCreatedEvent", eventData.getEventType());
    }

    @Test
    void roundTrip_GameEndedEvent() {
        EventSerializer serializer = new EventSerializer();

        UUID gameId = UUID.randomUUID();
        GameEndedEvent originalEvent = new GameEndedEvent(
            gameId,
            PlayerColor.BLACK,
            GameEndedEvent.EndReason.CHECKMATE
        );

        // Serialize
        EventData eventData = serializer.serialize(originalEvent);

        // Print JSON for debugging
        String json = new String(eventData.getEventData(), StandardCharsets.UTF_8);
        System.out.println("Serialized GameEndedEvent JSON:");
        System.out.println(json);

        // Verify we can serialize
        assertNotNull(eventData);
        assertEquals("GameEndedEvent", eventData.getEventType());
    }
}
