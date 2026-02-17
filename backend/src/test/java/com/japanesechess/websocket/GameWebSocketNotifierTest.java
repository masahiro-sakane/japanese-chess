package com.japanesechess.websocket;

import com.japanesechess.query.GameQueryDto;
import com.japanesechess.query.GameQueryService;
import com.japanesechess.exception.GameNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameWebSocketNotifierTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private GameQueryService queryService;

    private GameWebSocketNotifier notifier;

    @BeforeEach
    void setUp() {
        notifier = new GameWebSocketNotifier(messagingTemplate, queryService);
    }

    @Test
    void notifyGameUpdated_ShouldSendGameDtoToTopic() {
        UUID gameId = UUID.randomUUID();
        GameQueryDto gameDto = mock(GameQueryDto.class);
        when(queryService.getGameById(gameId)).thenReturn(gameDto);

        notifier.notifyGameUpdated(gameId);

        verify(queryService).getGameById(gameId);
        verify(messagingTemplate).convertAndSend(
            eq("/topic/game/" + gameId),
            eq(gameDto)
        );
    }

    @Test
    void notifyGameUpdated_ShouldFetchLatestGameStateBeforeSending() {
        UUID gameId = UUID.randomUUID();
        GameQueryDto gameDto = mock(GameQueryDto.class);
        when(queryService.getGameById(gameId)).thenReturn(gameDto);

        notifier.notifyGameUpdated(gameId);

        var inOrder = inOrder(queryService, messagingTemplate);
        inOrder.verify(queryService).getGameById(gameId);
        inOrder.verify(messagingTemplate).convertAndSend(anyString(), eq(gameDto));
    }

    @Test
    void notifyGameUpdated_ShouldNotThrow_WhenQueryServiceFails() {
        UUID gameId = UUID.randomUUID();
        when(queryService.getGameById(gameId)).thenThrow(new GameNotFoundException(gameId));

        assertDoesNotThrow(() -> notifier.notifyGameUpdated(gameId));
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void notifyGameUpdated_ShouldNotThrow_WhenMessagingTemplateFails() {
        UUID gameId = UUID.randomUUID();
        GameQueryDto gameDto = mock(GameQueryDto.class);
        when(queryService.getGameById(gameId)).thenReturn(gameDto);
        doThrow(new RuntimeException("Broker unavailable"))
            .when(messagingTemplate).convertAndSend(anyString(), eq(gameDto));

        assertDoesNotThrow(() -> notifier.notifyGameUpdated(gameId));
    }
}
