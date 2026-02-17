package com.japanesechess.query;

import com.japanesechess.domain.PieceType;
import com.japanesechess.domain.PlayerColor;
import com.japanesechess.exception.GameNotFoundException;
import com.japanesechess.readmodel.GameEntity;
import com.japanesechess.readmodel.GameEntityRepository;
import com.japanesechess.readmodel.MoveHistoryEntity;
import com.japanesechess.readmodel.MoveHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * KIF（棋譜）フォーマットエクスポートサービス
 * 日本将棋の標準棋譜記法でゲームを出力
 */
@Service
@RequiredArgsConstructor
public class KifExportService {

    private final MoveHistoryRepository moveHistoryRepository;
    private final GameEntityRepository gameEntityRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH時mm分ss秒")
            .withZone(ZoneId.systemDefault());

    /**
     * ゲームをKIFフォーマットでエクスポート
     * @param gameId ゲームID
     * @return KIF形式の文字列
     */
    public String exportToKIF(UUID gameId) {
        GameEntity game = gameEntityRepository.findById(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));

        List<MoveHistoryEntity> moves = moveHistoryRepository
            .findByGameIdOrderByMoveNumberAsc(gameId);

        StringBuilder kif = new StringBuilder();

        // ヘッダー情報
        appendHeader(kif, game);

        // 指し手一覧
        appendMoves(kif, moves);

        // 終局情報
        appendFooter(kif, game, moves.size());

        return kif.toString();
    }

    /**
     * KIFヘッダー情報を追加
     */
    private void appendHeader(StringBuilder kif, GameEntity game) {
        kif.append("# 棋譜ファイル\n");
        kif.append("# 開始日時：")
            .append(DATE_FORMATTER.format(game.getCreatedAt()))
            .append("\n");
        kif.append("先手：").append(formatPlayerId(game.getBlackPlayerId())).append("\n");
        kif.append("後手：").append(formatPlayerId(game.getWhitePlayerId())).append("\n");
        kif.append("手合割：平手\n");
        kif.append("\n");
        kif.append("手数----指手---------消費時間--\n");
    }

    /**
     * 指し手一覧を追加
     */
    private void appendMoves(StringBuilder kif, List<MoveHistoryEntity> moves) {
        for (MoveHistoryEntity move : moves) {
            kif.append(formatMove(move)).append("\n");
        }
    }

    /**
     * フッター情報を追加
     */
    private void appendFooter(StringBuilder kif, GameEntity game, int totalMoves) {
        if ("FINISHED".equals(game.getStatus())) {
            kif.append("\n");
            if (game.getWinner() != null) {
                String winner = PlayerColor.valueOf(game.getWinner()).getJapaneseName();
                kif.append("まで").append(totalMoves).append("手で")
                    .append(winner).append("の勝ち\n");
            } else {
                kif.append("まで").append(totalMoves).append("手で終局\n");
            }
        }
    }

    /**
     * 1手をKIFフォーマットに変換
     */
    private String formatMove(MoveHistoryEntity move) {
        StringBuilder moveStr = new StringBuilder();

        // 手数（4桁右詰め）
        moveStr.append(String.format("%4d", move.getMoveNumber()));
        moveStr.append(" ");

        if (move.getIsDrop()) {
            // 打つ手: "５五歩打"
            moveStr.append(formatPosition(move.getToRow(), move.getToColumn()));
            moveStr.append(formatPieceType(move.getPieceType()));
            moveStr.append("打");
        } else {
            // 通常の手: "７六歩(77)"
            moveStr.append(formatPosition(move.getToRow(), move.getToColumn()));
            moveStr.append(formatPieceType(move.getPieceType()));

            if (move.getPromoted()) {
                moveStr.append("成");
            }

            // 移動元を括弧で表示
            moveStr.append("(")
                .append(toKifColumn(move.getFromColumn()))
                .append(toKifRow(move.getFromRow()))
                .append(")");
        }

        return moveStr.toString();
    }

    /**
     * 位置を日本語表記に変換 (例: "７六")
     */
    private String formatPosition(Integer row, Integer column) {
        return toKifColumn(column) + toKifRow(row);
    }

    /**
     * 列を日本語数字に変換 (0→９, 8→１)
     */
    private String toKifColumn(Integer col) {
        String[] columns = {"９", "８", "７", "６", "５", "４", "３", "２", "１"};
        return columns[col];
    }

    /**
     * 行を日本語数字に変換 (0→一, 8→九)
     */
    private String toKifRow(Integer row) {
        String[] rows = {"一", "二", "三", "四", "五", "六", "七", "八", "九"};
        return rows[row];
    }

    /**
     * 駒の種類を日本語に変換
     */
    private String formatPieceType(String pieceType) {
        try {
            PieceType type = PieceType.valueOf(pieceType);
            return type.getJapaneseName();
        } catch (IllegalArgumentException e) {
            return pieceType;
        }
    }

    /**
     * プレイヤーIDを表示用にフォーマット
     */
    private String formatPlayerId(UUID playerId) {
        return "Player-" + playerId.toString().substring(0, 8);
    }
}
