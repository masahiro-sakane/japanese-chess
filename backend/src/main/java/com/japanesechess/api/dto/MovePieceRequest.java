package com.japanesechess.api.dto;

import com.japanesechess.domain.PieceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovePieceRequest {
    @NotNull(message = "Player ID is required")
    private UUID player;

    @NotNull(message = "From row is required")
    @Min(value = 0, message = "From row must be between 0 and 8")
    @Max(value = 8, message = "From row must be between 0 and 8")
    private Integer fromRow;

    @NotNull(message = "From column is required")
    @Min(value = 0, message = "From column must be between 0 and 8")
    @Max(value = 8, message = "From column must be between 0 and 8")
    private Integer fromColumn;

    @NotNull(message = "To row is required")
    @Min(value = 0, message = "To row must be between 0 and 8")
    @Max(value = 8, message = "To row must be between 0 and 8")
    private Integer toRow;

    @NotNull(message = "To column is required")
    @Min(value = 0, message = "To column must be between 0 and 8")
    @Max(value = 8, message = "To column must be between 0 and 8")
    private Integer toColumn;

    @NotNull(message = "Piece type is required")
    private PieceType pieceType;

    private boolean promote;
}
