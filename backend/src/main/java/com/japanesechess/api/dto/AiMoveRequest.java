package com.japanesechess.api.dto;

import com.japanesechess.ai.AiDifficulty;
import com.japanesechess.domain.PlayerColor;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiMoveRequest {
    @NotNull(message = "AI color is required")
    private PlayerColor aiColor;

    @NotNull(message = "Difficulty is required")
    private AiDifficulty difficulty;
}
