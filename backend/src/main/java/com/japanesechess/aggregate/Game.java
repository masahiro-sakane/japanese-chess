package com.japanesechess.aggregate;

import com.japanesechess.domain.*;
import com.japanesechess.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Game {
    private UUID gameId;
    private UUID blackPlayerId;
    private UUID whitePlayerId;
    private Board board;
    private PlayerColor currentTurn;
    private GameStatus status;
    private final MoveValidator moveValidator;
    private final List<DomainEvent> uncommittedEvents;

    public enum GameStatus {
        NOT_STARTED,
        IN_PROGRESS,
        ENDED
    }

    public Game() {
        this.moveValidator = new MoveValidator();
        this.uncommittedEvents = new ArrayList<>();
        this.status = GameStatus.NOT_STARTED;
    }

    public static Game create(UUID gameId, UUID blackPlayerId, UUID whitePlayerId) {
        Game game = new Game();
        GameCreatedEvent event = new GameCreatedEvent(gameId, blackPlayerId, whitePlayerId);
        game.apply(event);
        game.uncommittedEvents.add(event);
        return game;
    }

    public void makeMove(Move move) {
        System.out.println("=== DEBUG makeMove ===");
        System.out.println("Move player: " + move.getPlayer());
        System.out.println("Current turn: " + currentTurn);
        System.out.println("Game status: " + status);

        validateGameState();
        validatePlayerTurn(move.getPlayer());

        if (!moveValidator.isValidMove(board, move)) {
            throw new IllegalArgumentException("Invalid move: " + move);
        }

        Optional<Piece> targetPiece = board.getPieceAt(move.getTo());
        Move executedMove = move;
        if (targetPiece.isPresent()) {
            executedMove = move.withCapturedPiece(targetPiece.get().getType());
        }

        DomainEvent event;
        if (move.isDrop()) {
            event = new PieceDroppedEvent(gameId, executedMove);
        } else {
            event = new PieceMovedEvent(gameId, executedMove);
        }

        apply(event);
        uncommittedEvents.add(event);
    }

    public void resign(PlayerColor player) {
        validateGameState();

        PlayerColor winner = player.opposite();
        GameEndedEvent event = new GameEndedEvent(
            gameId,
            winner,
            GameEndedEvent.EndReason.RESIGNATION
        );

        apply(event);
        uncommittedEvents.add(event);
    }

    private void apply(DomainEvent event) {
        switch (event) {
            case GameCreatedEvent e -> {
                this.gameId = e.getAggregateId();
                this.blackPlayerId = e.getBlackPlayerId();
                this.whitePlayerId = e.getWhitePlayerId();
                this.board = Board.createInitialBoard();
                this.currentTurn = e.getFirstTurn();
                this.status = GameStatus.IN_PROGRESS;
            }
            case PieceMovedEvent e -> {
                this.board = board.applyMove(Move.normalMove(
                    e.getFrom(),
                    e.getTo(),
                    e.getPieceType(),
                    e.getPlayer()
                ).withCapturedPiece(e.getCapturedPiece()));
                this.currentTurn = currentTurn.opposite();
            }
            case PieceDroppedEvent e -> {
                this.board = board.applyMove(Move.dropMove(
                    e.getPosition(),
                    e.getPieceType(),
                    e.getPlayer()
                ));
                this.currentTurn = currentTurn.opposite();
            }
            case GameEndedEvent e -> {
                this.status = GameStatus.ENDED;
            }
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass());
        }
    }

    public void loadFromHistory(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            apply(event);
        }
    }

    public List<DomainEvent> getUncommittedEvents() {
        return new ArrayList<>(uncommittedEvents);
    }

    public void clearUncommittedEvents() {
        uncommittedEvents.clear();
    }

    private void validateGameState() {
        if (status == GameStatus.NOT_STARTED) {
            throw new IllegalStateException("Game has not started");
        }
        if (status == GameStatus.ENDED) {
            throw new IllegalStateException("Game has already ended");
        }
    }

    private void validatePlayerTurn(PlayerColor player) {
        if (player != currentTurn) {
            throw new IllegalArgumentException(
                String.format("Not %s's turn. Current turn: %s",
                    player.getJapaneseName(),
                    currentTurn.getJapaneseName())
            );
        }
    }

    public UUID getGameId() {
        return gameId;
    }

    public UUID getBlackPlayerId() {
        return blackPlayerId;
    }

    public UUID getWhitePlayerId() {
        return whitePlayerId;
    }

    public Board getBoard() {
        return board;
    }

    public PlayerColor getCurrentTurn() {
        return currentTurn;
    }

    public GameStatus getStatus() {
        return status;
    }
}
