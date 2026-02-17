package com.japanesechess.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameResponse {
    private UUID gameId;
    private String status;
    private String message;
}
