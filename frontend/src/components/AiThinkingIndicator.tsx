import React from 'react'

interface Props {
  difficulty: string | null
}

const DIFFICULTY_LABELS: Record<string, string> = {
  BEGINNER: '初級',
  INTERMEDIATE: '中級',
  ADVANCED: '上級',
}

export const AiThinkingIndicator: React.FC<Props> = ({ difficulty }) => {
  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: '10px',
        padding: '12px 16px',
        backgroundColor: '#fff3cd',
        border: '1px solid #ffc107',
        borderRadius: '8px',
        marginBottom: '12px',
      }}
    >
      <div
        style={{
          width: '20px',
          height: '20px',
          border: '3px solid #ffc107',
          borderTopColor: 'transparent',
          borderRadius: '50%',
          animation: 'spin 0.8s linear infinite',
        }}
      />
      <span style={{ fontWeight: 'bold', color: '#856404' }}>
        AI ({difficulty ? DIFFICULTY_LABELS[difficulty] || difficulty : '?'}) が考え中...
      </span>
      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
    </div>
  )
}
