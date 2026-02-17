package com.japanesechess.command;

import com.japanesechess.domain.PieceType;
import com.japanesechess.domain.PlayerColor;
import com.japanesechess.domain.Position;
import lombok.Value;

import java.util.UUID;

@Value
public class DropPieceCommand {
    UUID gameId;
    PlayerColor player;
    Position to;
    PieceType pieceType;
}
