package com.japanesechess.repository;

import com.japanesechess.aggregate.Game;
import com.japanesechess.event.DomainEvent;
import com.japanesechess.infrastructure.EventStoreRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class GameRepository {

    private final EventStoreRepository eventStore;

    public GameRepository(EventStoreRepository eventStore) {
        this.eventStore = eventStore;
    }

    public void save(Game game) {
        List<DomainEvent> uncommittedEvents = game.getUncommittedEvents();
        if (uncommittedEvents.isEmpty()) {
            return;
        }

        long currentVersion = eventStore.getStreamVersion(game.getGameId());
        long expectedVersion = currentVersion;

        eventStore.appendEvents(game.getGameId(), uncommittedEvents, expectedVersion);
        game.clearUncommittedEvents();
    }

    public Optional<Game> findById(UUID gameId) {
        List<DomainEvent> events = eventStore.readEvents(gameId);

        if (events.isEmpty()) {
            return Optional.empty();
        }

        Game game = new Game();
        game.loadFromHistory(events);

        return Optional.of(game);
    }
}
