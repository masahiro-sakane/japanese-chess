import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { gameService } from '../services/gameService'
import { AiDifficultySelector } from '../components/AiDifficultySelector'
import type { AiDifficulty } from '../types/api'

export const CreateGamePage: React.FC = () => {
  const navigate = useNavigate()
  const [blackPlayerId, setBlackPlayerId] = useState<string>(() => crypto.randomUUID())
  const [whitePlayerId] = useState<string>(() => crypto.randomUUID())
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [isAiGame, setIsAiGame] = useState(false)
  const [aiDifficulty, setAiDifficulty] = useState<AiDifficulty>('BEGINNER')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setError(null)

    const effectiveWhitePlayerId = isAiGame ? 'AI_PLAYER' : whitePlayerId

    try {
      const response = await gameService.createGame({
        blackPlayerId,
        whitePlayerId: effectiveWhitePlayerId,
        aiGame: isAiGame,
        aiDifficulty: isAiGame ? aiDifficulty : undefined,
      })

      let retries = 0
      const maxRetries = 20
      let gameFound = false

      while (retries < maxRetries && !gameFound) {
        await new Promise(resolve => setTimeout(resolve, 300))
        try {
          const checkResponse = await fetch(`http://localhost:8080/api/queries/games/${response.gameId}`)
          if (checkResponse.ok) {
            gameFound = true
          }
        } catch {
          // game not found yet
        }
        retries++
      }

      if (!gameFound) {
        throw new Error('ゲームの作成に時間がかかっています。少し待ってから対局一覧から開いてください。')
      }

      navigate(`/game/${response.gameId}`)
    } catch (err) {
      setError(err instanceof Error ? err.message : '対局の作成に失敗しました')
    } finally {
      setIsLoading(false)
    }
  }

  const generateRandomId = () => crypto.randomUUID()

  const inputStyle: React.CSSProperties = {
    flex: 1,
    padding: '8px',
    border: '1px solid #ccc',
    borderRadius: '4px',
    fontFamily: 'monospace',
  }

  const buttonSecondaryStyle: React.CSSProperties = {
    padding: '8px 16px',
    backgroundColor: '#6c757d',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
  }

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto', padding: '20px' }}>
      <h1>新規対局の作成</h1>

      <form onSubmit={handleSubmit}>
        {/* Game mode selection */}
        <div style={{ marginBottom: '24px' }}>
          <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
            対局モード
          </label>
          <div style={{ display: 'flex', gap: '12px' }}>
            <label
              style={{
                flex: 1,
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '12px',
                border: `2px solid ${!isAiGame ? '#007bff' : '#ddd'}`,
                borderRadius: '6px',
                cursor: 'pointer',
                backgroundColor: !isAiGame ? '#e7f3ff' : 'white',
              }}
            >
              <input
                type="radio"
                checked={!isAiGame}
                onChange={() => setIsAiGame(false)}
                style={{ margin: 0 }}
              />
              <div>
                <div style={{ fontWeight: 'bold' }}>対人戦</div>
                <div style={{ fontSize: '0.8rem', color: '#666' }}>2人のプレイヤーで対戦</div>
              </div>
            </label>
            <label
              style={{
                flex: 1,
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '12px',
                border: `2px solid ${isAiGame ? '#007bff' : '#ddd'}`,
                borderRadius: '6px',
                cursor: 'pointer',
                backgroundColor: isAiGame ? '#e7f3ff' : 'white',
              }}
            >
              <input
                type="radio"
                checked={isAiGame}
                onChange={() => setIsAiGame(true)}
                style={{ margin: 0 }}
              />
              <div>
                <div style={{ fontWeight: 'bold' }}>AI対戦</div>
                <div style={{ fontSize: '0.8rem', color: '#666' }}>AIと対戦 (あなたは先手)</div>
              </div>
            </label>
          </div>
        </div>

        {/* AI difficulty (shown only for AI game) */}
        {isAiGame && (
          <div style={{ marginBottom: '24px' }}>
            <AiDifficultySelector
              difficulty={aiDifficulty}
              onChange={setAiDifficulty}
              disabled={isLoading}
            />
          </div>
        )}

        {/* Player IDs */}
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            先手（あなた）プレイヤーID
          </label>
          <div style={{ display: 'flex', gap: '10px' }}>
            <input
              type="text"
              value={blackPlayerId}
              onChange={(e) => setBlackPlayerId(e.target.value)}
              style={inputStyle}
              required
            />
            <button
              type="button"
              onClick={() => setBlackPlayerId(generateRandomId())}
              style={buttonSecondaryStyle}
            >
              ランダム生成
            </button>
          </div>
        </div>

        {!isAiGame && (
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
              後手（白）プレイヤーID
            </label>
            <div style={{ display: 'flex', gap: '10px' }}>
              <input
                type="text"
                value={whitePlayerId}
                readOnly
                style={{ ...inputStyle, backgroundColor: '#f8f9fa' }}
              />
            </div>
          </div>
        )}

        {error && (
          <div
            style={{
              padding: '12px',
              marginBottom: '20px',
              backgroundColor: '#f8d7da',
              color: '#721c24',
              border: '1px solid #f5c6cb',
              borderRadius: '4px',
            }}
          >
            {error}
          </div>
        )}

        <div style={{ display: 'flex', gap: '10px' }}>
          <button
            type="submit"
            disabled={isLoading}
            style={{
              flex: 1,
              padding: '12px',
              backgroundColor: isLoading ? '#ccc' : '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              fontSize: '16px',
              fontWeight: 'bold',
              cursor: isLoading ? 'not-allowed' : 'pointer',
            }}
          >
            {isLoading ? '作成中...' : isAiGame ? 'AIと対局を開始' : '対局を開始'}
          </button>

          <button
            type="button"
            onClick={() => navigate('/')}
            style={{
              padding: '12px 24px',
              backgroundColor: '#6c757d',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              fontSize: '16px',
              cursor: 'pointer',
            }}
          >
            キャンセル
          </button>
        </div>
      </form>

      <div
        style={{
          marginTop: '30px',
          padding: '15px',
          backgroundColor: '#e7f3ff',
          border: '1px solid #b3d9ff',
          borderRadius: '4px',
        }}
      >
        <h3 style={{ marginTop: 0 }}>ヒント</h3>
        <ul style={{ marginBottom: 0 }}>
          <li>AI対戦モードでは、あなたが先手（黒）でAIが後手（白）になります</li>
          <li>難易度が高いほどAIは強くなりますが、応答に時間がかかります</li>
          <li>対人戦では2ブラウザタブやウィンドウで対戦できます</li>
        </ul>
      </div>
    </div>
  )
}
