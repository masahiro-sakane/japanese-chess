import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, AI_PLAYER_ID } from '../services/api'
import type { AiDifficulty, PlayerColor } from '../services/api'

const DIFFICULTY_LABELS: Record<AiDifficulty, string> = {
  BEGINNER: '初級',
  INTERMEDIATE: '中級',
  ADVANCED: '上級',
}

const DIFFICULTY_DESCRIPTIONS: Record<AiDifficulty, string> = {
  BEGINNER: 'ランダムに手を指します。将棋を始めたばかりの方向け。',
  INTERMEDIATE: '2手先まで読んで指します。ある程度の挑戦になります。',
  ADVANCED: '4手先まで読んで指します。強い相手と対局したい方向け。',
}

const COLOR_LABELS: Record<PlayerColor, string> = {
  BLACK: '先手（黒）',
  WHITE: '後手（白）',
}

export const CreateAiGamePage: React.FC = () => {
  const navigate = useNavigate()
  const [humanPlayerId] = useState(() => crypto.randomUUID())
  const [humanColor, setHumanColor] = useState<PlayerColor>('BLACK')
  const [difficulty, setDifficulty] = useState<AiDifficulty>('BEGINNER')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setError(null)

    try {
      const response = await api.createAiGame({
        humanPlayerId,
        humanColor,
        difficulty,
      })

      let retries = 0
      const maxRetries = 20
      let gameFound = false

      while (retries < maxRetries && !gameFound) {
        await new Promise(resolve => setTimeout(resolve, 300))

        try {
          await api.getGameById(response.gameId)
          gameFound = true
        } catch {
          // ゲームがまだ見つからない
        }

        retries++
      }

      if (!gameFound) {
        throw new Error('ゲームの作成に時間がかかっています。少し待ってから対局一覧から開いてください。')
      }

      navigate(`/game/${response.gameId}?playerId=${humanPlayerId}&aiPlayerId=${AI_PLAYER_ID}&aiColor=${response.aiColor}&difficulty=${difficulty}`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'AI対局の作成に失敗しました')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto', padding: '20px' }}>
      <h1>AI対局の作成</h1>
      <p style={{ color: '#666', marginBottom: '24px' }}>
        コンピュータと将棋を対局できます。難易度を選んで挑戦してみましょう。
      </p>

      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '24px' }}>
          <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold', fontSize: '16px' }}>
            手番を選択
          </label>
          <div style={{ display: 'flex', gap: '12px' }}>
            {(['BLACK', 'WHITE'] as PlayerColor[]).map((color) => (
              <button
                key={color}
                type="button"
                onClick={() => setHumanColor(color)}
                style={{
                  flex: 1,
                  padding: '16px',
                  border: humanColor === color ? '2px solid #007bff' : '2px solid #ddd',
                  borderRadius: '8px',
                  backgroundColor: humanColor === color ? '#e7f3ff' : '#fff',
                  cursor: 'pointer',
                  fontSize: '15px',
                  fontWeight: humanColor === color ? 'bold' : 'normal',
                  color: humanColor === color ? '#007bff' : '#333',
                  transition: 'all 0.2s',
                }}
              >
                {COLOR_LABELS[color]}
                {color === 'BLACK' && (
                  <div style={{ fontSize: '12px', color: '#888', marginTop: '4px', fontWeight: 'normal' }}>
                    先に指します
                  </div>
                )}
                {color === 'WHITE' && (
                  <div style={{ fontSize: '12px', color: '#888', marginTop: '4px', fontWeight: 'normal' }}>
                    後から指します
                  </div>
                )}
              </button>
            ))}
          </div>
        </div>

        <div style={{ marginBottom: '24px' }}>
          <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold', fontSize: '16px' }}>
            難易度を選択
          </label>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {(['BEGINNER', 'INTERMEDIATE', 'ADVANCED'] as AiDifficulty[]).map((level) => (
              <button
                key={level}
                type="button"
                onClick={() => setDifficulty(level)}
                style={{
                  padding: '14px 16px',
                  border: difficulty === level ? '2px solid #28a745' : '2px solid #ddd',
                  borderRadius: '8px',
                  backgroundColor: difficulty === level ? '#e8f5e9' : '#fff',
                  cursor: 'pointer',
                  textAlign: 'left',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  transition: 'all 0.2s',
                }}
              >
                <div>
                  <span style={{
                    fontWeight: 'bold',
                    fontSize: '15px',
                    color: difficulty === level ? '#28a745' : '#333',
                  }}>
                    {DIFFICULTY_LABELS[level]}
                  </span>
                  <div style={{ fontSize: '13px', color: '#666', marginTop: '2px' }}>
                    {DIFFICULTY_DESCRIPTIONS[level]}
                  </div>
                </div>
                {difficulty === level && (
                  <span style={{ color: '#28a745', fontSize: '18px' }}>✓</span>
                )}
              </button>
            ))}
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

        <div style={{ display: 'flex', gap: '12px' }}>
          <button
            type="submit"
            disabled={isLoading}
            style={{
              flex: 1,
              padding: '14px',
              backgroundColor: isLoading ? '#ccc' : '#28a745',
              color: 'white',
              border: 'none',
              borderRadius: '6px',
              fontSize: '16px',
              fontWeight: 'bold',
              cursor: isLoading ? 'not-allowed' : 'pointer',
            }}
          >
            {isLoading ? 'AI対局を準備中...' : `AI（${DIFFICULTY_LABELS[difficulty]}）と対局開始`}
          </button>

          <button
            type="button"
            onClick={() => navigate('/')}
            style={{
              padding: '14px 20px',
              backgroundColor: '#6c757d',
              color: 'white',
              border: 'none',
              borderRadius: '6px',
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
          backgroundColor: '#fff3cd',
          border: '1px solid #ffc107',
          borderRadius: '6px',
        }}
      >
        <h3 style={{ marginTop: 0, color: '#856404' }}>AI対局について</h3>
        <ul style={{ marginBottom: 0, color: '#856404' }}>
          <li>先手（黒）が最初に指します</li>
          <li>後手（白）を選ぶとAIが先手で最初の手を指してから対局が始まります</li>
          <li>上級AIは計算に時間がかかる場合があります</li>
        </ul>
      </div>
    </div>
  )
}
