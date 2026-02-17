package com.japanesechess.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 駒の位置情報を表すDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PiecePositionDto {
    private Integer row;
    private Integer column;
    private String pieceType;
    private String owner;
    private Boolean promoted;
}
