package com.japanesechess.infrastructure;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.RecordedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.japanesechess.event.DomainEvent;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.UUID;

@Component
public class EventSerializer {

    private final ObjectMapper objectMapper;

    public EventSerializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public EventData serialize(DomainEvent event) {
        try {
            String eventType = event.getClass().getSimpleName();
            String jsonString = objectMapper.writeValueAsString(event);

            return EventData.builderAsJson(
                UUID.randomUUID(),
                eventType,
                jsonString.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            ).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event: " + event, e);
        }
    }

    public DomainEvent deserialize(RecordedEvent recordedEvent) {
        try {
            String eventType = recordedEvent.getEventType();
            byte[] eventData = recordedEvent.getEventData();
            String rawDataAsString = new String(eventData, java.nio.charset.StandardCharsets.UTF_8);

            // Determine if data is Base64 encoded or already JSON
            byte[] jsonData;
            if (rawDataAsString.startsWith("{")) {
                // Data is already JSON
                jsonData = eventData;
            } else if (rawDataAsString.startsWith("\"") && rawDataAsString.endsWith("\"")) {
                // Data is a JSON string literal containing Base64
                String base64String = rawDataAsString.substring(1, rawDataAsString.length() - 1);
                jsonData = Base64.getDecoder().decode(base64String);
            } else {
                // Data is Base64 encoded directly
                jsonData = Base64.getDecoder().decode(eventData);
            }

            Class<?> eventClass = Class.forName("com.japanesechess.event." + eventType);
            return (DomainEvent) objectMapper.readValue(jsonData, eventClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event: " + recordedEvent.getEventType(), e);
        }
    }
}
