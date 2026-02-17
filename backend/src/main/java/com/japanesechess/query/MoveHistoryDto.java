package com.japanesechess.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveHistoryDto {
    private Long id;
    private Integer moveNumber;
    private String player;
    private Integer fromRow;
    private Integer fromColumn;
    private Integer toRow;
    private Integer toColumn;
    private String pieceType;
    private Boolean promoted;
    private Boolean isDrop;
    private String capturedPiece;
    private Instant timestamp;
}
