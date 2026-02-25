package com.japanesechess.api;

import com.japanesechess.ai.AiDifficulty;
import com.japanesechess.ai.AiGameService;
import com.japanesechess.ai.AiMoveResult;
import com.japanesechess.api.dto.AiMoveRequest;
import com.japanesechess.api.dto.CreateAiGameRequest;
import com.japanesechess.api.dto.GameResponse;
import com.japanesechess.command.CreateGameCommand;
import com.japanesechess.domain.PlayerColor;
import com.japanesechess.service.GameCommandHandler;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final UUID AI_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final GameCommandHandler commandHandler;
    private final AiGameService aiGameService;

    public AiController(GameCommandHandler commandHandler, AiGameService aiGameService) {
        this.commandHandler = commandHandler;
        this.aiGameService = aiGameService;
    }

    @PostMapping("/games")
    public ResponseEntity<Map<String, Object>> createAiGame(
        @Valid @RequestBody CreateAiGameRequest request
    ) {
        UUID gameId = UUID.randomUUID();
        UUID humanPlayerId = request.getHumanPlayerId();
        PlayerColor humanColor = request.getHumanColor();

        UUID blackPlayerId = humanColor == PlayerColor.BLACK ? humanPlayerId : AI_PLAYER_ID;
        UUID whitePlayerId = humanColor == PlayerColor.WHITE ? humanPlayerId : AI_PLAYER_ID;

        CreateGameCommand command = new CreateGameCommand(gameId, blackPlayerId, whitePlayerId);
        commandHandler.handle(command);

        Map<String, Object> response = new HashMap<>();
        response.put("gameId", gameId);
        response.put("status", "created");
        response.put("message", "AI game created successfully");
        response.put("aiPlayerId", AI_PLAYER_ID);
        response.put("humanPlayerId", humanPlayerId);
        response.put("humanColor", humanColor.name());
        response.put("aiColor", humanColor.opposite().name());
        response.put("difficulty", request.getDifficulty().name());

        if (humanColor == PlayerColor.WHITE) {
            Optional<AiMoveResult> aiMove = aiGameService.makeAiMove(
                gameId,
                PlayerColor.BLACK,
                request.getDifficulty()
            );
            aiMove.ifPresent(result -> {
                response.put("aiFirstMove", result.getMoveDescription());
            });
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/games/{gameId}/ai-move")
    public ResponseEntity<Map<String, Object>> makeAiMove(
        @PathVariable UUID gameId,
        @Valid @RequestBody AiMoveRequest request
    ) {
        Optional<AiMoveResult> result = aiGameService.makeAiMove(
            gameId,
            request.getAiColor(),
            request.getDifficulty()
        );

        Map<String, Object> response = new HashMap<>();

        if (result.isPresent()) {
            AiMoveResult aiMove = result.get();
            response.put("success", true);
            response.put("gameId", gameId);
            response.put("moveDescription", aiMove.getMoveDescription());
            response.put("isDrop", aiMove.isDrop());
            response.put("aiColor", aiMove.getAiColor().name());
            response.put("difficulty", aiMove.getDifficulty().name());
        } else {
            response.put("success", false);
            response.put("gameId", gameId);
            response.put("message", "AI could not make a move (game may be over or it's not AI's turn)");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/difficulties")
    public ResponseEntity<List<Map<String, String>>> getDifficulties() {
        List<Map<String, String>> difficulties = Arrays.stream(AiDifficulty.values())
            .map(d -> {
                Map<String, String> map = new HashMap<>();
                map.put("value", d.name());
                map.put("label", d.getJapaneseName());
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(difficulties);
    }
}
