import React, { useEffect } from 'react'
import { useReplayStore } from '../stores/replayStore'

const SPEED_OPTIONS = [
  { label: '0.5x', value: 2000 },
  { label: '1x', value: 1000 },
  { label: '2x', value: 500 },
  { label: '5x', value: 200 },
  { label: '10x', value: 100 },
]

export const ReplayControls: React.FC = () => {
  const {
    currentMoveNumber,
    totalMoves,
    isPlaying,
    playbackSpeed,
    nextMove,
    previousMove,
    firstMove,
    lastMove,
    togglePlayback,
    setPlaybackSpeed,
    setCurrentMove,
  } = useReplayStore()

  useEffect(() => {
    if (!isPlaying) return

    const interval = setInterval(() => {
      if (currentMoveNumber < totalMoves) {
        nextMove()
      } else {
        togglePlayback()
      }
    }, playbackSpeed)

    return () => clearInterval(interval)
  }, [isPlaying, currentMoveNumber, totalMoves, playbackSpeed, nextMove, togglePlayback])

  const handleSliderChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const moveNumber = parseInt(event.target.value, 10)
    await setCurrentMove(moveNumber)
  }

  return (
    <div className="replay-controls">
      <div className="controls-buttons">
        <button
          onClick={firstMove}
          disabled={currentMoveNumber === 0}
          className="btn btn-control"
          title="First move"
        >
          ⏮
        </button>

        <button
          onClick={previousMove}
          disabled={currentMoveNumber === 0}
          className="btn btn-control"
          title="Previous move"
        >
          ◀
        </button>

        <button
          onClick={togglePlayback}
          className="btn btn-control btn-play"
          title={isPlaying ? 'Pause' : 'Play'}
        >
          {isPlaying ? '⏸' : '▶'}
        </button>

        <button
          onClick={nextMove}
          disabled={currentMoveNumber === totalMoves}
          className="btn btn-control"
          title="Next move"
        >
          ▶
        </button>

        <button
          onClick={lastMove}
          disabled={currentMoveNumber === totalMoves}
          className="btn btn-control"
          title="Last move"
        >
          ⏭
        </button>
      </div>

      <div className="controls-slider">
        <span className="move-counter">
          {currentMoveNumber} / {totalMoves}
        </span>
        <input
          type="range"
          min={0}
          max={totalMoves}
          value={currentMoveNumber}
          onChange={handleSliderChange}
          className="move-slider"
        />
      </div>

      <div className="controls-speed">
        <label>Speed:</label>
        {SPEED_OPTIONS.map((option) => (
          <button
            key={option.value}
            onClick={() => setPlaybackSpeed(option.value)}
            className={`btn btn-speed ${
              playbackSpeed === option.value ? 'active' : ''
            }`}
          >
            {option.label}
          </button>
        ))}
      </div>

      <style>{`
        .replay-controls {
          display: flex;
          flex-direction: column;
          gap: 1rem;
          padding: 1rem;
          background-color: #f5f5f5;
          border-radius: 8px;
          margin-bottom: 1rem;
        }

        .controls-buttons {
          display: flex;
          justify-content: center;
          gap: 0.5rem;
        }

        .btn {
          padding: 0.5rem 1rem;
          border: 1px solid #ccc;
          border-radius: 4px;
          background-color: white;
          cursor: pointer;
          transition: all 0.2s;
        }

        .btn:hover:not(:disabled) {
          background-color: #e0e0e0;
        }

        .btn:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .btn-control {
          font-size: 1.2rem;
          width: 3rem;
        }

        .btn-play {
          background-color: #4caf50;
          color: white;
          border-color: #4caf50;
        }

        .btn-play:hover:not(:disabled) {
          background-color: #45a049;
        }

        .controls-slider {
          display: flex;
          align-items: center;
          gap: 1rem;
        }

        .move-counter {
          font-weight: bold;
          min-width: 4rem;
          text-align: center;
        }

        .move-slider {
          flex: 1;
          height: 6px;
          border-radius: 3px;
          outline: none;
        }

        .controls-speed {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 0.5rem;
        }

        .controls-speed label {
          font-weight: bold;
        }

        .btn-speed {
          padding: 0.25rem 0.75rem;
          font-size: 0.9rem;
        }

        .btn-speed.active {
          background-color: #2196f3;
          color: white;
          border-color: #2196f3;
        }
      `}</style>
    </div>
  )
}
