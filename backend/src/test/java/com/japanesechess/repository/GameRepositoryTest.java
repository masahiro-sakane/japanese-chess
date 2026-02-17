package com.japanesechess.repository;

import com.japanesechess.aggregate.Game;
import com.japanesechess.domain.Move;
import com.japanesechess.domain.PlayerColor;
import com.japanesechess.domain.Position;
import com.japanesechess.event.DomainEvent;
import com.japanesechess.event.GameCreatedEvent;
import com.japanesechess.infrastructure.EventStoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.japanesechess.domain.PieceType.PAWN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameRepositoryTest {

    @Mock
    private EventStoreRepository eventStore;

    private GameRepository gameRepository;

    @BeforeEach
    void setUp() {
        gameRepository = new GameRepository(eventStore);
    }

    @Test
    void save_ShouldAppendUncommittedEvents_WhenGameHasEvents() {
        UUID gameId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();
        UUID whitePlayerId = UUID.randomUUID();

        Game game = Game.create(gameId, blackPlayerId, whitePlayerId);

        when(eventStore.getStreamVersion(gameId)).thenReturn(-1L);

        gameRepository.save(game);

        verify(eventStore, times(1))
            .appendEvents(eq(gameId), any(List.class), eq(-1L));

        assertTrue(game.getUncommittedEvents().isEmpty());
    }

    @Test
    void save_ShouldNotAppendEvents_WhenNoUncommittedEvents() {
        UUID gameId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();
        UUID whitePlayerId = UUID.randomUUID();

        Game game = Game.create(gameId, blackPlayerId, whitePlayerId);
        game.clearUncommittedEvents();

        gameRepository.save(game);

        verify(eventStore, never()).appendEvents(any(), any(), anyLong());
    }

    @Test
    void save_ShouldUseCorrectVersion_WhenStreamAlreadyExists() {
        UUID gameId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();
        UUID whitePlayerId = UUID.randomUUID();

        Game game = Game.create(gameId, blackPlayerId, whitePlayerId);
        game.clearUncommittedEvents();

        Move move = Move.normalMove(
            new Position(6, 4),
            new Position(5, 4),
            PAWN,
            PlayerColor.BLACK
        );
        game.makeMove(move);

        when(eventStore.getStreamVersion(gameId)).thenReturn(0L);

        gameRepository.save(game);

        verify(eventStore, times(1))
            .appendEvents(eq(gameId), any(List.class), eq(0L));
    }

    @Test
    void findById_ShouldReturnGame_WhenEventsExist() {
        UUID gameId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();
        UUID whitePlayerId = UUID.randomUUID();

        GameCreatedEvent event = new GameCreatedEvent(gameId, blackPlayerId, whitePlayerId);
        List<DomainEvent> events = List.of(event);

        when(eventStore.readEvents(gameId)).thenReturn(events);

        Optional<Game> result = gameRepository.findById(gameId);

        assertTrue(result.isPresent());
        Game game = result.get();
        assertEquals(gameId, game.getGameId());
        assertEquals(blackPlayerId, game.getBlackPlayerId());
        assertEquals(whitePlayerId, game.getWhitePlayerId());
        assertEquals(Game.GameStatus.IN_PROGRESS, game.getStatus());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNoEventsExist() {
        UUID gameId = UUID.randomUUID();

        when(eventStore.readEvents(gameId)).thenReturn(List.of());

        Optional<Game> result = gameRepository.findById(gameId);

        assertFalse(result.isPresent());
    }

    @Test
    void findById_ShouldReconstructGameState_WhenMultipleEventsExist() {
        UUID gameId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();
        UUID whitePlayerId = UUID.randomUUID();

        GameCreatedEvent createEvent = new GameCreatedEvent(gameId, blackPlayerId, whitePlayerId);

        List<DomainEvent> events = List.of(createEvent);

        when(eventStore.readEvents(gameId)).thenReturn(events);

        Optional<Game> result = gameRepository.findById(gameId);

        assertTrue(result.isPresent());
        Game game = result.get();
        assertEquals(gameId, game.getGameId());
        assertEquals(PlayerColor.BLACK, game.getCurrentTurn());
        assertEquals(Game.GameStatus.IN_PROGRESS, game.getStatus());
    }

    @Test
    void saveAndLoad_ShouldPreserveGameState() {
        UUID gameId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();
        UUID whitePlayerId = UUID.randomUUID();

        Game originalGame = Game.create(gameId, blackPlayerId, whitePlayerId);
        List<DomainEvent> uncommittedEvents = originalGame.getUncommittedEvents();

        when(eventStore.getStreamVersion(gameId)).thenReturn(-1L);
        when(eventStore.readEvents(gameId)).thenReturn(uncommittedEvents);

        gameRepository.save(originalGame);

        Optional<Game> loadedGame = gameRepository.findById(gameId);

        assertTrue(loadedGame.isPresent());
        assertEquals(originalGame.getGameId(), loadedGame.get().getGameId());
        assertEquals(originalGame.getBlackPlayerId(), loadedGame.get().getBlackPlayerId());
        assertEquals(originalGame.getWhitePlayerId(), loadedGame.get().getWhitePlayerId());
        assertEquals(originalGame.getCurrentTurn(), loadedGame.get().getCurrentTurn());
        assertEquals(originalGame.getStatus(), loadedGame.get().getStatus());
    }
}
