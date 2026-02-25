package com.japanesechess.ai;

import com.japanesechess.domain.Board;
import com.japanesechess.domain.Move;
import com.japanesechess.domain.PlayerColor;

import java.util.Optional;

public interface AiStrategy {
    Optional<Move> selectMove(Board board, PlayerColor player);
}
