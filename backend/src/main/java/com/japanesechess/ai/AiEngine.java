package com.japanesechess.ai;

import com.japanesechess.domain.AiDifficulty;
import com.japanesechess.domain.Board;
import com.japanesechess.domain.Move;
import com.japanesechess.domain.PlayerColor;

import java.util.Optional;

public interface AiEngine {
    Optional<Move> selectMove(Board board, PlayerColor player, AiDifficulty difficulty);
}
