package com.japanesechess.infrastructure;

import com.eventstore.dbclient.*;
import com.japanesechess.domain.PlayerColor;
import com.japanesechess.event.DomainEvent;
import com.japanesechess.event.GameCreatedEvent;
import com.japanesechess.event.GameEndedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventStoreRepositoryTest {

    @Mock
    private EventStoreDBClient client;

    @Mock
    private EventSerializer serializer;

    private EventStoreRepository repository;

    @BeforeEach
    void setUp() {
        repository = new EventStoreRepository(client, serializer);
    }

    @Test
    void appendEvents_ShouldAppendToStream_WhenGivenEvents() throws Exception {
        UUID gameId = UUID.randomUUID();
        GameCreatedEvent event = new GameCreatedEvent(
            gameId,
            UUID.randomUUID(),
            UUID.randomUUID()
        );
        List<DomainEvent> events = List.of(event);

        EventData eventData = mock(EventData.class);
        when(serializer.serialize(event)).thenReturn(eventData);

        WriteResult writeResult = mock(WriteResult.class);
        CompletableFuture<WriteResult> future = CompletableFuture.completedFuture(writeResult);
        when(client.appendToStream(anyString(), any(AppendToStreamOptions.class), any(Iterator.class)))
            .thenReturn(future);

        assertDoesNotThrow(() -> repository.appendEvents(gameId, events, -1));

        verify(client, times(1)).appendToStream(
            eq("Game-" + gameId),
            any(AppendToStreamOptions.class),
            any(Iterator.class)
        );
    }

    @Test
    void appendEvents_ShouldDoNothing_WhenGivenEmptyList() {
        UUID gameId = UUID.randomUUID();
        List<DomainEvent> events = List.of();

        repository.appendEvents(gameId, events, -1);

        verify(client, never()).appendToStream(anyString(), any(), any(Iterator.class));
    }

    @Test
    void appendEvents_ShouldThrowException_WhenAppendFails() throws Exception {
        UUID gameId = UUID.randomUUID();
        GameCreatedEvent event = new GameCreatedEvent(
            gameId,
            UUID.randomUUID(),
            UUID.randomUUID()
        );
        List<DomainEvent> events = List.of(event);

        EventData eventData = mock(EventData.class);
        when(serializer.serialize(event)).thenReturn(eventData);

        CompletableFuture<WriteResult> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Connection failed"));
        when(client.appendToStream(anyString(), any(AppendToStreamOptions.class), any(Iterator.class)))
            .thenReturn(future);

        assertThrows(RuntimeException.class, () ->
            repository.appendEvents(gameId, events, -1)
        );
    }

    @Test
    void readEvents_ShouldReturnEvents_WhenStreamExists() throws Exception {
        UUID gameId = UUID.randomUUID();
        String streamName = "Game-" + gameId;

        GameCreatedEvent event1 = new GameCreatedEvent(
            gameId,
            UUID.randomUUID(),
            UUID.randomUUID()
        );
        GameEndedEvent event2 = new GameEndedEvent(
            gameId,
            PlayerColor.BLACK,
            GameEndedEvent.EndReason.CHECKMATE
        );

        RecordedEvent recordedEvent1 = mock(RecordedEvent.class);
        RecordedEvent recordedEvent2 = mock(RecordedEvent.class);

        ResolvedEvent resolvedEvent1 = mock(ResolvedEvent.class);
        ResolvedEvent resolvedEvent2 = mock(ResolvedEvent.class);
        when(resolvedEvent1.getOriginalEvent()).thenReturn(recordedEvent1);
        when(resolvedEvent2.getOriginalEvent()).thenReturn(recordedEvent2);

        ReadResult readResult = mock(ReadResult.class);
        when(readResult.getEvents()).thenReturn(List.of(resolvedEvent1, resolvedEvent2));

        CompletableFuture<ReadResult> future = CompletableFuture.completedFuture(readResult);
        when(client.readStream(eq(streamName), any(ReadStreamOptions.class)))
            .thenReturn(future);

        when(serializer.deserialize(recordedEvent1)).thenReturn(event1);
        when(serializer.deserialize(recordedEvent2)).thenReturn(event2);

        List<DomainEvent> events = repository.readEvents(gameId);

        assertEquals(2, events.size());
        assertEquals(event1, events.get(0));
        assertEquals(event2, events.get(1));
    }

    @Test
    void readEvents_ShouldReturnEmptyList_WhenStreamNotFound() throws Exception {
        UUID gameId = UUID.randomUUID();
        String streamName = "Game-" + gameId;

        CompletableFuture<ReadResult> future = new CompletableFuture<>();
        StreamNotFoundException exception = mock(StreamNotFoundException.class);
        future.completeExceptionally(exception);
        when(client.readStream(eq(streamName), any(ReadStreamOptions.class)))
            .thenReturn(future);

        List<DomainEvent> events = repository.readEvents(gameId);

        assertTrue(events.isEmpty());
    }

    @Test
    void getStreamVersion_ShouldReturnVersion_WhenStreamExists() throws Exception {
        UUID gameId = UUID.randomUUID();
        String streamName = "Game-" + gameId;

        RecordedEvent recordedEvent = mock(RecordedEvent.class);
        when(recordedEvent.getRevision()).thenReturn(5L);

        ResolvedEvent resolvedEvent = mock(ResolvedEvent.class);
        when(resolvedEvent.getOriginalEvent()).thenReturn(recordedEvent);

        ReadResult readResult = mock(ReadResult.class);
        when(readResult.getEvents()).thenReturn(List.of(resolvedEvent));

        CompletableFuture<ReadResult> future = CompletableFuture.completedFuture(readResult);
        when(client.readStream(eq(streamName), any(ReadStreamOptions.class)))
            .thenReturn(future);

        long version = repository.getStreamVersion(gameId);

        assertEquals(5L, version);
    }

    @Test
    void getStreamVersion_ShouldReturnMinusOne_WhenStreamNotFound() throws Exception {
        UUID gameId = UUID.randomUUID();
        String streamName = "Game-" + gameId;

        CompletableFuture<ReadResult> future = new CompletableFuture<>();
        StreamNotFoundException exception = mock(StreamNotFoundException.class);
        future.completeExceptionally(exception);
        when(client.readStream(eq(streamName), any(ReadStreamOptions.class)))
            .thenReturn(future);

        long version = repository.getStreamVersion(gameId);

        assertEquals(-1L, version);
    }

    @Test
    void getStreamVersion_ShouldReturnMinusOne_WhenStreamIsEmpty() throws Exception {
        UUID gameId = UUID.randomUUID();
        String streamName = "Game-" + gameId;

        ReadResult readResult = mock(ReadResult.class);
        when(readResult.getEvents()).thenReturn(List.of());

        CompletableFuture<ReadResult> future = CompletableFuture.completedFuture(readResult);
        when(client.readStream(eq(streamName), any(ReadStreamOptions.class)))
            .thenReturn(future);

        long version = repository.getStreamVersion(gameId);

        assertEquals(-1L, version);
    }
}
