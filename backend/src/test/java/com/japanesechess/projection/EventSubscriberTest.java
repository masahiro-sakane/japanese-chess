package com.japanesechess.projection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests for EventSubscriber.
 *
 * NOTE: The EventStoreDB async subscription is intentionally DISABLED.
 * Projections are updated synchronously by GameCommandHandler to avoid
 * double-processing and race conditions.
 * These tests verify the disabled subscription behavior.
 */
class EventSubscriberTest {

    private EventSubscriber eventSubscriber;

    @BeforeEach
    void setUp() {
        eventSubscriber = new EventSubscriber();
    }

    @Test
    void start_shouldNotThrow() {
        assertDoesNotThrow(() -> eventSubscriber.start());
    }

    @Test
    void stop_shouldNotThrow() {
        assertDoesNotThrow(() -> eventSubscriber.stop());
    }
}
