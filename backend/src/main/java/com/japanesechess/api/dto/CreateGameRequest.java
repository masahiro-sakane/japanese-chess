package com.japanesechess.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGameRequest {
    @NotNull(message = "Black player ID is required")
    private UUID blackPlayerId;

    @NotNull(message = "White player ID is required")
    private UUID whitePlayerId;
}
