package com.japanesechess.readmodel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "move_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private UUID gameId;

    @Column(name = "move_number", nullable = false)
    private Integer moveNumber;

    @Column(name = "player", nullable = false, length = 10)
    private String player;

    @Column(name = "from_row")
    private Integer fromRow;

    @Column(name = "from_column")
    private Integer fromColumn;

    @Column(name = "to_row", nullable = false)
    private Integer toRow;

    @Column(name = "to_column", nullable = false)
    private Integer toColumn;

    @Column(name = "piece_type", nullable = false, length = 20)
    private String pieceType;

    @Column(name = "promoted", nullable = false)
    private Boolean promoted;

    @Column(name = "is_drop", nullable = false)
    private Boolean isDrop;

    @Column(name = "captured_piece", length = 20)
    private String capturedPiece;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
