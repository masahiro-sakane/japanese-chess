package com.japanesechess.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japanesechess.readmodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameQueryService Test")
class GameQueryServiceTest {

    @Mock
    private GameEntityRepository gameEntityRepository;

    @Mock
    private GameViewRepository gameViewRepository;

    @Mock
    private MoveHistoryRepository moveHistoryRepository;

    private ObjectMapper objectMapper;

    private GameQueryService gameQueryService;

    private UUID gameId1;
    private UUID gameId2;
    private UUID player1Id;
    private UUID player2Id;
    private GameEntity gameEntity1;
    private GameEntity gameEntity2;
    private GameViewEntity gameView1;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        gameQueryService = new GameQueryService(
                gameEntityRepository,
                gameViewRepository,
                moveHistoryRepository,
                objectMapper
        );

        gameId1 = UUID.randomUUID();
        gameId2 = UUID.randomUUID();
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();

        gameEntity1 = new GameEntity();
        gameEntity1.setGameId(gameId1);
        gameEntity1.setBlackPlayerId(player1Id);
        gameEntity1.setWhitePlayerId(player2Id);
        gameEntity1.setStatus("IN_PROGRESS");
        gameEntity1.setCurrentTurn("BLACK");
        gameEntity1.setCreatedAt(Instant.now());
        gameEntity1.setUpdatedAt(Instant.now());

        gameEntity2 = new GameEntity();
        gameEntity2.setGameId(gameId2);
        gameEntity2.setBlackPlayerId(player2Id);
        gameEntity2.setWhitePlayerId(player1Id);
        gameEntity2.setStatus("COMPLETED");
        gameEntity2.setCurrentTurn("WHITE");
        gameEntity2.setWinner("BLACK");
        gameEntity2.setCreatedAt(Instant.now());
        gameEntity2.setUpdatedAt(Instant.now());

