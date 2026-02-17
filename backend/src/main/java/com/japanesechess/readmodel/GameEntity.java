package com.japanesechess.readmodel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameEntity {

    @Id
    @Column(name = "game_id")
    private UUID gameId;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "black_player_id", nullable = false)
    private UUID blackPlayerId;

    @Column(name = "white_player_id", nullable = false)
    private UUID whitePlayerId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "current_turn", length = 10)
    private String currentTurn;

    @Column(name = "winner", length = 10)
    private String winner;

    @Column(name = "end_reason", length = 20)
    private String endReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
