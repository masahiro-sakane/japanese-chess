package com.japanesechess.websocket;

import com.japanesechess.query.GameQueryDto;
import com.japanesechess.query.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameWebSocketNotifier {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameQueryService queryService;

    public void notifyGameUpdated(UUID gameId) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendNotification(gameId);
                }
            });
        } else {
            sendNotification(gameId);
        }
    }

    private void sendNotification(UUID gameId) {
        try {
            GameQueryDto game = queryService.getGameById(gameId);
            messagingTemplate.convertAndSend("/topic/game/" + gameId, game);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification for game {}", gameId, e);
        }
    }
}