        gameView1 = new GameViewEntity();
        gameView1.setGameId(gameId1);
        gameView1.setBoardState("[]");
        gameView1.setBlackCapturedPieces("[]");
        gameView1.setWhiteCapturedPieces("[]");
        gameView1.setMoveCount(0);
    }

    @Test
    @DisplayName("getAllGames() should return paginated games")
    void getAllGames_shouldReturnPaginatedGames() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        List<GameEntity> gameList = Arrays.asList(gameEntity1, gameEntity2);
        Page<GameEntity> gamePage = new PageImpl<>(gameList, pageable, 2);

        when(gameEntityRepository.findAll(pageable)).thenReturn(gamePage);
        when(gameViewRepository.findById(gameId1)).thenReturn(java.util.Optional.of(gameView1));
        when(gameViewRepository.findById(gameId2)).thenReturn(java.util.Optional.empty());

        // Act
        Page<GameQueryDto> result = gameQueryService.getAllGames(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(20);

        verify(gameEntityRepository).findAll(pageable);
        verify(gameViewRepository).findById(gameId1);
        verify(gameViewRepository).findById(gameId2);
    }

    @Test
    @DisplayName("getGamesByStatus() should return games filtered by status")
    void getGamesByStatus_shouldReturnFilteredGames() throws Exception {
        // Arrange
        String status = "IN_PROGRESS";
        Pageable pageable = PageRequest.of(0, 20);
        List<GameEntity> gameList = Arrays.asList(gameEntity1);
        Page<GameEntity> gamePage = new PageImpl<>(gameList, pageable, 1);

        when(gameEntityRepository.findByStatus(status, pageable)).thenReturn(gamePage);
        when(gameViewRepository.findById(gameId1)).thenReturn(java.util.Optional.of(gameView1));

        // Act
        Page<GameQueryDto> result = gameQueryService.getGamesByStatus(status, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(gameEntityRepository).findByStatus(status, pageable);
    }

    @Test
    @DisplayName("getGamesByPlayer() should return games for specific player")
    void getGamesByPlayer_shouldReturnPlayerGames() throws Exception {
        // Arrange
        UUID playerId = player1Id;
        Pageable pageable = PageRequest.of(0, 20);
        List<GameEntity> gameList = Arrays.asList(gameEntity1, gameEntity2);
        Page<GameEntity> gamePage = new PageImpl<>(gameList, pageable, 2);

        when(gameEntityRepository.findByPlayer(playerId, pageable)).thenReturn(gamePage);
        when(gameViewRepository.findById(gameId1)).thenReturn(java.util.Optional.of(gameView1));
        when(gameViewRepository.findById(gameId2)).thenReturn(java.util.Optional.empty());

        // Act
        Page<GameQueryDto> result = gameQueryService.getGamesByPlayer(playerId, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(gameEntityRepository).findByPlayer(playerId, pageable);
    }

    @Test
    @DisplayName("getGameStatistics() should return correct statistics")
    void getGameStatistics_shouldReturnCorrectStats() {
        // Arrange
        when(gameEntityRepository.count()).thenReturn(100L);
        when(gameEntityRepository.countByStatus("IN_PROGRESS")).thenReturn(30L);
        when(gameEntityRepository.countByStatus("COMPLETED")).thenReturn(70L);

        // Act
        GameStatistics result = gameQueryService.getGameStatistics();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.totalGames()).isEqualTo(100L);
        assertThat(result.activeGames()).isEqualTo(30L);
        assertThat(result.completedGames()).isEqualTo(70L);

        verify(gameEntityRepository).count();
        verify(gameEntityRepository).countByStatus("IN_PROGRESS");
        verify(gameEntityRepository).countByStatus("COMPLETED");
    }

    @Test
    @DisplayName("getPlayerStatistics() should return player-specific statistics")
    void getPlayerStatistics_shouldReturnPlayerStats() {
        // Arrange
        UUID playerId = player1Id;
        when(gameEntityRepository.countByPlayer(playerId)).thenReturn(50L);
        when(gameEntityRepository.countActiveByPlayer(playerId)).thenReturn(10L);
        when(gameEntityRepository.countByWinner(playerId)).thenReturn(25L);

        // Act
        PlayerStatistics result = gameQueryService.getPlayerStatistics(playerId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.playerId()).isEqualTo(playerId);
        assertThat(result.totalGames()).isEqualTo(50L);
        assertThat(result.activeGames()).isEqualTo(10L);
        assertThat(result.wins()).isEqualTo(25L);
        assertThat(result.losses()).isEqualTo(15L); // totalGames - activeGames - wins

        verify(gameEntityRepository).countByPlayer(playerId);
        verify(gameEntityRepository).countActiveByPlayer(playerId);
        verify(gameEntityRepository).countByWinner(playerId);
    }

    @Test
    @DisplayName("getAllGames() with empty page should return empty result")
    void getAllGames_withEmptyPage_shouldReturnEmptyResult() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<GameEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(gameEntityRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<GameQueryDto> result = gameQueryService.getAllGames(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        verify(gameEntityRepository).findAll(pageable);
        verifyNoInteractions(gameViewRepository);
    }

    @Test
    @DisplayName("getGamesByStatus() with no matching games should return empty page")
    void getGamesByStatus_withNoMatches_shouldReturnEmptyPage() {
        // Arrange
        String status = "ABANDONED";
        Pageable pageable = PageRequest.of(0, 20);
        Page<GameEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(gameEntityRepository.findByStatus(status, pageable)).thenReturn(emptyPage);

        // Act
        Page<GameQueryDto> result = gameQueryService.getGamesByStatus(status, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();

        verify(gameEntityRepository).findByStatus(status, pageable);
    }

    @Test
    @DisplayName("getGamesByPlayer() with no games should return empty page")
    void getGamesByPlayer_withNoGames_shouldReturnEmptyPage() {
        // Arrange
        UUID playerId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<GameEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(gameEntityRepository.findByPlayer(playerId, pageable)).thenReturn(emptyPage);

        // Act
        Page<GameQueryDto> result = gameQueryService.getGamesByPlayer(playerId, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();

        verify(gameEntityRepository).findByPlayer(playerId, pageable);
    }

    @Test
    @DisplayName("getGameStatistics() with zero games should return zero stats")
    void getGameStatistics_withZeroGames_shouldReturnZeroStats() {
        // Arrange
        when(gameEntityRepository.count()).thenReturn(0L);
        when(gameEntityRepository.countByStatus("IN_PROGRESS")).thenReturn(0L);
        when(gameEntityRepository.countByStatus("COMPLETED")).thenReturn(0L);

        // Act
        GameStatistics result = gameQueryService.getGameStatistics();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.totalGames()).isEqualTo(0L);
        assertThat(result.activeGames()).isEqualTo(0L);
        assertThat(result.completedGames()).isEqualTo(0L);
    }

    @Test
    @DisplayName("getPlayerStatistics() for player with no games should return zero stats")
    void getPlayerStatistics_withNoGames_shouldReturnZeroStats() {
        // Arrange
        UUID playerId = UUID.randomUUID();
        when(gameEntityRepository.countByPlayer(playerId)).thenReturn(0L);
        when(gameEntityRepository.countActiveByPlayer(playerId)).thenReturn(0L);
        when(gameEntityRepository.countByWinner(playerId)).thenReturn(0L);

        // Act
        PlayerStatistics result = gameQueryService.getPlayerStatistics(playerId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.playerId()).isEqualTo(playerId);
        assertThat(result.totalGames()).isEqualTo(0L);
        assertThat(result.activeGames()).isEqualTo(0L);
        assertThat(result.wins()).isEqualTo(0L);
        assertThat(result.losses()).isEqualTo(0L);
    }

    @Test
    @DisplayName("getAllGames() should handle pagination correctly")
    void getAllGames_shouldHandlePaginationCorrectly() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(1, 10); // Second page, 10 items per page
        List<GameEntity> gameList = Arrays.asList(gameEntity1);
        Page<GameEntity> gamePage = new PageImpl<>(gameList, pageable, 25);

        when(gameEntityRepository.findAll(pageable)).thenReturn(gamePage);
        when(gameViewRepository.findById(gameId1)).thenReturn(java.util.Optional.of(gameView1));

        // Act
        Page<GameQueryDto> result = gameQueryService.getAllGames(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }
}
