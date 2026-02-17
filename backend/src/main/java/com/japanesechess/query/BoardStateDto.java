package com.japanesechess.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 特定の手数における盤面状態を表すDTO
 * 棋譜再生機能で使用される
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardStateDto {
    private List<PiecePositionDto> pieces;
    private List<String> blackCapturedPieces;
    private List<String> whiteCapturedPieces;
    private Integer moveNumber;
    private String currentPlayer;
}
