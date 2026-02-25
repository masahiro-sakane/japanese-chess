package com.japanesechess.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.japanesechess.domain.AiDifficulty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGameRequest {
    private static final UUID AI_PLAYER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @NotNull(message = "Black player ID is required")
    private UUID blackPlayerId;

    // AI対戦時は "AI_PLAYER" 文字列を受け付けるため String で受け取る
    @NotNull(message = "White player ID is required")
    private String whitePlayerId;

    private boolean aiGame = false;
    private AiDifficulty aiDifficulty;

    @JsonIgnore
    public UUID getEffectiveWhitePlayerId() {
        if ("AI_PLAYER".equals(whitePlayerId)) {
            return AI_PLAYER_UUID;
        }
        return UUID.fromString(whitePlayerId);
    }
}
