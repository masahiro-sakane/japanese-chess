package com.japanesechess.ai;

import com.japanesechess.aggregate.Game;
import com.japanesechess.command.MovePieceCommand;
import com.japanesechess.command.DropPieceCommand;
import com.japanesechess.domain.*;
import com.japanesechess.repository.GameRepository;
import com.japanesechess.service.GameCommandHandler;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AiGameService {

    private final GameRepository gameRepository;
    private final GameCommandHandler commandHandler;
    private final MinimaxEngine minimaxEngine = new MinimaxEngine();

    public AiGameService(GameRepository gameRepository, GameCommandHandler commandHandler) {
        this.gameRepository = gameRepository;
        this.commandHandler = commandHandler;
    }

    public Optional<AiMoveResult> makeAiMove(UUID gameId, PlayerColor aiColor, AiDifficulty difficulty) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

        if (game.getStatus() != Game.GameStatus.IN_PROGRESS) {
            return Optional.empty();
        }

        if (game.getCurrentTurn() != aiColor) {
            return Optional.empty();
        }

        Board board = game.getBoard();
        Optional<Move> bestMove = minimaxEngine.findBestMove(board, aiColor, difficulty);

        if (bestMove.isEmpty()) {
            return Optional.empty();
        }

        Move move = bestMove.get();
        executeMove(gameId, move);

        return Optional.of(new AiMoveResult(move, aiColor, difficulty));
    }

    private void executeMove(UUID gameId, Move move) {
        if (move.isDrop()) {
            DropPieceCommand command = new DropPieceCommand(
                gameId,
                move.getPlayer(),
                move.getTo(),
                move.getPieceType()
            );
            commandHandler.handle(command);
        } else {
            MovePieceCommand command = new MovePieceCommand(
                gameId,
                move.getPlayer(),
                move.getFrom(),
                move.getTo(),
                move.isPromote(),
                move.getPieceType()
            );
            commandHandler.handle(command);
        }
    }
}
