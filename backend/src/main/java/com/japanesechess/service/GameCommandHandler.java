package com.japanesechess.service;

import com.japanesechess.aggregate.Game;
import com.japanesechess.ai.AiGameRegistry;
import com.japanesechess.ai.AiMoveScheduler;
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
import com.japanesechess.websocket.GameWebSocketNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

@Service
public class GameCommandHandler {

    private final GameRepository gameRepository;
    private final GameProjectionHandler projectionHandler;
    private final GameWebSocketNotifier webSocketNotifier;
    private final AiGameRegistry aiGameRegistry;

    @Lazy
    @Autowired
    private AiMoveScheduler aiMoveScheduler;

    public GameCommandHandler(
        GameRepository gameRepository,
        GameProjectionHandler projectionHandler,
        GameWebSocketNotifier webSocketNotifier,
        AiGameRegistry aiGameRegistry
    ) {
        this.gameRepository = gameRepository;
        this.projectionHandler = projectionHandler;
        this.webSocketNotifier = webSocketNotifier;
        this.aiGameRegistry = aiGameRegistry;
    }

    @Transactional
    public UUID handle(CreateGameCommand command) {
        Game game;
        if (command.isAiGame() && command.getAiDifficulty() != null) {
            game = Game.createWithAi(
                command.getGameId(),
                command.getBlackPlayerId(),
                command.getWhitePlayerId(),
                command.getAiDifficulty()
            );
            aiGameRegistry.register(command.getGameId(), command.getAiDifficulty());
        } else {
            game = Game.create(
                command.getGameId(),
                command.getBlackPlayerId(),
                command.getWhitePlayerId()
            );
        }

        List<DomainEvent> newEvents = game.getUncommittedEvents();
        gameRepository.save(game);
        applyProjections(newEvents);
        webSocketNotifier.notifyGameUpdated(command.getGameId());
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
        webSocketNotifier.notifyGameUpdated(command.getGameId());

        scheduleAiMoveIfNeeded(command.getGameId(), game);
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
        webSocketNotifier.notifyGameUpdated(command.getGameId());

        scheduleAiMoveIfNeeded(command.getGameId(), game);
    }

    @Transactional
    public void handle(ResignGameCommand command) {
        Game game = gameRepository.findById(command.getGameId())
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + command.getGameId()));

        game.resign(command.getPlayer());
        List<DomainEvent> newEvents = game.getUncommittedEvents();
        gameRepository.save(game);
        applyProjections(newEvents);
        webSocketNotifier.notifyGameUpdated(command.getGameId());

        if (game.getStatus() == Game.GameStatus.ENDED) {
            aiGameRegistry.unregister(command.getGameId());
        }
    }

    @Transactional
    public void handleAiMove(UUID gameId, Move move) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

        game.makeMove(move);
        List<DomainEvent> newEvents = game.getUncommittedEvents();
        gameRepository.save(game);
        applyProjections(newEvents);

        if (game.getStatus() == Game.GameStatus.ENDED) {
            aiGameRegistry.unregister(gameId);
        }
    }

    // @Async スレッドから呼ばれるため @Transactional の外でWebSocket通知を送る
    public void notifyAfterAiMove(UUID gameId) {
        webSocketNotifier.notifyGameUpdated(gameId);
    }

    private void scheduleAiMoveIfNeeded(UUID gameId, Game game) {
        if (game.getStatus() == Game.GameStatus.IN_PROGRESS
            && aiGameRegistry.isAiGame(gameId)) {
            // トランザクションのコミット後にAIの手を実行する
            // コミット前に実行すると、AIが古い盤面状態を読み込んでしまうため
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        aiMoveScheduler.scheduleAiMove(gameId);
                    }
                });
            } else {
                aiMoveScheduler.scheduleAiMove(gameId);
            }
        }
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
