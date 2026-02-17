package com.japanesechess.service;

import com.japanesechess.aggregate.Game;
import com.japanesechess.command.CreateGameCommand;
import com.japanesechess.command.DropPieceCommand;
import com.japanesechess.command.MovePieceCommand;
import com.japanesechess.command.ResignGameCommand;
import com.japanesechess.domain.Move;
import com.japanesechess.event.DomainEvent;
import com.japanesechess.event.GameCreatedEvent;
import com.japanesechess.event.GameEndedEvent;
import com.japanesechess.event.PieceDroppedEvent;
import com.japanesechess.event.PieceMovedEvent;
import com.japanesechess.projection.GameProjectionHandler;
import com.japanesechess.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GameCommandHandler {

    private final GameRepository gameRepository;
    private final GameProjectionHandler projectionHandler;

    public GameCommandHandler(GameRepository gameRepository, GameProjectionHandler projectionHandler) {
        this.gameRepository = gameRepository;
        this.projectionHandler = projectionHandler;
    }

    @Transactional
    public UUID handle(CreateGameCommand command) {
        Game game = Game.create(
            command.getGameId(),
            command.getBlackPlayerId(),
            command.getWhitePlayerId()
        );

        List<DomainEvent> newEvents = game.getUncommittedEvents();
        gameRepository.save(game);
        applyProjections(newEvents);
        return command.getGameId();
    }

    @Transactional
    public void handle(MovePieceCommand command) {
        Game game = gameRepository.findById(command.getGameId())
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + command.getGameId()));

        Move move;
        if (command.isPromote()) {
            move = Move.promoteMove(
                command.getFrom(),
                command.getTo(),
                command.getPieceType(),
                command.getPlayer()
            );
        } else {
            move = Move.normalMove(
                command.getFrom(),
                command.getTo(),
                command.getPieceType(),
                command.getPlayer()
            );
        }

        game.makeMove(move);
        List<DomainEvent> newEvents = game.getUncommittedEvents();
        gameRepository.save(game);
        applyProjections(newEvents);
    }

    @Transactional
    public void handle(DropPieceCommand command) {
        Game game = gameRepository.findById(command.getGameId())
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + command.getGameId()));

        Move move = Move.dropMove(
            command.getTo(),
            command.getPieceType(),
            command.getPlayer()
        );

        game.makeMove(move);
        List<DomainEvent> newEvents = game.getUncommittedEvents();
        gameRepository.save(game);
        applyProjections(newEvents);
    }

    @Transactional
    public void handle(ResignGameCommand command) {
        Game game = gameRepository.findById(command.getGameId())
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + command.getGameId()));

        game.resign(command.getPlayer());
        List<DomainEvent> newEvents = game.getUncommittedEvents();
        gameRepository.save(game);
        applyProjections(newEvents);
    }

    private void applyProjections(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            switch (event) {
                case GameCreatedEvent e -> projectionHandler.handle(e);
                case PieceMovedEvent e -> projectionHandler.handle(e);
                case PieceDroppedEvent e -> projectionHandler.handle(e);
                case GameEndedEvent e -> projectionHandler.handle(e);
                default -> { /* unknown event type, skip */ }
            }
        }
    }
}
