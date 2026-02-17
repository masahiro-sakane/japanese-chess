package com.japanesechess.query;

import java.util.UUID;

/**
 * Player ranking DTO for ranking table/chart
 *
 * @param rank       Rank position (1-based)
 * @param playerId   Player UUID
 * @param totalGames Total games played
 * @param wins       Number of wins
 * @param losses     Number of losses
 * @param winRate    Win rate as a percentage (0-100)
 */
public record PlayerRankingDto(
        int rank,
        UUID playerId,
        long totalGames,
        long wins,
        long losses,
        double winRate
) {}
