package com.japanesechess.domain;

public enum PlayerColor {
    BLACK("先手"),
    WHITE("後手");

    private final String japaneseName;

    PlayerColor(String japaneseName) {
        this.japaneseName = japaneseName;
    }

    public String getJapaneseName() {
        return japaneseName;
    }

    public PlayerColor opposite() {
        return this == BLACK ? WHITE : BLACK;
    }
}
