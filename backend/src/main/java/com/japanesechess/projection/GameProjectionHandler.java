package com.japanesechess.projection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.japanesechess.domain.Board;
import com.japanesechess.domain.Piece;
import com.japanesechess.domain.PlayerColor;
import com.japanesechess.domain.Position;
import com.japanesechess.event.GameCreatedEvent;
import com.japanesechess.event.GameEndedEvent;
import com.japanesechess.event.PieceDroppedEvent;
import com.japanesechess.event.PieceMovedEvent;
import com.japanesechess.exception.GameNotFoundException;
import com.japanesechess.exception.InvalidBoardStateException;
import com.japanesechess.exception.ProjectionException;
import com.japanesechess.readmodel.*;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameProjectionHandler {

    private final GameEntityRepository gameEntityRepository;
    private final GameViewRepository gameViewRepository;
    private final MoveHistoryRepository moveHistoryRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void handle(GameCreatedEvent event) {
        try {
            log.info("Handling GameCreatedEvent for game {}", event.getAggregateId());

            // Create GameEntity
            GameEntity gameEntity = new GameEntity();
            gameEntity.setGameId(event.getAggregateId());
            gameEntity.setBlackPlayerId(event.getBlackPlayerId());
            gameEntity.setWhitePlayerId(event.getWhitePlayerId());
            gameEntity.setStatus("IN_PROGRESS");
            gameEntity.setCurrentTurn(event.getFirstTurn().name());
            gameEntityRepository.save(gameEntity);

            // Create GameViewEntity with initial board state
            Board initialBoard = Board.createInitialBoard();
            GameViewEntity gameView = new GameViewEntity();
            gameView.setGameId(event.getAggregateId());
            gameView.setBoardState(serializeBoardState(initialBoard));
            gameView.setBlackCapturedPieces("[]");
            gameView.setWhiteCapturedPieces("[]");
            gameView.setMoveCount(0);
            gameViewRepository.save(gameView);

            log.info("Game projection created for game {}", event.getAggregateId());
        } catch (OptimisticLockException e) {
            log.warn("Concurrent modification detected for game {}, event will be retried", event.getAggregateId());
            throw new ProjectionException(
                event.getAggregateId(),
                event.getEventType(),
                "Concurrent modification detected, retry needed",
                e);
        }
    }

    @Transactional
    public void handle(PieceMovedEvent event) {
        try {
            log.info("Handling PieceMovedEvent for game {}", event.getAggregateId());

            // Update GameEntity
            GameEntity gameEntity = gameEntityRepository.findById(event.getAggregateId())
                    .orElseThrow(() -> new GameNotFoundException(event.getAggregateId()));

            // Toggle current turn
            PlayerColor nextTurn = PlayerColor.valueOf(gameEntity.getCurrentTurn()) == PlayerColor.BLACK
                    ? PlayerColor.WHITE
                    : PlayerColor.BLACK;
            gameEntity.setCurrentTurn(nextTurn.name());
            gameEntityRepository.save(gameEntity);

            // Update GameViewEntity
            GameViewEntity gameView = gameViewRepository.findById(event.getAggregateId())
                    .orElseThrow(() -> new GameNotFoundException(event.getAggregateId()));

            // Create move history BEFORE incrementing moveCount
            // This ensures moveNumber matches the current moveCount
            MoveHistoryEntity moveHistory = createMoveHistory(event);
            moveHistoryRepository.save(moveHistory);

            // Apply move to board state
            Board currentBoard = deserializeBoardState(gameView.getBoardState());
            Board newBoard = currentBoard.applyMove(event.getMove());
            gameView.setBoardState(serializeBoardState(newBoard));
            gameView.setMoveCount(gameView.getMoveCount() + 1);

            // Update captured pieces if any
            if (event.getMove().getCapturedPiece() != null) {
                if (event.getMove().getPlayer() == PlayerColor.BLACK) {
                    gameView.setBlackCapturedPieces(addCapturedPiece(
                            gameView.getBlackCapturedPieces(),
                            event.getMove().getCapturedPiece()));
                } else {
                    gameView.setWhiteCapturedPieces(addCapturedPiece(
                            gameView.getWhiteCapturedPieces(),
                            event.getMove().getCapturedPiece()));
                }
            }
            gameViewRepository.save(gameView);

            log.info("Game projection updated for move in game {}", event.getAggregateId());
        } catch (OptimisticLockException e) {
            log.warn("Concurrent modification detected for game {}, event will be retried", event.getAggregateId());
            throw new ProjectionException(
                event.getAggregateId(),
                event.getEventType(),
                "Concurrent modification detected, retry needed",
                e);
        }
    }

    @Transactional
    public void handle(PieceDroppedEvent event) {
        try {
            log.info("Handling PieceDroppedEvent for game {}", event.getAggregateId());

            // Update GameEntity
            GameEntity gameEntity = gameEntityRepository.findById(event.getAggregateId())
                    .orElseThrow(() -> new GameNotFoundException(event.getAggregateId()));

            // Toggle current turn
            PlayerColor nextTurn = PlayerColor.valueOf(gameEntity.getCurrentTurn()) == PlayerColor.BLACK
                    ? PlayerColor.WHITE
                    : PlayerColor.BLACK;
            gameEntity.setCurrentTurn(nextTurn.name());
            gameEntityRepository.save(gameEntity);

            // Update GameViewEntity
            GameViewEntity gameView = gameViewRepository.findById(event.getAggregateId())
                    .orElseThrow(() -> new GameNotFoundException(event.getAggregateId()));

            // Create move history BEFORE incrementing moveCount
            // This ensures moveNumber matches the current moveCount
            MoveHistoryEntity moveHistory = createMoveHistory(event);
            moveHistoryRepository.save(moveHistory);

            // Apply drop to board state
            Board currentBoard = deserializeBoardState(gameView.getBoardState());
            Board newBoard = currentBoard.applyMove(event.getMove());
            gameView.setBoardState(serializeBoardState(newBoard));
            gameView.setMoveCount(gameView.getMoveCount() + 1);

            // Remove piece from captured pieces
            if (event.getMove().getPlayer() == PlayerColor.BLACK) {
                gameView.setBlackCapturedPieces(removeCapturedPiece(
                        gameView.getBlackCapturedPieces(),
                        event.getMove().getPieceType()));
            } else {
                gameView.setWhiteCapturedPieces(removeCapturedPiece(
                        gameView.getWhiteCapturedPieces(),
                        event.getMove().getPieceType()));
            }
            gameViewRepository.save(gameView);

            log.info("Game projection updated for drop in game {}", event.getAggregateId());
        } catch (OptimisticLockException e) {
            log.warn("Concurrent modification detected for game {}, event will be retried", event.getAggregateId());
            throw new ProjectionException(
                event.getAggregateId(),
                event.getEventType(),
                "Concurrent modification detected, retry needed",
                e);
        }
    }

    @Transactional
    public void handle(GameEndedEvent event) {
        log.info("Handling GameEndedEvent for game {}", event.getAggregateId());

        GameEntity gameEntity = gameEntityRepository.findById(event.getAggregateId())
                .orElseThrow(() -> new GameNotFoundException(event.getAggregateId()));

        gameEntity.setStatus("FINISHED");
        gameEntity.setWinner(event.getWinner().name());
        gameEntity.setEndReason(event.getReason().name());
        gameEntity.setCurrentTurn(null);
        gameEntityRepository.save(gameEntity);

        log.info("Game projection ended for game {}", event.getAggregateId());
    }

    private String serializeBoardState(Board board) {
        try {
            List<PieceDto> pieces = new ArrayList<>();
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    Optional<Piece> pieceOpt = board.getPieceAt(new Position(row, col));
                    if (pieceOpt.isPresent()) {
                        Piece piece = pieceOpt.get();
                        pieces.add(new PieceDto(
                                row,
                                col,
                                piece.getType().name(),
                                piece.getOwner().name(),
                                piece.isPromoted()));
                    }
                }
            }
            return objectMapper.writeValueAsString(pieces);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize board state", e);
        }
    }

    private Board deserializeBoardState(String boardStateJson) {
        try {
            List<PieceDto> pieceDtos = objectMapper.readValue(
                    boardStateJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PieceDto.class));

            Board board = new Board();
            for (PieceDto dto : pieceDtos) {
                Position position = new Position(dto.row, dto.column);
                com.japanesechess.domain.PieceType pieceType = com.japanesechess.domain.PieceType.valueOf(dto.type);
                PlayerColor owner = PlayerColor.valueOf(dto.owner);

                Piece piece = new Piece(pieceType, owner, position);
                if (dto.promoted) {
                    piece = piece.promote();
                }

                board.placePiece(piece);
            }
            return board;
        } catch (JsonProcessingException e) {
            throw new InvalidBoardStateException("Failed to deserialize board state", e);
        }
    }

    private String addCapturedPiece(String capturedPiecesJson, com.japanesechess.domain.PieceType capturedPieceType) {
        try {
            List<String> pieces = objectMapper.readValue(
                    capturedPiecesJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            pieces.add(capturedPieceType.name());
            return objectMapper.writeValueAsString(pieces);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to add captured piece", e);
        }
    }

    private String removeCapturedPiece(String capturedPiecesJson, com.japanesechess.domain.PieceType pieceType) {
        try {
            List<String> pieces = objectMapper.readValue(
                    capturedPiecesJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            pieces.remove(pieceType.name());
            return objectMapper.writeValueAsString(pieces);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to remove captured piece", e);
        }
    }

    private MoveHistoryEntity createMoveHistory(PieceMovedEvent event) {
        MoveHistoryEntity history = new MoveHistoryEntity();
        history.setGameId(event.getAggregateId());
        history.setMoveNumber(getMoveNumber(event.getAggregateId()));
        history.setPlayer(event.getMove().getPlayer().name());
        history.setFromRow(event.getMove().getFrom().getRow());
        history.setFromColumn(event.getMove().getFrom().getColumn());
        history.setToRow(event.getMove().getTo().getRow());
        history.setToColumn(event.getMove().getTo().getColumn());
        history.setPieceType(event.getMove().getPieceType().name());
        history.setPromoted(event.getMove().isPromote());
        history.setIsDrop(false);
        history.setCapturedPiece(event.getMove().getCapturedPiece() != null
                ? event.getMove().getCapturedPiece().name()
                : null);
        return history;
    }

    private MoveHistoryEntity createMoveHistory(PieceDroppedEvent event) {
        MoveHistoryEntity history = new MoveHistoryEntity();
        history.setGameId(event.getAggregateId());
        history.setMoveNumber(getMoveNumber(event.getAggregateId()));
        history.setPlayer(event.getMove().getPlayer().name());
        history.setFromRow(null);
        history.setFromColumn(null);
        history.setToRow(event.getMove().getTo().getRow());
        history.setToColumn(event.getMove().getTo().getColumn());
        history.setPieceType(event.getMove().getPieceType().name());
        history.setPromoted(false);
        history.setIsDrop(true);
        history.setCapturedPiece(null);
        return history;
    }

    private Integer getMoveNumber(java.util.UUID gameId) {
        GameViewEntity gameView = gameViewRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("GameView not found: " + gameId));
        // moveCount is incremented AFTER this method is called, so return current count + 1
        // which represents the move number being saved
        return gameView.getMoveCount() + 1;
    }

    // DTO for serializing piece positions
    private static class PieceDto {
        public int row;
        public int column;
        public String type;
        public String owner;
        public boolean promoted;

        public PieceDto() {}

        public PieceDto(int row, int column, String type, String owner, boolean promoted) {
            this.row = row;
            this.column = column;
            this.type = type;
            this.owner = owner;
            this.promoted = promoted;
        }
    }
}
