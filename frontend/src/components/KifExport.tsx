import React, { useState } from 'react'
import { gameService } from '../services/gameService'

interface KifExportProps {
  gameId: string
}

export const KifExport: React.FC<KifExportProps> = ({ gameId }) => {
  const [kifContent, setKifContent] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)

  const loadKif = async () => {
    setIsLoading(true)
    setError(null)

    try {
      const kif = await gameService.exportKIF(gameId)
      setKifContent(kif)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to export KIF')
    } finally {
      setIsLoading(false)
    }
  }

  const copyToClipboard = async () => {
    if (!kifContent) return

    try {
      await navigator.clipboard.writeText(kifContent)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch (err) {
      setError('Failed to copy to clipboard')
    }
  }

  const downloadKif = () => {
    if (!kifContent) return

    const blob = new Blob([kifContent], { type: 'text/plain; charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `game-${gameId.substring(0, 8)}.kif`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  }

  return (
    <div className="kif-export">
      <h3>KIF Export (棋譜)</h3>

      {!kifContent && (
        <button
          onClick={loadKif}
          disabled={isLoading}
          className="btn btn-primary"
        >
          {isLoading ? 'Loading...' : 'Generate KIF'}
        </button>
      )}

      {error && <div className="error-message">{error}</div>}

      {kifContent && (
        <>
          <div className="kif-actions">
            <button onClick={copyToClipboard} className="btn btn-secondary">
              {copied ? 'Copied!' : 'Copy to Clipboard'}
            </button>
            <button onClick={downloadKif} className="btn btn-secondary">
              Download as .kif
            </button>
            <button
              onClick={() => setKifContent(null)}
              className="btn btn-secondary"
            >
              Close
            </button>
          </div>

          <div className="kif-content">
            <pre>{kifContent}</pre>
          </div>
        </>
      )}

      <style>{`
        .kif-export {
          background-color: white;
          border-radius: 8px;
          padding: 1rem;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
          margin-top: 1rem;
        }

        .kif-export h3 {
          margin: 0 0 1rem 0;
          color: #333;
        }

        .btn {
          padding: 0.5rem 1rem;
          border: 1px solid #ccc;
          border-radius: 4px;
          background-color: white;
          cursor: pointer;
          transition: all 0.2s;
          font-size: 1rem;
        }

        .btn:hover:not(:disabled) {
          background-color: #e0e0e0;
        }

        .btn:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .btn-primary {
          background-color: #2196f3;
          color: white;
          border-color: #2196f3;
        }

        .btn-primary:hover:not(:disabled) {
          background-color: #1976d2;
        }

        .btn-secondary {
          margin-right: 0.5rem;
        }

        .error-message {
          color: #d32f2f;
          background-color: #ffebee;
          padding: 0.75rem;
          border-radius: 4px;
          margin: 1rem 0;
        }

        .kif-actions {
          display: flex;
          gap: 0.5rem;
          margin-bottom: 1rem;
        }

        .kif-content {
          background-color: #f5f5f5;
          border: 1px solid #ccc;
          border-radius: 4px;
          padding: 1rem;
          max-height: 400px;
          overflow-y: auto;
        }

        .kif-content pre {
          margin: 0;
          font-family: 'Courier New', monospace;
          font-size: 0.9rem;
          line-height: 1.5;
          white-space: pre-wrap;
          word-wrap: break-word;
        }
      `}</style>
    </div>
  )
}
