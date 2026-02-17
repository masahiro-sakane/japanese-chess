import React from 'react'
import { useReplayStore } from '../stores/replayStore'

export const MoveList: React.FC = () => {
  const { moves, currentMoveNumber, setCurrentMove } = useReplayStore()

  const formatPosition = (row: number | null, col: number | null): string => {
    if (row === null || col === null) return '-'
    return `(${row},${col})`
  }

  const formatMove = (
    isDrop: boolean,
    fromRow: number | null,
    fromCol: number | null,
    toRow: number,
    toCol: number
  ): string => {
    if (isDrop) {
      return `Drop → ${formatPosition(toRow, toCol)}`
    }
    return `${formatPosition(fromRow, fromCol)} → ${formatPosition(toRow, toCol)}`
  }

  return (
    <div className="move-list">
      <h3>Move History</h3>
      <div className="move-table-container">
        <table className="move-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Player</th>
              <th>Move</th>
              <th>Piece</th>
              <th>Captured</th>
            </tr>
          </thead>
          <tbody>
            {moves.map((move) => (
              <tr
                key={move.id}
                className={`move-row ${
                  move.moveNumber === currentMoveNumber ? 'current' : ''
                } ${move.moveNumber < currentMoveNumber ? 'past' : ''}`}
                onClick={() => setCurrentMove(move.moveNumber)}
              >
                <td className="move-number">{move.moveNumber}</td>
                <td className="move-player">
                  <span className={`player-badge ${move.player.toLowerCase()}`}>
                    {move.player}
                  </span>
                </td>
                <td className="move-position">
                  {formatMove(
                    move.isDrop,
                    move.fromRow,
                    move.fromColumn,
                    move.toRow,
                    move.toColumn
                  )}
                </td>
                <td className="move-piece">
                  {move.pieceType}
                  {move.promoted && ' (成)'}
                </td>
                <td className="move-captured">
                  {move.capturedPiece || '-'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <style>{`
        .move-list {
          background-color: white;
          border-radius: 8px;
          padding: 1rem;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }

        .move-list h3 {
          margin: 0 0 1rem 0;
          color: #333;
        }

        .move-table-container {
          max-height: 400px;
          overflow-y: auto;
          border: 1px solid #e0e0e0;
          border-radius: 4px;
        }

        .move-table {
          width: 100%;
          border-collapse: collapse;
        }

        .move-table th {
          position: sticky;
          top: 0;
          background-color: #f5f5f5;
          padding: 0.75rem;
          text-align: left;
          font-weight: bold;
          border-bottom: 2px solid #ccc;
          z-index: 10;
        }

        .move-table td {
          padding: 0.5rem 0.75rem;
          border-bottom: 1px solid #e0e0e0;
        }

        .move-row {
          cursor: pointer;
          transition: background-color 0.2s;
        }

        .move-row:hover {
          background-color: #f9f9f9;
        }

        .move-row.current {
          background-color: #e3f2fd;
          font-weight: bold;
        }

        .move-row.past {
          opacity: 0.7;
        }

        .move-number {
          font-weight: bold;
          text-align: center;
          width: 50px;
        }

        .player-badge {
          display: inline-block;
          padding: 0.25rem 0.5rem;
          border-radius: 4px;
          font-size: 0.8rem;
          font-weight: bold;
        }

        .player-badge.black {
          background-color: #333;
          color: white;
        }

        .player-badge.white {
          background-color: #f5f5f5;
          color: #333;
          border: 1px solid #ccc;
        }

        .move-position {
          font-family: monospace;
        }

        .move-piece {
          font-weight: 500;
        }

        .move-captured {
          color: #d32f2f;
          font-weight: 500;
        }
      `}</style>
    </div>
  )
}
