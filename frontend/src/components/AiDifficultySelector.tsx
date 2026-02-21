import React from 'react'
import type { AiDifficulty } from '../types/api'

interface Props {
  difficulty: AiDifficulty
  onChange: (difficulty: AiDifficulty) => void
  disabled?: boolean
}

const DIFFICULTY_LABELS: Record<AiDifficulty, string> = {
  BEGINNER: '初級 (ランダム)',
  INTERMEDIATE: '中級 (2手先)',
  ADVANCED: '上級 (4手先)',
}

export const AiDifficultySelector: React.FC<Props> = ({ difficulty, onChange, disabled }) => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
      <label style={{ fontWeight: 'bold' }}>AI難易度</label>
      <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
        {(Object.keys(DIFFICULTY_LABELS) as AiDifficulty[]).map((level) => (
          <label
            key={level}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              padding: '8px 16px',
              border: `2px solid ${difficulty === level ? '#007bff' : '#ddd'}`,
              borderRadius: '4px',
              cursor: disabled ? 'not-allowed' : 'pointer',
              backgroundColor: difficulty === level ? '#e7f3ff' : 'white',
              opacity: disabled ? 0.6 : 1,
            }}
          >
            <input
              type="radio"
              value={level}
              checked={difficulty === level}
              onChange={() => onChange(level)}
              disabled={disabled}
              style={{ margin: 0 }}
            />
            {DIFFICULTY_LABELS[level]}
          </label>
        ))}
      </div>
    </div>
  )
}
