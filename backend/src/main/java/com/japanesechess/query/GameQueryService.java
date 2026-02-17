package com.japanesechess.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.japanesechess.domain.*;
import com.japanesechess.exception.GameNotFoundException;
import com.japanesechess.readmodel.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameQueryService {

    private final GameEntityRepository gameEntityRepository;
    private final GameViewRepository gameViewRepository;
    private final MoveHistoryRepository moveHistoryRepository;
    private final ObjectMapper objectMapper;
    private final CheckDetector checkDetector = new CheckDetector();

    @Transactional(readOnly = true)
    public GameQueryDto getGameById(UUID gameId) {
        GameEntity gameEntity = gameEntityRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        GameViewEntity gameView = gameViewRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        return mapToDto(gameEntity, gameView);
    }

    @Transactional(readOnly = true)
    public List<GameQueryDto> getAllGames() {
        List<GameEntity> games = gameEntityRepository.findAll();

        return games.stream()
                .map(game -> {
                    GameViewEntity gameView = gameViewRepository.findById(game.getGameId())
                            .orElse(null);
                    return mapToDto(game, gameView);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MoveHistoryDto> getMoveHistory(UUID gameId) {
        List<MoveHistoryEntity> moves = moveHistoryRepository.findByGameIdOrderByMoveNumberAsc(gameId);

        return moves.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<GameQueryDto> getAllGames(Pageable pageable) {
        Page<GameEntity> games = gameEntityRepository.findAll(pageable);
        return mapPageToDto(games);
    }

    @Transactional(readOnly = true)
    public Page<GameQueryDto> getGamesByStatus(String status, Pageable pageable) {
        Page<GameEntity> games = gameEntityRepository.findByStatus(status, pageable);
        return mapPageToDto(games);
    }

    @Transactional(readOnly = true)
    public Page<GameQueryDto> getGamesByPlayer(UUID playerId, Pageable pageable) {
        Page<GameEntity> games = gameEntityRepository.findByPlayer(playerId, pageable);
        return mapPageToDto(games);
    }

    private Page<GameQueryDto> mapPageToDto(Page<GameEntity> games) {
        return games.map(game -> {
            GameViewEntity gameView = gameViewRepository.findById(game.getGameId())
                    .orElse(null);
            return mapToDto(game, gameView);
        });
    }

    @Transactional(readOnly = true)
    public GameStatistics getGameStatistics() {
        long totalGames = gameEntityRepository.count();
        long activeGames = gameEntityRepository.countByStatus("IN_PROGRESS");
        long completedGames = gameEntityRepository.countByStatus("COMPLETED");

        return new GameStatistics(totalGames, activeGames, completedGames);
    }

    @Transactional(readOnly = true)
    public PlayerStatistics getPlayerStatistics(UUID playerId) {
        long totalGames = gameEntityRepository.countByPlayer(playerId);
        long activeGames = gameEntityRepository.countActiveByPlayer(playerId);
        long wins = gameEntityRepository.countByWinner(playerId);
        long losses = totalGames - activeGames - wins;

        return new PlayerStatistics(playerId, totalGames, activeGames, wins, losses);
    }

    private GameQueryDto mapToDto(GameEntity gameEntity, GameViewEntity gameView) {
        GameQueryDto dto = new GameQueryDto();
        dto.setGameId(gameEntity.getGameId());
        dto.setBlackPlayerId(gameEntity.getBlackPlayerId());
        dto.setWhitePlayerId(gameEntity.getWhitePlayerId());
        dto.setStatus(gameEntity.getStatus());
        dto.setCurrentTurn(gameEntity.getCurrentTurn());
        dto.setWinner(gameEntity.getWinner());
        dto.setEndReason(gameEntity.getEndReason());
        dto.setCreatedAt(gameEntity.getCreatedAt());
        dto.setUpdatedAt(gameEntity.getUpdatedAt());

        if (gameView != null) {
            dto.setBoardState(parseBoardState(gameView.getBoardState()));
            dto.setBlackCapturedPieces(parseCapturedPieces(gameView.getBlackCapturedPieces()));
            dto.setWhiteCapturedPieces(parseCapturedPieces(gameView.getWhiteCapturedPieces()));
            dto.setMoveCount(gameView.getMoveCount());

            // Calculate check status if game is in progress
            if ("IN_PROGRESS".equals(gameEntity.getStatus())) {
                Board board = reconstructBoard(gameView);
                dto.setBlackInCheck(checkDetector.isInCheck(board, PlayerColor.BLACK));
                dto.setWhiteInCheck(checkDetector.isInCheck(board, PlayerColor.WHITE));
            } else {
                dto.setBlackInCheck(false);
                dto.setWhiteInCheck(false);
            }
        }

        return dto;
    }

    private Board reconstructBoard(GameViewEntity gameView) {
        List<GameQueryDto.PiecePosition> pieceDtos = parseBoardState(gameView.getBoardState());
        Board board = new Board();

        for (GameQueryDto.PiecePosition dto : pieceDtos) {
            Position position = new Position(dto.getRow(), dto.getColumn());
            PieceType pieceType = PieceType.valueOf(dto.getType());
            PlayerColor owner = PlayerColor.valueOf(dto.getOwner());

            Piece piece = new Piece(pieceType, owner, position);
            if (dto.getPromoted()) {
                piece = piece.promote();
            }

            board.placePiece(piece);
        }

        // Note: We don't need to reconstruct captured pieces for check detection
        // as check only depends on pieces on the board
        return board;
    }

    private MoveHistoryDto mapToDto(MoveHistoryEntity entity) {
        MoveHistoryDto dto = new MoveHistoryDto();
        dto.setId(entity.getId());
        dto.setMoveNumber(entity.getMoveNumber());
        dto.setPlayer(entity.getPlayer());
        dto.setFromRow(entity.getFromRow());
        dto.setFromColumn(entity.getFromColumn());
        dto.setToRow(entity.getToRow());
        dto.setToColumn(entity.getToColumn());
        dto.setPieceType(entity.getPieceType());
        dto.setPromoted(entity.getPromoted());
        dto.setIsDrop(entity.getIsDrop());
        dto.setCapturedPiece(entity.getCapturedPiece());
        dto.setTimestamp(entity.getTimestamp());
        return dto;
    }

    private List<GameQueryDto.PiecePosition> parseBoardState(String boardStateJson) {
        try {
            return objectMapper.readValue(
                    boardStateJson,
                    objectMapper.getTypeFactory().constructCollectionType(
                            List.class, GameQueryDto.PiecePosition.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse board state", e);
        }
    }

    private List<String> parseCapturedPieces(String capturedPiecesJson) {
        try {
            return objectMapper.readValue(
                    capturedPiecesJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse captured pieces", e);
        }
    }
}
