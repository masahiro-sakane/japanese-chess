package com.japanesechess.projection;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventSubscriber {

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        // EventStoreDB subscription is disabled.
        // Projections are now updated synchronously by GameCommandHandler
        // to avoid double-processing and race conditions.
        log.info("EventStoreDB subscription is disabled - projections are updated synchronously");
    }

    @PreDestroy
    public void stop() {
        log.debug("EventSubscriber stopped (subscription was disabled)");
    }
}
