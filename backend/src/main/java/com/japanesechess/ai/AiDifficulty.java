package com.japanesechess.ai;

public enum AiDifficulty {
    BEGINNER(0, "初級"),
    INTERMEDIATE(2, "中級"),
    ADVANCED(4, "上級");

    private final int searchDepth;
    private final String japaneseName;

    AiDifficulty(int searchDepth, String japaneseName) {
        this.searchDepth = searchDepth;
        this.japaneseName = japaneseName;
    }

    public int getSearchDepth() {
        return searchDepth;
    }

    public String getJapaneseName() {
        return japaneseName;
    }
}
