package com.japanesechess.query;

import com.japanesechess.domain.*;
import com.japanesechess.exception.GameNotFoundException;
import com.japanesechess.readmodel.MoveHistoryEntity;
import com.japanesechess.readmodel.MoveHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 棋譜再生サービス
 * 特定の手数における盤面状態を再構築する
 */
@Service
@RequiredArgsConstructor
public class GameReplayService {

    private final MoveHistoryRepository moveHistoryRepository;

    /**
     * 特定の手数における盤面状態を取得
     * @param gameId ゲームID
     * @param moveNumber 手数（0=初期状態）
     * @return 盤面状態DTO
     */
    public BoardStateDto getBoardStateAtMove(UUID gameId, Integer moveNumber) {
        if (moveNumber < 0) {
            throw new IllegalArgumentException("Move number cannot be negative");
        }

        // 初期盤面から開始
        Board board = Board.createInitialBoard();
        PlayerColor currentPlayer = PlayerColor.BLACK;

        // 指定手数までの全ての手を取得
        List<MoveHistoryEntity> moves = moveHistoryRepository
            .findByGameIdAndMoveNumberLessThanEqualOrderByMoveNumberAsc(gameId, moveNumber);

        // If requesting moves but none found, verify game exists
        // moveNumber 0 is valid (initial state) even with no moves
        if (moveNumber > 0 && moves.isEmpty()) {
            // Check if game exists at all - if not, throw GameNotFoundException
            // If game exists but has no moves yet, that's also an error for moveNumber > 0
            throw new GameNotFoundException(gameId);
        }

        // 各手を順次適用
        for (MoveHistoryEntity moveEntity : moves) {
            Move move = reconstructMove(moveEntity);
            board = board.applyMove(move);
            currentPlayer = currentPlayer == PlayerColor.BLACK
                ? PlayerColor.WHITE
                : PlayerColor.BLACK;
        }

        // DTOにシリアライズ
        return serializeBoardState(board, moveNumber, currentPlayer);
    }

    /**
     * 再生用の全ての手を取得
     * @param gameId ゲームID
     * @return 手の履歴リスト
     */
    public List<MoveHistoryDto> getMovesForReplay(UUID gameId) {
        List<MoveHistoryEntity> moves = moveHistoryRepository
            .findByGameIdOrderByMoveNumberAsc(gameId);

        if (moves.isEmpty()) {
            throw new GameNotFoundException(gameId);
        }

        return moves.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * MoveHistoryEntityからMoveオブジェクトを再構築
     */
    private Move reconstructMove(MoveHistoryEntity entity) {
        PlayerColor player = PlayerColor.valueOf(entity.getPlayer());
        PieceType pieceType = PieceType.valueOf(entity.getPieceType());

        if (entity.getIsDrop()) {
            Position to = new Position(entity.getToRow(), entity.getToColumn());
            return Move.dropMove(to, pieceType, player);
        } else {
            Position from = new Position(entity.getFromRow(), entity.getFromColumn());
            Position to = new Position(entity.getToRow(), entity.getToColumn());

            Move move = entity.getPromoted()
                ? Move.promoteMove(from, to, pieceType, player)
                : Move.normalMove(from, to, pieceType, player);

            if (entity.getCapturedPiece() != null) {
                PieceType capturedType = PieceType.valueOf(entity.getCapturedPiece());
                move = move.withCapturedPiece(capturedType);
            }

            return move;
        }
    }

    /**
     * BoardをBoardStateDtoにシリアライズ
     */
    private BoardStateDto serializeBoardState(Board board, Integer moveNumber,
                                              PlayerColor currentPlayer) {
        List<PiecePositionDto> pieceDtos = board.getAllPieces().stream()
            .map(this::convertPieceToDto)
            .collect(Collectors.toList());

        List<String> blackCaptured = board.getCapturedPieces(PlayerColor.BLACK).stream()
            .map(Enum::name)
            .collect(Collectors.toList());

        List<String> whiteCaptured = board.getCapturedPieces(PlayerColor.WHITE).stream()
            .map(Enum::name)
            .collect(Collectors.toList());

        return new BoardStateDto(
            pieceDtos,
            blackCaptured,
            whiteCaptured,
            moveNumber,
            currentPlayer.name()
        );
    }

    /**
     * PieceをPiecePositionDtoに変換
     */
    private PiecePositionDto convertPieceToDto(Piece piece) {
        return new PiecePositionDto(
            piece.getPosition().getRow(),
            piece.getPosition().getColumn(),
            piece.getType().name(),
            piece.getOwner().name(),
            piece.isPromoted()
        );
    }

    /**
     * MoveHistoryEntityをMoveHistoryDtoに変換
     */
    private MoveHistoryDto convertToDto(MoveHistoryEntity entity) {
        return new MoveHistoryDto(
            entity.getId(),
            entity.getMoveNumber(),
            entity.getPlayer(),
            entity.getFromRow(),
            entity.getFromColumn(),
            entity.getToRow(),
            entity.getToColumn(),
            entity.getPieceType(),
            entity.getPromoted(),
            entity.getIsDrop(),
            entity.getCapturedPiece(),
            entity.getTimestamp()
        );
    }
}
