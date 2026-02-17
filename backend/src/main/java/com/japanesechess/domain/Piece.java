package com.japanesechess.domain;

import lombok.Value;
import lombok.With;

@Value
public class Piece {
    PieceType type;
    PlayerColor owner;
    @With Position position;

    public Piece promote() {
        if (!type.canPromote()) {
            throw new IllegalStateException("Cannot promote " + type);
        }
        return new Piece(type.promote(), owner, position);
    }

    public boolean isPromoted() {
        return type.isPromoted();
    }

    public boolean canPromoteAt(Position targetPosition) {
        if (!type.canPromote()) {
            return false;
        }

        if (owner == PlayerColor.BLACK) {
            return targetPosition.getRow() <= 2 || position.getRow() <= 2;
        } else {
            return targetPosition.getRow() >= 6 || position.getRow() >= 6;
        }
    }

    public boolean mustPromoteAt(Position targetPosition) {
        if (!type.canPromote()) {
            return false;
        }

        return switch (type) {
            case PAWN, LANCE -> {
                if (owner == PlayerColor.BLACK) {
                    yield targetPosition.getRow() == 0;
                } else {
                    yield targetPosition.getRow() == 8;
                }
            }
            case KNIGHT -> {
                if (owner == PlayerColor.BLACK) {
                    yield targetPosition.getRow() <= 1;
                } else {
                    yield targetPosition.getRow() >= 7;
                }
            }
            default -> false;
        };
    }

    @Override
    public String toString() {
        return String.format("%s's %s at %s",
            owner.getJapaneseName(),
            type.getJapaneseName(),
            position);
    }
}
