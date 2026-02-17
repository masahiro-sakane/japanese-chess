package com.japanesechess.service;

import com.japanesechess.aggregate.Game;
import com.japanesechess.command.CreateGameCommand;
import com.japanesechess.command.MovePieceCommand;
import com.japanesechess.command.ResignGameCommand;
import com.japanesechess.domain.PieceType;
import com.japanesechess.domain.PlayerColor;
import com.japanesechess.domain.Position;
import com.japanesechess.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameCommandHandlerTest {

    @Mock
    private GameRepository gameRepository;

    private GameCommandHandler commandHandler;

    @BeforeEach
    void setUp() {
        commandHandler = new GameCommandHandler(gameRepository);
    }

    @Test
    void handle_CreateGameCommand_ShouldCreateNewGame() {
        UUID gameId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();
        UUID whitePlayerId = UUID.randomUUID();

        CreateGameCommand command = new CreateGameCommand(gameId, blackPlayerId, whitePlayerId);

        UUID result = commandHandler.handle(command);

        assertEquals(gameId, result);
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    void handle_MovePieceCommand_ShouldExecuteMove() {
        UUID gameId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();
        UUID whitePlayerId = UUID.randomUUID();

        Game game = Game.create(gameId, blackPlayerId, whitePlayerId);
        game.clearUncommittedEvents();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        Position from = new Position(6, 4);
        Position to = new Position(5, 4);
        MovePieceCommand command = new MovePieceCommand(
            gameId,
            PlayerColor.BLACK,
            from,
            to,
            false,
            PieceType.PAWN
        );

        commandHandler.handle(command);

        verify(gameRepository, times(1)).save(game);
    }

    @Test
    void handle_MovePieceCommand_ShouldThrowException_WhenGameNotFound() {
        UUID gameId = UUID.randomUUID();
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        Position from = new Position(6, 4);
        Position to = new Position(5, 4);
        MovePieceCommand command = new MovePieceCommand(
            gameId,
            PlayerColor.BLACK,
            from,
            to,
            false,
            PieceType.PAWN
        );

        assertThrows(IllegalArgumentException.class, () -> commandHandler.handle(command));
    }
}
