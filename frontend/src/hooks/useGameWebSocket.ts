import { useEffect, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import type { GameQueryDto } from '../types/api'

const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws'

export function useGameWebSocket(
  gameId: string | undefined,
  onUpdate: (game: GameQueryDto) => void
): void {
  const onUpdateRef = useRef(onUpdate)
  onUpdateRef.current = onUpdate

  useEffect(() => {
    if (!gameId) return

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/game/${gameId}`, (message) => {
          try {
            const game: GameQueryDto = JSON.parse(message.body)
            onUpdateRef.current(game)
          } catch (e) {
            console.error('Failed to parse WebSocket message:', e)
          }
        })
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers['message'], frame.body)
      },
      onWebSocketError: (event) => {
        console.error('WebSocket connection error:', event)
      },
    })

    client.activate()

    return () => {
      client.deactivate()
    }
  }, [gameId])
}
