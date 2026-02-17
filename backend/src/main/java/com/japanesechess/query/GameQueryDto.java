package com.japanesechess.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameQueryDto {
    private UUID gameId;
    private UUID blackPlayerId;
    private UUID whitePlayerId;
    private String status;
    private String currentTurn;
    private String winner;
    private List<PiecePosition> boardState;
    private List<String> blackCapturedPieces;
    private List<String> whiteCapturedPieces;
    private Integer moveCount;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PiecePosition {
        private Integer row;
        private Integer column;
        private String type;
        private String owner;
        private Boolean promoted;
    }
}
