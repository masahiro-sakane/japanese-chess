package com.japanesechess.api;

import com.japanesechess.api.dto.*;
import com.japanesechess.command.CreateGameCommand;
import com.japanesechess.command.DropPieceCommand;
import com.japanesechess.command.MovePieceCommand;
import com.japanesechess.command.ResignGameCommand;
import com.japanesechess.domain.PlayerColor;
import com.japanesechess.domain.Position;
import com.japanesechess.query.GameQueryService;
import com.japanesechess.query.GameQueryDto;
import com.japanesechess.service.GameCommandHandler;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameCommandHandler commandHandler;
    private final GameQueryService queryService;

    public GameController(GameCommandHandler commandHandler, GameQueryService queryService) {
        this.commandHandler = commandHandler;
        this.queryService = queryService;
    }

    private PlayerColor getPlayerColor(UUID gameId, UUID playerId) {
        GameQueryDto game = queryService.getGameById(gameId);

        if (playerId.equals(game.getBlackPlayerId())) {
            return PlayerColor.BLACK;
        } else if (playerId.equals(game.getWhitePlayerId())) {
            return PlayerColor.WHITE;
        } else {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Player " + playerId + " is not part of this game"
            );
        }
    }

    @PostMapping
    public ResponseEntity<GameResponse> createGame(@Valid @RequestBody CreateGameRequest request) {
        UUID gameId = UUID.randomUUID();

        CreateGameCommand command = new CreateGameCommand(
            gameId,
            request.getBlackPlayerId(),
            request.getWhitePlayerId()
        );

        commandHandler.handle(command);

        GameResponse response = new GameResponse(
            gameId,
            "created",
            "Game created successfully"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{gameId}/moves")
    public ResponseEntity<Map<String, Object>> makeMove(
        @PathVariable UUID gameId,
        @Valid @RequestBody MovePieceRequest request
    ) {
        PlayerColor playerColor = getPlayerColor(gameId, request.getPlayer());
        Position from = new Position(request.getFromRow(), request.getFromColumn());
        Position to = new Position(request.getToRow(), request.getToColumn());

        MovePieceCommand command = new MovePieceCommand(
            gameId,
            playerColor,
            from,
            to,
            request.isPromote(),
            request.getPieceType()
        );

        commandHandler.handle(command);

        // コマンド実行後、Projectionは同期的に更新済み
        PlayerColor nextTurn = playerColor.opposite();

        Map<String, Object> response = new HashMap<>();
        response.put("gameId", gameId);
        response.put("status", "move_made");
        response.put("message", "Move executed successfully");
        response.put("nextTurn", nextTurn.name());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{gameId}/drops")
    public ResponseEntity<GameResponse> dropPiece(
        @PathVariable UUID gameId,
        @Valid @RequestBody DropPieceRequest request
    ) {
        PlayerColor playerColor = getPlayerColor(gameId, request.getPlayer());
        Position to = new Position(request.getToRow(), request.getToColumn());

        DropPieceCommand command = new DropPieceCommand(
            gameId,
            playerColor,
            to,
            request.getPieceType()
        );

        commandHandler.handle(command);

        GameResponse response = new GameResponse(
            gameId,
            "piece_dropped",
            "Piece dropped successfully"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{gameId}/resign")
    public ResponseEntity<GameResponse> resignGame(
        @PathVariable UUID gameId,
        @Valid @RequestBody ResignGameRequest request
    ) {
        PlayerColor playerColor = getPlayerColor(gameId, request.getPlayer());

        ResignGameCommand command = new ResignGameCommand(
            gameId,
            playerColor
        );

        commandHandler.handle(command);

        GameResponse response = new GameResponse(
            gameId,
            "resigned",
            "Player resigned successfully"
        );

        return ResponseEntity.ok(response);
    }
}
