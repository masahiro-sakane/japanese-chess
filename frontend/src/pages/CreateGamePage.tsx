import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { gameService } from '../services/gameService'

export const CreateGamePage: React.FC = () => {
  const navigate = useNavigate()
  const [blackPlayerId, setBlackPlayerId] = useState(() => crypto.randomUUID())
  const [whitePlayerId, setWhitePlayerId] = useState(() => crypto.randomUUID())
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setError(null)

    try {
      const response = await gameService.createGame({
        blackPlayerId,
        whitePlayerId,
      })

      // Projectionの更新を待つ（ポーリング）
      let retries = 0
      const maxRetries = 20
      let gameFound = false

      while (retries < maxRetries && !gameFound) {
        await new Promise(resolve => setTimeout(resolve, 300))

        try {
          // ゲームが取得できるか確認
          const checkResponse = await fetch(`http://localhost:8080/api/queries/games/${response.gameId}`)
          if (checkResponse.ok) {
            gameFound = true
            console.log(`✅ Game found after ${retries + 1} attempts`)
          }
        } catch {
          // ゲームがまだ見つからない
        }

        retries++
        console.log(`⏳ Waiting for game projection... (${retries}/${maxRetries})`)
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

  const generateRandomId = () => {
    return crypto.randomUUID()
  }

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto', padding: '20px' }}>
      <h1>新規対局の作成</h1>

      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            先手（黒）プレイヤーID
          </label>
          <div style={{ display: 'flex', gap: '10px' }}>
            <input
              type="text"
              value={blackPlayerId}
              onChange={(e) => setBlackPlayerId(e.target.value)}
              style={{
                flex: 1,
                padding: '8px',
                border: '1px solid #ccc',
                borderRadius: '4px',
                fontFamily: 'monospace',
              }}
              required
            />
            <button
              type="button"
              onClick={() => setBlackPlayerId(generateRandomId())}
              style={{
                padding: '8px 16px',
                backgroundColor: '#6c757d',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
              }}
            >
              ランダム生成
            </button>
          </div>
        </div>

        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            後手（白）プレイヤーID
          </label>
          <div style={{ display: 'flex', gap: '10px' }}>
            <input
              type="text"
              value={whitePlayerId}
              onChange={(e) => setWhitePlayerId(e.target.value)}
              style={{
                flex: 1,
                padding: '8px',
                border: '1px solid #ccc',
                borderRadius: '4px',
                fontFamily: 'monospace',
              }}
              required
            />
            <button
              type="button"
              onClick={() => setWhitePlayerId(generateRandomId())}
              style={{
                padding: '8px 16px',
                backgroundColor: '#6c757d',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
              }}
            >
              ランダム生成
            </button>
          </div>
        </div>

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
            {isLoading ? '作成中...' : '対局を開始'}
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
          <li>プレイヤーIDはUUID形式で入力してください</li>
          <li>「ランダム生成」ボタンで自動的にIDを生成できます</li>
          <li>対局作成後、ゲーム詳細ページに自動的に移動します</li>
        </ul>
      </div>
    </div>
  )
}
