package com.japanesechess.query;

import java.util.UUID;

/**
 * Player statistics DTO
 *
 * @param playerId The player's ID
 * @param totalGames Total number of games the player has participated in
 * @param activeGames Number of active games for the player
 * @param wins Number of wins
 * @param losses Number of losses
 */
public record PlayerStatistics(
        UUID playerId,
        long totalGames,
        long activeGames,
        long wins,
        long losses
) {}
