package com.japanesechess.service;

import com.japanesechess.aggregate.Game;
import com.japanesechess.command.CreateGameCommand;
import com.japanesechess.command.DropPieceCommand;
import com.japanesechess.command.MovePieceCommand;
import com.japanesechess.command.ResignGameCommand;
import com.japanesechess.domain.Move;
import com.japanesechess.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GameCommandHandler {

    private final GameRepository gameRepository;

    public GameCommandHandler(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Transactional
    public UUID handle(CreateGameCommand command) {
        Game game = Game.create(
            command.getGameId(),
            command.getBlackPlayerId(),
            command.getWhitePlayerId()
        );

        gameRepository.save(game);
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
        gameRepository.save(game);
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
        gameRepository.save(game);
    }

    @Transactional
    public void handle(ResignGameCommand command) {
        Game game = gameRepository.findById(command.getGameId())
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + command.getGameId()));

        game.resign(command.getPlayer());
        gameRepository.save(game);
    }
}
