package com.japanesechess.api.dto;

import com.japanesechess.ai.AiDifficulty;
import com.japanesechess.domain.PlayerColor;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAiGameRequest {
    @NotNull(message = "Human player ID is required")
    private UUID humanPlayerId;

    @NotNull(message = "Human player color is required")
    private PlayerColor humanColor;

    @NotNull(message = "Difficulty is required")
    private AiDifficulty difficulty;
}
