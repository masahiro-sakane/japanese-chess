package com.japanesechess.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameQueryController.class)
@DisplayName("GameQueryController Test")
class GameQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GameQueryService queryService;

    @MockBean
    private GameReplayService replayService;

    @MockBean
    private KifExportService kifExportService;

    private UUID gameId1;
    private UUID gameId2;
    private UUID player1Id;
    private UUID player2Id;
    private GameQueryDto gameDto1;
    private GameQueryDto gameDto2;

    @BeforeEach
    void setUp() {
        gameId1 = UUID.randomUUID();
        gameId2 = UUID.randomUUID();
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();

        gameDto1 = new GameQueryDto();
        gameDto1.setGameId(gameId1);
        gameDto1.setBlackPlayerId(player1Id);
        gameDto1.setWhitePlayerId(player2Id);
        gameDto1.setStatus("IN_PROGRESS");
        gameDto1.setCurrentTurn("BLACK");
        gameDto1.setMoveCount(10);
        gameDto1.setCreatedAt(Instant.now());
        gameDto1.setUpdatedAt(Instant.now());

        gameDto2 = new GameQueryDto();
        gameDto2.setGameId(gameId2);
        gameDto2.setBlackPlayerId(player2Id);
        gameDto2.setWhitePlayerId(player1Id);
        gameDto2.setStatus("COMPLETED");
        gameDto2.setCurrentTurn("WHITE");
        gameDto2.setWinner("BLACK");
        gameDto2.setMoveCount(45);
        gameDto2.setCreatedAt(Instant.now());
        gameDto2.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("GET /api/queries/games should return paginated games")
    void getAllGames_shouldReturnPaginatedGames() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        List<GameQueryDto> gameList = Arrays.asList(gameDto1, gameDto2);
        Page<GameQueryDto> gamePage = new PageImpl<>(gameList, pageable, 2);

        when(queryService.getAllGames(any(Pageable.class))).thenReturn(gamePage);

        // Act & Assert
        mockMvc.perform(get("/api/queries/games")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].gameId", is(gameId1.toString())))
                .andExpect(jsonPath("$.content[0].status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.content[1].gameId", is(gameId2.toString())))
                .andExpect(jsonPath("$.content[1].status", is("COMPLETED")))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(20)));

        verify(queryService).getAllGames(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/queries/games with default pagination should use defaults")
    void getAllGames_withDefaultPagination_shouldUseDefaults() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<GameQueryDto> gamePage = new PageImpl<>(List.of(gameDto1), pageable, 1);

        when(queryService.getAllGames(any(Pageable.class))).thenReturn(gamePage);

        // Act & Assert
        mockMvc.perform(get("/api/queries/games")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.number", is(0)));

        verify(queryService).getAllGames(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/queries/games/status/{status} should return filtered games")
    void getGamesByStatus_shouldReturnFilteredGames() throws Exception {
        // Arrange
        String status = "IN_PROGRESS";
        Pageable pageable = PageRequest.of(0, 20);
        Page<GameQueryDto> gamePage = new PageImpl<>(List.of(gameDto1), pageable, 1);

        when(queryService.getGamesByStatus(eq(status), any(Pageable.class))).thenReturn(gamePage);

        // Act & Assert
        mockMvc.perform(get("/api/queries/games/status/{status}", status)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.totalElements", is(1)));

        verify(queryService).getGamesByStatus(eq(status), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/queries/games/status/{status} with invalid status should still work")
    void getGamesByStatus_withInvalidStatus_shouldReturnEmptyPage() throws Exception {
        // Arrange
        String status = "INVALID_STATUS";
        Pageable pageable = PageRequest.of(0, 20);
        Page<GameQueryDto> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(queryService.getGamesByStatus(eq(status), any(Pageable.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/queries/games/status/{status}", status)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));

        verify(queryService).getGamesByStatus(eq(status), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/queries/games/player/{playerId} should return player's games")
    void getGamesByPlayer_shouldReturnPlayerGames() throws Exception {
        // Arrange
        UUID playerId = player1Id;
        Pageable pageable = PageRequest.of(0, 20);
        List<GameQueryDto> gameList = Arrays.asList(gameDto1, gameDto2);
        Page<GameQueryDto> gamePage = new PageImpl<>(gameList, pageable, 2);

        when(queryService.getGamesByPlayer(eq(playerId), any(Pageable.class))).thenReturn(gamePage);

        // Act & Assert
        mockMvc.perform(get("/api/queries/games/player/{playerId}", playerId)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));

        verify(queryService).getGamesByPlayer(eq(playerId), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/queries/games/player/{playerId} with no games should return empty page")
    void getGamesByPlayer_withNoGames_shouldReturnEmptyPage() throws Exception {
        // Arrange
        UUID playerId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<GameQueryDto> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(queryService.getGamesByPlayer(eq(playerId), any(Pageable.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/queries/games/player/{playerId}", playerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        verify(queryService).getGamesByPlayer(eq(playerId), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/queries/statistics should return game statistics")
    void getGameStatistics_shouldReturnStatistics() throws Exception {
        // Arrange
        GameStatistics statistics = new GameStatistics(100L, 30L, 70L);

        when(queryService.getGameStatistics()).thenReturn(statistics);

        // Act & Assert
        mockMvc.perform(get("/api/queries/statistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGames", is(100)))
                .andExpect(jsonPath("$.activeGames", is(30)))
                .andExpect(jsonPath("$.completedGames", is(70)));

        verify(queryService).getGameStatistics();
    }

    @Test
    @DisplayName("GET /api/queries/statistics with zero games should return zero stats")
    void getGameStatistics_withZeroGames_shouldReturnZeroStats() throws Exception {
        // Arrange
        GameStatistics statistics = new GameStatistics(0L, 0L, 0L);

        when(queryService.getGameStatistics()).thenReturn(statistics);

        // Act & Assert
        mockMvc.perform(get("/api/queries/statistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGames", is(0)))
                .andExpect(jsonPath("$.activeGames", is(0)))
                .andExpect(jsonPath("$.completedGames", is(0)));

        verify(queryService).getGameStatistics();
    }

    @Test
    @DisplayName("GET /api/queries/statistics/player/{playerId} should return player statistics")
    void getPlayerStatistics_shouldReturnPlayerStats() throws Exception {
        // Arrange
        UUID playerId = player1Id;
        PlayerStatistics statistics = new PlayerStatistics(playerId, 50L, 10L, 25L, 15L);

        when(queryService.getPlayerStatistics(playerId)).thenReturn(statistics);

        // Act & Assert
        mockMvc.perform(get("/api/queries/statistics/player/{playerId}", playerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId", is(playerId.toString())))
                .andExpect(jsonPath("$.totalGames", is(50)))
                .andExpect(jsonPath("$.activeGames", is(10)))
                .andExpect(jsonPath("$.wins", is(25)))
                .andExpect(jsonPath("$.losses", is(15)));

        verify(queryService).getPlayerStatistics(playerId);
    }

    @Test
    @DisplayName("GET /api/queries/statistics/player/{playerId} with no games should return zero stats")
    void getPlayerStatistics_withNoGames_shouldReturnZeroStats() throws Exception {
        // Arrange
        UUID playerId = UUID.randomUUID();
        PlayerStatistics statistics = new PlayerStatistics(playerId, 0L, 0L, 0L, 0L);

        when(queryService.getPlayerStatistics(playerId)).thenReturn(statistics);

        // Act & Assert
        mockMvc.perform(get("/api/queries/statistics/player/{playerId}", playerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId", is(playerId.toString())))
                .andExpect(jsonPath("$.totalGames", is(0)))
                .andExpect(jsonPath("$.activeGames", is(0)))
                .andExpect(jsonPath("$.wins", is(0)))
                .andExpect(jsonPath("$.losses", is(0)));

        verify(queryService).getPlayerStatistics(playerId);
    }

    @Test
    @DisplayName("GET /api/queries/games with custom page size should respect the size")
    void getAllGames_withCustomPageSize_shouldRespectSize() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        Page<GameQueryDto> gamePage = new PageImpl<>(List.of(gameDto1), pageable, 10);

        when(queryService.getAllGames(any(Pageable.class))).thenReturn(gamePage);

        // Act & Assert
        mockMvc.perform(get("/api/queries/games")
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(5)))
                .andExpect(jsonPath("$.totalElements", is(10)))
                .andExpect(jsonPath("$.totalPages", is(2)));

        verify(queryService).getAllGames(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/queries/games with large page number should work")
    void getAllGames_withLargePageNumber_shouldWork() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(5, 20);
        Page<GameQueryDto> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(queryService.getAllGames(any(Pageable.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/queries/games")
                        .param("page", "5")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.number", is(5)));

        verify(queryService).getAllGames(any(Pageable.class));
    }
}
