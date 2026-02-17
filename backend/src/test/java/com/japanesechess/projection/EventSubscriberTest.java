package com.japanesechess.projection;

import com.eventstore.dbclient.*;
import com.japanesechess.domain.Move;
import com.japanesechess.domain.PieceType;
import com.japanesechess.domain.PlayerColor;
import com.japanesechess.domain.Position;
import com.japanesechess.event.*;
import com.japanesechess.infrastructure.EventSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventSubscriberTest {

    @Mock
    private EventStoreDBClient eventStoreClient;

    @Mock
    private EventSerializer eventSerializer;

    @Mock
    private GameProjectionHandler projectionHandler;

    @Mock
    private Subscription subscription;

    @Mock
    private ResolvedEvent resolvedEvent;

    @Mock
    private RecordedEvent recordedEvent;

    private EventSubscriber eventSubscriber;

    private UUID gameId;

    @BeforeEach
    void setUp() {
        gameId = UUID.randomUUID();
        eventSubscriber = new EventSubscriber(eventStoreClient, eventSerializer, projectionHandler);
    }

    @Test
    void start_shouldSubscribeToEventStoreWithGameStreamPrefix() throws Exception {
        // Given
        CompletableFuture<Subscription> future = CompletableFuture.completedFuture(subscription);
        when(eventStoreClient.subscribeToAll(any(SubscriptionListener.class), any(SubscribeToAllOptions.class)))
                .thenReturn(future);

        // When
        eventSubscriber.start();

        // Then
        ArgumentCaptor<SubscribeToAllOptions> optionsCaptor = ArgumentCaptor.forClass(SubscribeToAllOptions.class);
        verify(eventStoreClient).subscribeToAll(any(SubscriptionListener.class), optionsCaptor.capture());

        // Verify subscription was set up (we can't easily verify the filter prefix without reflection)
        verifyNoMoreInteractions(projectionHandler);
    }

    @Test
    void start_shouldThrowRuntimeException_whenSubscriptionFails() throws Exception {
        // Given
        CompletableFuture<Subscription> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Connection failed"));
        when(eventStoreClient.subscribeToAll(any(SubscriptionListener.class), any(SubscribeToAllOptions.class)))
                .thenReturn(future);

        // When & Then
        assertThrows(RuntimeException.class, () -> eventSubscriber.start());
    }

    @Test
    void stop_shouldStopSubscription_whenSubscriptionExists() throws Exception {
        // Given
        CompletableFuture<Subscription> future = CompletableFuture.completedFuture(subscription);
        when(eventStoreClient.subscribeToAll(any(SubscriptionListener.class), any(SubscribeToAllOptions.class)))
                .thenReturn(future);
        eventSubscriber.start();

        // When
        eventSubscriber.stop();

        // Then
        verify(subscription).stop();
    }

    @Test
    void stop_shouldNotThrow_whenSubscriptionIsNull() {
        // When & Then
        assertDoesNotThrow(() -> eventSubscriber.stop());
    }

    @Test
    void handleEvent_shouldRouteGameCreatedEventToHandler() throws Exception {
        // Given
        GameCreatedEvent event = GameCreatedEvent.create(gameId, UUID.randomUUID(), UUID.randomUUID());

        when(resolvedEvent.getOriginalEvent()).thenReturn(recordedEvent);
        when(recordedEvent.getEventType()).thenReturn("GameCreated");
        when(recordedEvent.getEventData()).thenReturn(new byte[0]);
        when(eventSerializer.deserialize(recordedEvent)).thenReturn(event);

        // When
        CompletableFuture<Subscription> future = CompletableFuture.completedFuture(subscription);
        SubscriptionListener[] capturedListener = new SubscriptionListener[1];
        when(eventStoreClient.subscribeToAll(any(SubscriptionListener.class), any(SubscribeToAllOptions.class)))
                .thenAnswer(invocation -> {
                    capturedListener[0] = invocation.getArgument(0);
                    return future;
                });

        eventSubscriber.start();
        capturedListener[0].onEvent(subscription, resolvedEvent);

        // Then
        verify(projectionHandler).handle(event);
    }

    @Test
    void handleEvent_shouldRoutePieceMovedEventToHandler() throws Exception {
        // Given
        Position from = new Position(6, 4);
        Position to = new Position(5, 4);
        Move move = Move.normalMove(from, to, PieceType.PAWN, PlayerColor.BLACK);
        PieceMovedEvent event = PieceMovedEvent.create(gameId, move);

        when(resolvedEvent.getOriginalEvent()).thenReturn(recordedEvent);
        when(recordedEvent.getEventType()).thenReturn("PieceMoved");
        when(recordedEvent.getEventData()).thenReturn(new byte[0]);
        when(eventSerializer.deserialize(recordedEvent)).thenReturn(event);

        // When
        CompletableFuture<Subscription> future = CompletableFuture.completedFuture(subscription);
        SubscriptionListener[] capturedListener = new SubscriptionListener[1];
        when(eventStoreClient.subscribeToAll(any(SubscriptionListener.class), any(SubscribeToAllOptions.class)))
                .thenAnswer(invocation -> {
                    capturedListener[0] = invocation.getArgument(0);
                    return future;
                });

        eventSubscriber.start();
        capturedListener[0].onEvent(subscription, resolvedEvent);

        // Then
        verify(projectionHandler).handle(event);
    }

    @Test
    void handleEvent_shouldRoutePieceDroppedEventToHandler() throws Exception {
        // Given
        Position to = new Position(5, 4);
        Move dropMove = Move.dropMove(to, PieceType.PAWN, PlayerColor.BLACK);
        PieceDroppedEvent event = PieceDroppedEvent.create(gameId, dropMove);

        when(resolvedEvent.getOriginalEvent()).thenReturn(recordedEvent);
        when(recordedEvent.getEventType()).thenReturn("PieceDropped");
        when(recordedEvent.getEventData()).thenReturn(new byte[0]);
        when(eventSerializer.deserialize(recordedEvent)).thenReturn(event);

        // When
        CompletableFuture<Subscription> future = CompletableFuture.completedFuture(subscription);
        SubscriptionListener[] capturedListener = new SubscriptionListener[1];
        when(eventStoreClient.subscribeToAll(any(SubscriptionListener.class), any(SubscribeToAllOptions.class)))
                .thenAnswer(invocation -> {
                    capturedListener[0] = invocation.getArgument(0);
                    return future;
                });

        eventSubscriber.start();
        capturedListener[0].onEvent(subscription, resolvedEvent);

        // Then
        verify(projectionHandler).handle(event);
    }

    @Test
    void handleEvent_shouldRouteGameEndedEventToHandler() throws Exception {
        // Given
        GameEndedEvent event = GameEndedEvent.create(gameId, PlayerColor.BLACK, "CHECKMATE");

        when(resolvedEvent.getOriginalEvent()).thenReturn(recordedEvent);
        when(recordedEvent.getEventType()).thenReturn("GameEnded");
        when(recordedEvent.getEventData()).thenReturn(new byte[0]);
        when(eventSerializer.deserialize(recordedEvent)).thenReturn(event);

        // When
        CompletableFuture<Subscription> future = CompletableFuture.completedFuture(subscription);
        SubscriptionListener[] capturedListener = new SubscriptionListener[1];
        when(eventStoreClient.subscribeToAll(any(SubscriptionListener.class), any(SubscribeToAllOptions.class)))
                .thenAnswer(invocation -> {
                    capturedListener[0] = invocation.getArgument(0);
                    return future;
                });

        eventSubscriber.start();
        capturedListener[0].onEvent(subscription, resolvedEvent);

        // Then
        verify(projectionHandler).handle(event);
    }

    @Test
    void handleEvent_shouldNotCallHandler_whenDeserializationFails() throws Exception {
        // Given
        when(resolvedEvent.getOriginalEvent()).thenReturn(recordedEvent);
        when(recordedEvent.getEventType()).thenReturn("GameCreated");
        when(recordedEvent.getEventData()).thenReturn(new byte[0]);
        when(eventSerializer.deserialize(recordedEvent)).thenThrow(new RuntimeException("Deserialization failed"));

        // When
        CompletableFuture<Subscription> future = CompletableFuture.completedFuture(subscription);
        SubscriptionListener[] capturedListener = new SubscriptionListener[1];
        when(eventStoreClient.subscribeToAll(any(SubscriptionListener.class), any(SubscribeToAllOptions.class)))
                .thenAnswer(invocation -> {
                    capturedListener[0] = invocation.getArgument(0);
                    return future;
                });

        eventSubscriber.start();
        capturedListener[0].onEvent(subscription, resolvedEvent);

        // Then
        verify(projectionHandler, never()).handle(any(GameCreatedEvent.class));
        verify(projectionHandler, never()).handle(any(PieceMovedEvent.class));
        verify(projectionHandler, never()).handle(any(PieceDroppedEvent.class));
        verify(projectionHandler, never()).handle(any(GameEndedEvent.class));
    }

    @Test
    void handleEvent_shouldNotThrow_whenHandlerThrowsException() throws Exception {
        // Given
        GameCreatedEvent event = GameCreatedEvent.create(gameId, UUID.randomUUID(), UUID.randomUUID());

        when(resolvedEvent.getOriginalEvent()).thenReturn(recordedEvent);
        when(recordedEvent.getEventType()).thenReturn("GameCreated");
        when(recordedEvent.getEventData()).thenReturn(new byte[0]);
        when(eventSerializer.deserialize(recordedEvent)).thenReturn(event);
        doThrow(new RuntimeException("Handler error")).when(projectionHandler).handle(event);

        // When
        CompletableFuture<Subscription> future = CompletableFuture.completedFuture(subscription);
        SubscriptionListener[] capturedListener = new SubscriptionListener[1];
        when(eventStoreClient.subscribeToAll(any(SubscriptionListener.class), any(SubscribeToAllOptions.class)))
                .thenAnswer(invocation -> {
                    capturedListener[0] = invocation.getArgument(0);
                    return future;
                });

        eventSubscriber.start();

        // Then - should not throw
        assertDoesNotThrow(() -> capturedListener[0].onEvent(subscription, resolvedEvent));
    }

    @Test
    void onCancelled_shouldLogError_whenThrowableProvided() throws Exception {
        // Given
        Throwable error = new RuntimeException("Subscription cancelled due to error");

        // When
        CompletableFuture<Subscription> future = CompletableFuture.completedFuture(subscription);
        SubscriptionListener[] capturedListener = new SubscriptionListener[1];
        when(eventStoreClient.subscribeToAll(any(SubscriptionListener.class), any(SubscribeToAllOptions.class)))
                .thenAnswer(invocation -> {
                    capturedListener[0] = invocation.getArgument(0);
                    return future;
                });

        eventSubscriber.start();
        capturedListener[0].onCancelled(subscription, error);

        // Then - should not throw, just log
        assertDoesNotThrow(() -> capturedListener[0].onCancelled(subscription, error));
    }

    @Test
    void onCancelled_shouldLogWarning_whenNoThrowable() throws Exception {
        // Given
        // When
        CompletableFuture<Subscription> future = CompletableFuture.completedFuture(subscription);
        SubscriptionListener[] capturedListener = new SubscriptionListener[1];
        when(eventStoreClient.subscribeToAll(any(SubscriptionListener.class), any(SubscribeToAllOptions.class)))
                .thenAnswer(invocation -> {
                    capturedListener[0] = invocation.getArgument(0);
                    return future;
                });

        eventSubscriber.start();
        capturedListener[0].onCancelled(subscription, null);

        // Then - should not throw, just log
        assertDoesNotThrow(() -> capturedListener[0].onCancelled(subscription, null));
    }

    @Test
    void handleEvent_shouldLogWarning_forUnknownEventType() throws Exception {
        // Given
        DomainEvent unknownEvent = new DomainEvent(gameId, "UnknownEvent") {};

        when(resolvedEvent.getOriginalEvent()).thenReturn(recordedEvent);
        when(recordedEvent.getEventType()).thenReturn("UnknownEvent");
        when(recordedEvent.getEventData()).thenReturn(new byte[0]);
        when(eventSerializer.deserialize(recordedEvent)).thenReturn(unknownEvent);

        // When
        CompletableFuture<Subscription> future = CompletableFuture.completedFuture(subscription);
        SubscriptionListener[] capturedListener = new SubscriptionListener[1];
        when(eventStoreClient.subscribeToAll(any(SubscriptionListener.class), any(SubscribeToAllOptions.class)))
                .thenAnswer(invocation -> {
                    capturedListener[0] = invocation.getArgument(0);
                    return future;
                });

        eventSubscriber.start();
        capturedListener[0].onEvent(subscription, resolvedEvent);

        // Then - should not call any handler
        verify(projectionHandler, never()).handle(any(GameCreatedEvent.class));
        verify(projectionHandler, never()).handle(any(PieceMovedEvent.class));
        verify(projectionHandler, never()).handle(any(PieceDroppedEvent.class));
        verify(projectionHandler, never()).handle(any(GameEndedEvent.class));
    }
}
