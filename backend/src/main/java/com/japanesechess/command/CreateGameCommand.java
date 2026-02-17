package com.japanesechess.command;

import lombok.Value;

import java.util.UUID;

@Value
public class CreateGameCommand {
    UUID gameId;
    UUID blackPlayerId;
    UUID whitePlayerId;
}
