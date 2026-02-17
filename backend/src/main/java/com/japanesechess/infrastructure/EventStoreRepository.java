package com.japanesechess.infrastructure;

import com.eventstore.dbclient.*;
import com.japanesechess.event.DomainEvent;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Repository
public class EventStoreRepository {

    private final EventStoreDBClient client;
    private final EventSerializer serializer;

    public EventStoreRepository(EventStoreDBClient client, EventSerializer serializer) {
        this.client = client;
        this.serializer = serializer;
    }

    public void appendEvents(UUID streamId, List<DomainEvent> events, long expectedVersion) {
        if (events.isEmpty()) {
            return;
        }

        String streamName = getStreamName(streamId);
        List<EventData> eventDataList = new ArrayList<>();

        for (DomainEvent event : events) {
            eventDataList.add(serializer.serialize(event));
        }

        AppendToStreamOptions options = AppendToStreamOptions.get();
        if (expectedVersion == -1) {
            options.expectedRevision(ExpectedRevision.noStream());
        } else {
            options.expectedRevision(ExpectedRevision.expectedRevision(expectedVersion));
        }

        try {
            client.appendToStream(streamName, options, eventDataList.iterator())
                .get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while appending events to stream: " + streamName, e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof WrongExpectedVersionException) {
                throw new ConcurrentModificationException(
                    "Concurrent modification detected for stream: " + streamName +
                    ". Expected version: " + expectedVersion, e);
            }
            throw new RuntimeException("Failed to append events to stream: " + streamName, e);
        }
    }

    public List<DomainEvent> readEvents(UUID streamId) {
        String streamName = getStreamName(streamId);
        List<DomainEvent> events = new ArrayList<>();

        try {
            ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromStart();

            ReadResult result = client.readStream(streamName, options)
                .get();

            for (ResolvedEvent resolvedEvent : result.getEvents()) {
                RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
                DomainEvent event = serializer.deserialize(recordedEvent);
                events.add(event);
            }

            return events;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while reading events from stream: " + streamName, e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof StreamNotFoundException) {
                return new ArrayList<>();
            }
            throw new RuntimeException("Failed to read events from stream: " + streamName, e);
        }
    }

    public long getStreamVersion(UUID streamId) {
        String streamName = getStreamName(streamId);

        try {
            ReadStreamOptions options = ReadStreamOptions.get()
                .backwards()
                .fromEnd()
                .maxCount(1);

            ReadResult result = client.readStream(streamName, options)
                .get();

            List<ResolvedEvent> events = result.getEvents();
            if (events.isEmpty()) {
                return -1;
            }

            return events.get(0).getOriginalEvent().getRevision();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while getting stream version: " + streamName, e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof StreamNotFoundException) {
                return -1;
            }
            throw new RuntimeException("Failed to get stream version: " + streamName, e);
        }
    }

    private String getStreamName(UUID streamId) {
        return "game-" + streamId;
    }
}
