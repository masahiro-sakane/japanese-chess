package com.japanesechess.readmodel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
