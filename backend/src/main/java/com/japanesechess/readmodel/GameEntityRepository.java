package com.japanesechess.readmodel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GameEntityRepository extends JpaRepository<GameEntity, UUID> {

    /**
     * Find games by status with pagination
     */
    Page<GameEntity> findByStatus(String status, Pageable pageable);

    /**
     * Find games where the player is either black or white player
     */
    @Query("SELECT g FROM GameEntity g WHERE g.blackPlayerId = :playerId OR g.whitePlayerId = :playerId")
    Page<GameEntity> findByPlayer(@Param("playerId") UUID playerId, Pageable pageable);

    /**
     * Count games by status
     */
    Long countByStatus(String status);

    /**
     * Count games where the player is either black or white player
     */
    @Query("SELECT COUNT(g) FROM GameEntity g WHERE g.blackPlayerId = :playerId OR g.whitePlayerId = :playerId")
    Long countByPlayer(@Param("playerId") UUID playerId);

    /**
     * Count games won by a specific player
     */
    @Query("SELECT COUNT(g) FROM GameEntity g WHERE (g.blackPlayerId = :playerId AND g.winner = 'BLACK') OR (g.whitePlayerId = :playerId AND g.winner = 'WHITE')")
    Long countByWinner(@Param("playerId") UUID playerId);

    /**
     * Count active games for a specific player
     */
    @Query("SELECT COUNT(g) FROM GameEntity g WHERE (g.blackPlayerId = :playerId OR g.whitePlayerId = :playerId) AND g.status = 'IN_PROGRESS'")
    Long countActiveByPlayer(@Param("playerId") UUID playerId);

    /**
     * Count games per day for the last N days (time-series)
     * Returns [date_string, count] pairs ordered by date ascending
     */
    @Query(value = """
            SELECT TO_CHAR(DATE_TRUNC('day', created_at AT TIME ZONE 'UTC'), 'YYYY-MM-DD') AS date,
                   COUNT(*) AS count
            FROM games
            WHERE created_at >= NOW() - INTERVAL '30 days'
            GROUP BY DATE_TRUNC('day', created_at AT TIME ZONE 'UTC')
            ORDER BY DATE_TRUNC('day', created_at AT TIME ZONE 'UTC') ASC
            """, nativeQuery = true)
    List<Object[]> countGamesPerDay();

    /**
     * Get all distinct player IDs (both black and white) with their win/loss counts
     * Returns [player_id, total_games, wins] tuples
     */
    @Query(value = """
            SELECT p.player_id,
                   COUNT(*) AS total_games,
                   SUM(CASE
                       WHEN (p.role = 'BLACK' AND g.winner = 'BLACK') OR (p.role = 'WHITE' AND g.winner = 'WHITE')
                       THEN 1 ELSE 0 END) AS wins
            FROM (
                SELECT game_id, black_player_id AS player_id, 'BLACK' AS role FROM games
                UNION ALL
                SELECT game_id, white_player_id AS player_id, 'WHITE' AS role FROM games
            ) p
            JOIN games g ON g.game_id = p.game_id
            WHERE g.status = 'FINISHED'
            GROUP BY p.player_id
            ORDER BY wins DESC, total_games DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> findPlayerRankings();
}
