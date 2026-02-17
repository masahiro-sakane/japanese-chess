package com.japanesechess.domain;

import lombok.Value;

@Value
public class Move {
    Position from;
    Position to;
    boolean promote;
    PieceType pieceType;
    PlayerColor player;
    boolean isDrop;
    PieceType capturedPiece;

    public static Move normalMove(Position from, Position to, PieceType pieceType, PlayerColor player) {
        return new Move(from, to, false, pieceType, player, false, null);
    }

    public static Move promoteMove(Position from, Position to, PieceType pieceType, PlayerColor player) {
        return new Move(from, to, true, pieceType, player, false, null);
    }

    public static Move dropMove(Position to, PieceType pieceType, PlayerColor player) {
        return new Move(null, to, false, pieceType, player, true, null);
    }

    public Move withCapturedPiece(PieceType capturedPiece) {
        return new Move(from, to, promote, pieceType, player, isDrop, capturedPiece);
    }

    @Override
    public String toString() {
        if (isDrop) {
            return String.format("%s drops %s at %s",
                player.getJapaneseName(),
                pieceType.getJapaneseName(),
                to);
        }

        String moveStr = String.format("%s moves %s from %s to %s",
            player.getJapaneseName(),
            pieceType.getJapaneseName(),
            from,
            to);

        if (promote) {
            moveStr += " (promote)";
        }

        if (capturedPiece != null) {
            moveStr += " capturing " + capturedPiece.getJapaneseName();
        }

        return moveStr;
    }
}
