package com.japanesechess.query;

public record CheckStatusDto(
    boolean blackInCheck,
    boolean whiteInCheck
) {}
