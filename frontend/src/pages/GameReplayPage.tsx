import { useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useReplayStore } from '../stores/replayStore'
import { Board } from '../components/Board'
import { ReplayControls } from '../components/ReplayControls'
import { MoveList } from '../components/MoveList'
import { KifExport } from '../components/KifExport'

export function GameReplayPage() {
  const { gameId } = useParams<{ gameId: string }>()
  const { boardState, isLoading, error, loadReplay, reset } = useReplayStore()

  useEffect(() => {
    if (gameId) {
      loadReplay(gameId)
    }

    return () => {
      reset()
    }
  }, [gameId, loadReplay, reset])

  if (isLoading && !boardState) {
    return <div className="loading">Loading replay...</div>
  }

  if (error) {
    return (
      <div className="error">
        <p>Error: {error}</p>
        <Link to={`/game/${gameId}`}>Back to Game Detail</Link>
      </div>
    )
  }

  if (!boardState || !gameId) {
    return (
      <div className="empty-state">
        <p>Replay not available</p>
        <Link to="/">Back to Game List</Link>
      </div>
    )
  }

  return (
    <div className="page game-replay-page">
      <div className="replay-header">
        <Link to={`/game/${gameId}`} className="back-link">
          ← Back to Game Detail
        </Link>
        <h2>Game Replay (棋譜再生)</h2>
      </div>

      <div className="replay-content">
        <div className="replay-main">
          <ReplayControls />

          <div className="board-section">
            <h3>Board Position</h3>
            <Board boardState={boardState.pieces} />

            <div className="captured-pieces-display">
              <div className="captured-section">
                <h4>Black Captured (先手の持ち駒)</h4>
                <div className="captured-list">
                  {boardState.blackCapturedPieces.length > 0
                    ? boardState.blackCapturedPieces.join(', ')
                    : 'None'}
                </div>
              </div>
              <div className="captured-section">
                <h4>White Captured (後手の持ち駒)</h4>
                <div className="captured-list">
                  {boardState.whiteCapturedPieces.length > 0
                    ? boardState.whiteCapturedPieces.join(', ')
                    : 'None'}
                </div>
              </div>
            </div>

            <div className="current-player">
              Current Turn: {boardState.currentPlayer === 'BLACK' ? '先手 (Black)' : '後手 (White)'}
            </div>
          </div>
        </div>

        <div className="replay-sidebar">
          <MoveList />
          <KifExport gameId={gameId} />
        </div>
      </div>

      <style>{`
        .game-replay-page {
          max-width: 1600px;
          margin: 0 auto;
        }

        .replay-header {
          margin-bottom: 2rem;
        }

        .back-link {
          display: inline-block;
          margin-bottom: 1rem;
          color: #2196f3;
          text-decoration: none;
          font-weight: 500;
        }

        .back-link:hover {
          text-decoration: underline;
        }

        .replay-content {
          display: grid;
          grid-template-columns: 1fr 400px;
          gap: 2rem;
        }

        .replay-main {
          display: flex;
          flex-direction: column;
          gap: 1rem;
        }

        .board-section {
          background-color: white;
          border-radius: 8px;
          padding: 1rem;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }

        .board-section h3 {
          margin: 0 0 1rem 0;
          color: #333;
        }

        .captured-pieces-display {
          margin-top: 1.5rem;
          padding-top: 1rem;
          border-top: 1px solid #e0e0e0;
        }

        .captured-section {
          margin-bottom: 1rem;
        }

        .captured-section h4 {
          margin: 0 0 0.5rem 0;
          font-size: 0.9rem;
          color: #666;
        }

        .captured-list {
          padding: 0.5rem;
          background-color: #f5f5f5;
          border-radius: 4px;
          font-family: monospace;
        }

        .current-player {
          margin-top: 1rem;
          padding: 0.75rem;
          background-color: #e3f2fd;
          border-left: 4px solid #2196f3;
          font-weight: bold;
        }

        .replay-sidebar {
          display: flex;
          flex-direction: column;
          gap: 1rem;
        }

        .loading,
        .error,
        .empty-state {
          text-align: center;
          padding: 2rem;
          background-color: white;
          border-radius: 8px;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }

        @media (max-width: 1200px) {
          .replay-content {
            grid-template-columns: 1fr;
          }
        }
      `}</style>
    </div>
  )
}
