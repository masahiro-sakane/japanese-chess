package com.japanesechess.query;

import com.japanesechess.readmodel.GameEntity;
import com.japanesechess.readmodel.GameEntityRepository;
import com.japanesechess.readmodel.GameViewEntity;
import com.japanesechess.readmodel.GameViewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Disabled("Requires PostgreSQL database to be running")
class GameQueryIntegrationTest {

    @Autowired
    private GameEntityRepository gameEntityRepository;

    @Autowired
    private GameViewRepository gameViewRepository;

    @Autowired
    private GameQueryService queryService;

    @BeforeEach
    void setUp() {
        gameEntityRepository.deleteAll();
        gameViewRepository.deleteAll();
    }

    @Test
    void fullLifecycle_createQueryUpdateQuery_worksEndToEnd() {
        // Create test games
        UUID gameId1 = createAndSaveGame("IN_PROGRESS", UUID.randomUUID(), UUID.randomUUID());
        UUID gameId2 = createAndSaveGame("FINISHED", UUID.randomUUID(), UUID.randomUUID());
        UUID gameId3 = createAndSaveGame("IN_PROGRESS", UUID.randomUUID(), UUID.randomUUID());

        // Query all games
        Page<GameQueryDto> allGames = queryService.getAllGames(PageRequest.of(0, 10));
        assertEquals(3, allGames.getTotalElements());

        // Query by status
        Page<GameQueryDto> activeGames = queryService.getGamesByStatus(
            "IN_PROGRESS",
            PageRequest.of(0, 10)
        );
        assertEquals(2, activeGames.getTotalElements());

        // Get statistics
        GameStatistics stats = queryService.getGameStatistics();
        assertEquals(3, stats.totalGames());
        assertEquals(2, stats.activeGames());
    }

    @Test
    void pagination_largeDataSet_returnsCorrectPages() {
        // Create 50 games
        UUID playerId = UUID.randomUUID();
        for (int i = 0; i < 50; i++) {
            createAndSaveGame("IN_PROGRESS", playerId, UUID.randomUUID());
        }

        // Page 1
        Page<GameQueryDto> page1 = queryService.getAllGames(PageRequest.of(0, 20));
        assertEquals(20, page1.getContent().size());
        assertEquals(50, page1.getTotalElements());
        assertEquals(3, page1.getTotalPages());

        // Page 2
        Page<GameQueryDto> page2 = queryService.getAllGames(PageRequest.of(1, 20));
        assertEquals(20, page2.getContent().size());

        // Page 3
        Page<GameQueryDto> page3 = queryService.getAllGames(PageRequest.of(2, 20));
        assertEquals(10, page3.getContent().size());
    }

    @Test
    void getGamesByPlayer_returnsCorrectGames() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        UUID player3 = UUID.randomUUID();

        // Player1 games (as black player)
        createAndSaveGame("IN_PROGRESS", player1, player2);
        createAndSaveGame("FINISHED", player1, player3);

        // Player2 games (as white player)
        createAndSaveGame("IN_PROGRESS", player3, player2);

        // Check player1 games
        Page<GameQueryDto> player1Games = queryService.getGamesByPlayer(
            player1,
            PageRequest.of(0, 10)
        );
        assertEquals(2, player1Games.getTotalElements());

        // Check player2 games
        Page<GameQueryDto> player2Games = queryService.getGamesByPlayer(
            player2,
            PageRequest.of(0, 10)
        );
        assertEquals(2, player2Games.getTotalElements());

        // Check player3 games
        Page<GameQueryDto> player3Games = queryService.getGamesByPlayer(
            player3,
            PageRequest.of(0, 10)
        );
        assertEquals(2, player3Games.getTotalElements());
    }

    @Test
    void getPlayerStatistics_calculatesCorrectly() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        // Player1: 2 active, 1 win
        createAndSaveGame("IN_PROGRESS", player1, player2);
        createAndSaveGame("IN_PROGRESS", player1, player2);
        GameEntity winGame = createGameEntity("FINISHED", player1, player2);
        winGame.setWinner(player1.toString());
        gameEntityRepository.save(winGame);
        createGameView(winGame.getGameId());

        // Get player1 statistics
        PlayerStatistics stats = queryService.getPlayerStatistics(player1);

        assertEquals(player1, stats.playerId());
        assertEquals(3, stats.totalGames());
        assertEquals(2, stats.activeGames());
        assertEquals(1, stats.wins());
    }

    @Test
    void getGameById_existingGame_returnsCorrectData() {
        UUID gameId = createAndSaveGame("IN_PROGRESS", UUID.randomUUID(), UUID.randomUUID());

        GameQueryDto game = queryService.getGameById(gameId);

        assertNotNull(game);
        assertEquals(gameId, game.getGameId());
        assertEquals("IN_PROGRESS", game.getStatus());
    }

    private UUID createAndSaveGame(String status, UUID blackPlayer, UUID whitePlayer) {
        GameEntity gameEntity = createGameEntity(status, blackPlayer, whitePlayer);
        gameEntityRepository.save(gameEntity);
        createGameView(gameEntity.getGameId());
        return gameEntity.getGameId();
    }

    private GameEntity createGameEntity(String status, UUID blackPlayer, UUID whitePlayer) {
        GameEntity game = new GameEntity();
        game.setGameId(UUID.randomUUID());
        game.setBlackPlayerId(blackPlayer);
        game.setWhitePlayerId(whitePlayer);
        game.setStatus(status);
        game.setCurrentTurn("BLACK");
        return game;
    }

    private void createGameView(UUID gameId) {
        GameViewEntity gameView = new GameViewEntity();
        gameView.setGameId(gameId);
        gameView.setBoardState("[]");
        gameView.setBlackCapturedPieces("[]");
        gameView.setWhiteCapturedPieces("[]");
        gameView.setMoveCount(0);
        gameViewRepository.save(gameView);
    }
}
