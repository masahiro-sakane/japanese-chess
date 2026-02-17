package com.japanesechess.command;

import com.japanesechess.domain.PlayerColor;
import lombok.Value;

import java.util.UUID;

@Value
public class ResignGameCommand {
    UUID gameId;
    PlayerColor player;
}
