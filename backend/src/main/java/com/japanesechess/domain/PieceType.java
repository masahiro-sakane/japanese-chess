package com.japanesechess.domain;

public enum PieceType {
    KING("玉将", "K"),
    ROOK("飛車", "R"),
    BISHOP("角行", "B"),
    GOLD("金将", "G"),
    SILVER("銀将", "S"),
    KNIGHT("桂馬", "N"),
    LANCE("香車", "L"),
    PAWN("歩兵", "P"),

    PROMOTED_ROOK("龍王", "+R"),
    PROMOTED_BISHOP("龍馬", "+B"),
    PROMOTED_SILVER("成銀", "+S"),
    PROMOTED_KNIGHT("成桂", "+N"),
    PROMOTED_LANCE("成香", "+L"),
    PROMOTED_PAWN("と金", "+P");

    private final String japaneseName;
    private final String symbol;

    PieceType(String japaneseName, String symbol) {
        this.japaneseName = japaneseName;
        this.symbol = symbol;
    }

    public String getJapaneseName() {
        return japaneseName;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean canPromote() {
        return switch (this) {
            case ROOK, BISHOP, SILVER, KNIGHT, LANCE, PAWN -> true;
            default -> false;
        };
    }

    public PieceType promote() {
        return switch (this) {
            case ROOK -> PROMOTED_ROOK;
            case BISHOP -> PROMOTED_BISHOP;
            case SILVER -> PROMOTED_SILVER;
            case KNIGHT -> PROMOTED_KNIGHT;
            case LANCE -> PROMOTED_LANCE;
            case PAWN -> PROMOTED_PAWN;
            default -> throw new IllegalStateException("Cannot promote " + this);
        };
    }

    public boolean isPromoted() {
        return switch (this) {
            case PROMOTED_ROOK, PROMOTED_BISHOP, PROMOTED_SILVER,
                 PROMOTED_KNIGHT, PROMOTED_LANCE, PROMOTED_PAWN -> true;
            default -> false;
        };
    }

    public PieceType unpromote() {
        return switch (this) {
            case PROMOTED_ROOK -> ROOK;
            case PROMOTED_BISHOP -> BISHOP;
            case PROMOTED_SILVER -> SILVER;
            case PROMOTED_KNIGHT -> KNIGHT;
            case PROMOTED_LANCE -> LANCE;
            case PROMOTED_PAWN -> PAWN;
            default -> this;
        };
    }
}
