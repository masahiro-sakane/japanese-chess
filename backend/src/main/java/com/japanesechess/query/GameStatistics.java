package com.japanesechess.query;

/**
 * Game statistics DTO
 *
 * @param totalGames Total number of games
 * @param activeGames Number of games in progress
 * @param completedGames Number of completed games
 */
public record GameStatistics(
        long totalGames,
        long activeGames,
        long completedGames
) {}
