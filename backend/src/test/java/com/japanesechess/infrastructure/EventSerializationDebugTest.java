package com.japanesechess.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.japanesechess.event.GameCreatedEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class EventSerializationDebugTest {

    @Test
    void debugSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        UUID gameId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();
        UUID whitePlayerId = UUID.randomUUID();

        GameCreatedEvent event = new GameCreatedEvent(gameId, blackPlayerId, whitePlayerId);

        String json = mapper.writeValueAsString(event);
        System.out.println("Serialized JSON:");
        System.out.println(json);

        GameCreatedEvent deserialized = mapper.readValue(json, GameCreatedEvent.class);
        System.out.println("Deserialization successful!");
        System.out.println("Original gameId: " + event.getAggregateId());
        System.out.println("Deserialized gameId: " + deserialized.getAggregateId());
    }
}
