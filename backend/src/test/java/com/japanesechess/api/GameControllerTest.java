package com.japanesechess.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japanesechess.api.dto.CreateGameRequest;
import com.japanesechess.api.dto.MovePieceRequest;
import com.japanesechess.api.dto.ResignGameRequest;
import com.japanesechess.domain.PieceType;
import com.japanesechess.domain.PlayerColor;
import com.japanesechess.query.GameQueryDto;
import com.japanesechess.query.GameQueryService;
import com.japanesechess.service.GameCommandHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import com.japanesechess.command.CreateGameCommand;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GameCommandHandler commandHandler;

    @MockBean
    private GameQueryService queryService;

    @Test
    void createGame_ShouldReturn201_WhenValidRequest() throws Exception {
        UUID gameId = UUID.randomUUID();
        when(commandHandler.handle(any(CreateGameCommand.class))).thenReturn(gameId);

        CreateGameRequest request = new CreateGameRequest();
        request.setBlackPlayerId(UUID.randomUUID());
        request.setWhitePlayerId(UUID.randomUUID().toString());

        mockMvc.perform(post("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("created"))
            .andExpect(jsonPath("$.gameId").exists());
    }

    @Test
    void createGame_ShouldReturn400_WhenInvalidRequest() throws Exception {
        CreateGameRequest request = new CreateGameRequest();
        request.setBlackPlayerId(null);
        request.setWhitePlayerId(null);

        mockMvc.perform(post("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void makeMove_ShouldReturn200_WhenValidRequest() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        // Set up game query to identify player as BLACK
        GameQueryDto gameDto = new GameQueryDto();
        gameDto.setBlackPlayerId(playerId);
        gameDto.setWhitePlayerId(UUID.randomUUID());
        when(queryService.getGameById(eq(gameId))).thenReturn(gameDto);

        MovePieceRequest request = new MovePieceRequest(
            playerId,
            6, 4,
            5, 4,
            PieceType.PAWN,
            false
        );

        mockMvc.perform(post("/api/games/" + gameId + "/moves")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("move_made"));
    }

    @Test
    void resignGame_ShouldReturn200_WhenValidRequest() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        // Set up game query to identify player as BLACK
        GameQueryDto gameDto = new GameQueryDto();
        gameDto.setBlackPlayerId(playerId);
        gameDto.setWhitePlayerId(UUID.randomUUID());
        when(queryService.getGameById(eq(gameId))).thenReturn(gameDto);

        ResignGameRequest request = new ResignGameRequest(playerId);

        mockMvc.perform(post("/api/games/" + gameId + "/resign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("resigned"));
    }
}
