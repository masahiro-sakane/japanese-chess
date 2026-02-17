package com.japanesechess.projection;

import com.eventstore.dbclient.*;
import com.japanesechess.event.*;
import com.japanesechess.infrastructure.EventSerializer;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSubscriber {

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 100;

    private final EventStoreDBClient eventStoreClient;
    private final EventSerializer eventSerializer;
    private final GameProjectionHandler projectionHandler;
    private Subscription subscription;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        log.info("Starting EventStoreDB subscription...");

        SubscribeToAllOptions options = SubscribeToAllOptions.get()
                .fromEnd()
                .filter(SubscriptionFilter.newBuilder()
                        .addStreamNamePrefix("game-")
                        .build());

        try {
            subscription = eventStoreClient.subscribeToAll(
                    new SubscriptionListener() {
                        @Override
                        public void onEvent(Subscription subscription, ResolvedEvent resolvedEvent) {
                            handleEvent(resolvedEvent);
                        }

                        @Override
                        public void onCancelled(Subscription subscription, Throwable throwable) {
                            if (throwable != null) {
                                log.error("Subscription error", throwable);
                            } else {
                                log.warn("Subscription cancelled");
                            }
                        }
                    },
                    options
            ).get();

            log.info("EventStoreDB subscription started successfully");
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to start subscription", e);
            throw new RuntimeException("Failed to start EventStoreDB subscription", e);
        }
    }

    @PreDestroy
    public void stop() {
        if (subscription != null) {
            log.info("Stopping EventStoreDB subscription...");
            subscription.stop();
        }
    }

    private void handleEvent(ResolvedEvent resolvedEvent) {
        RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
        String eventType = recordedEvent.getEventType();

        int retryCount = 0;
        while (retryCount <= MAX_RETRIES) {
            try {
                log.debug("Processing event: {} (attempt {}/{})", eventType, retryCount + 1, MAX_RETRIES + 1);

                DomainEvent domainEvent = eventSerializer.deserialize(recordedEvent);

                // Route to appropriate handler
                switch (domainEvent) {
                    case GameCreatedEvent e -> projectionHandler.handle(e);
                    case PieceMovedEvent e -> projectionHandler.handle(e);
                    case PieceDroppedEvent e -> projectionHandler.handle(e);
                    case GameEndedEvent e -> projectionHandler.handle(e);
                    default -> log.warn("Unknown event type: {}", eventType);
                }

                log.debug("Event processed successfully: {}", eventType);
                return; // Success - exit retry loop

            } catch (Exception e) {
                retryCount++;

                if (retryCount > MAX_RETRIES) {
                    log.error("Failed to process event after {} retries, sending to dead letter queue. Event: {}, Error: {}",
                              MAX_RETRIES, eventType, e.getMessage(), e);
                    sendToDeadLetterQueue(resolvedEvent, e);
                    return;
                }

                long backoffMs = INITIAL_BACKOFF_MS * (long) Math.pow(2, retryCount - 1);
                log.warn("Error processing event (retry {}/{}), retrying after {}ms. Event: {}, Error: {}",
                         retryCount, MAX_RETRIES, backoffMs, eventType, e.getMessage());

                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Retry interrupted for event: {}", eventType);
                    sendToDeadLetterQueue(resolvedEvent, e);
                    return;
                }
            }
        }
    }

    private void sendToDeadLetterQueue(ResolvedEvent resolvedEvent, Exception error) {
        RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
        log.error("DEAD LETTER QUEUE - Event: {}, Stream: {}, Revision: {}, Error: {}",
                  recordedEvent.getEventType(),
                  recordedEvent.getStreamId(),
                  recordedEvent.getRevision(),
                  error.getMessage());

        // TODO: Implement actual DLQ storage (database table, message queue, etc.)
        // For now, we log it. In production, store in a DLQ table for manual review/replay
    }
}
