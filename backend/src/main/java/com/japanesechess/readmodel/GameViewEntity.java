package com.japanesechess.readmodel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "game_views")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameViewEntity {

    @Id
    @Column(name = "game_id")
    private UUID gameId;

    @Version
    @Column(name = "version")
    private Long version;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "board_state", columnDefinition = "jsonb", nullable = false)
    private String boardState;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "black_captured_pieces", columnDefinition = "jsonb", nullable = false)
    private String blackCapturedPieces;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "white_captured_pieces", columnDefinition = "jsonb", nullable = false)
    private String whiteCapturedPieces;

    @Column(name = "move_count", nullable = false)
    private Integer moveCount;
}
