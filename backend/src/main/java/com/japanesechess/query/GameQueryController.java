package com.japanesechess.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/queries")
@RequiredArgsConstructor
@Tag(name = "Game Queries", description = "クエリ操作 - ゲーム情報の取得と統計")
public class GameQueryController {

    private final GameQueryService queryService;
    private final GameReplayService replayService;
    private final KifExportService kifExportService;

    @Operation(summary = "ゲームIDで取得", description = "指定されたIDのゲーム情報を取得します")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "ゲームが見つかりました"),
        @ApiResponse(responseCode = "404", description = "ゲームが見つかりません")
    })
    @GetMapping("/games/{gameId}")
    public ResponseEntity<GameQueryDto> getGame(
            @Parameter(description = "ゲームID") @PathVariable UUID gameId) {
        GameQueryDto game = queryService.getGameById(gameId);
        return ResponseEntity.ok(game);
    }

    @Operation(summary = "全ゲーム一覧", description = "ページネーション付きでゲーム一覧を取得します")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/games")
    public ResponseEntity<Page<GameQueryDto>> getAllGames(
            @Parameter(description = "ページ番号（0から開始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "ページサイズ") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GameQueryDto> games = queryService.getAllGames(pageable);
        return ResponseEntity.ok(games);
    }

    @Operation(summary = "ステータスでフィルタ", description = "ゲームステータスでフィルタリングします（IN_PROGRESS, FINISHED）")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/games/status/{status}")
    public ResponseEntity<Page<GameQueryDto>> getGamesByStatus(
            @Parameter(description = "ゲームステータス") @PathVariable String status,
            @Parameter(description = "ページ番号") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "ページサイズ") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GameQueryDto> games = queryService.getGamesByStatus(status, pageable);
        return ResponseEntity.ok(games);
    }

    @Operation(summary = "プレイヤーでフィルタ", description = "指定プレイヤーが参加しているゲームを取得します")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/games/player/{playerId}")
    public ResponseEntity<Page<GameQueryDto>> getGamesByPlayer(
            @Parameter(description = "プレイヤーID") @PathVariable UUID playerId,
            @Parameter(description = "ページ番号") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "ページサイズ") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GameQueryDto> games = queryService.getGamesByPlayer(playerId, pageable);
        return ResponseEntity.ok(games);
    }

    @Operation(summary = "ゲーム統計", description = "全体のゲーム統計情報を取得します")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/statistics")
    public ResponseEntity<GameStatistics> getGameStatistics() {
        GameStatistics statistics = queryService.getGameStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "プレイヤー統計", description = "指定プレイヤーの統計情報を取得します")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/statistics/player/{playerId}")
    public ResponseEntity<PlayerStatistics> getPlayerStatistics(
            @Parameter(description = "プレイヤーID") @PathVariable UUID playerId) {
        PlayerStatistics statistics = queryService.getPlayerStatistics(playerId);
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "日別対局数", description = "過去30日間の日別対局数を取得します（折れ線グラフ用）")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/statistics/daily")
    public ResponseEntity<List<DailyGameCountDto>> getDailyGameCounts() {
        return ResponseEntity.ok(queryService.getDailyGameCounts());
    }

    @Operation(summary = "プレイヤーランキング", description = "勝利数上位10名のランキングを取得します")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/statistics/rankings")
    public ResponseEntity<List<PlayerRankingDto>> getPlayerRankings() {
        return ResponseEntity.ok(queryService.getPlayerRankings());
    }

    @Operation(summary = "指し手履歴", description = "ゲームの全指し手履歴を取得します")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "404", description = "ゲームが見つかりません")
    })
    @GetMapping("/games/{gameId}/moves")
    public ResponseEntity<List<MoveHistoryDto>> getMoveHistory(
            @Parameter(description = "ゲームID") @PathVariable UUID gameId) {
        List<MoveHistoryDto> moveHistory = queryService.getMoveHistory(gameId);
        return ResponseEntity.ok(moveHistory);
    }

    @Operation(summary = "盤面状態取得", description = "特定の手数における盤面状態を取得します")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "404", description = "ゲームが見つかりません"),
        @ApiResponse(responseCode = "400", description = "不正な手数")
    })
    @GetMapping("/games/{gameId}/replay/state/{moveNumber}")
    public ResponseEntity<BoardStateDto> getBoardStateAtMove(
            @Parameter(description = "ゲームID") @PathVariable UUID gameId,
            @Parameter(description = "手数（0=初期状態）") @PathVariable Integer moveNumber) {
        BoardStateDto boardState = replayService.getBoardStateAtMove(gameId, moveNumber);
        return ResponseEntity.ok(boardState);
    }

    @Operation(summary = "再生用指し手取得", description = "棋譜再生用の全指し手を取得します")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "404", description = "ゲームが見つかりません")
    })
    @GetMapping("/games/{gameId}/replay/moves")
    public ResponseEntity<List<MoveHistoryDto>> getMovesForReplay(
            @Parameter(description = "ゲームID") @PathVariable UUID gameId) {
        List<MoveHistoryDto> moves = replayService.getMovesForReplay(gameId);
        return ResponseEntity.ok(moves);
    }

    @Operation(summary = "KIF形式エクスポート", description = "ゲームをKIF（棋譜）形式でエクスポートします")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "404", description = "ゲームが見つかりません")
    })
    @GetMapping(value = "/games/{gameId}/replay/kif", produces = "text/plain; charset=UTF-8")
    public ResponseEntity<String> exportKIF(
            @Parameter(description = "ゲームID") @PathVariable UUID gameId) {
        String kif = kifExportService.exportToKIF(gameId);
        return ResponseEntity.ok(kif);
    }
}
