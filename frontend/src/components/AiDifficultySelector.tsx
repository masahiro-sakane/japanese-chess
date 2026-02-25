import React from 'react'
import { RadioGroup } from '@atlaskit/radio'
import { token } from '@atlaskit/tokens'
import type { AiDifficulty } from '../types/api'

interface Props {
  difficulty: AiDifficulty
  onChange: (difficulty: AiDifficulty) => void
  disabled?: boolean
}

const DIFFICULTY_OPTIONS = [
  { label: '初級 (ランダム)', value: 'BEGINNER', name: 'ai-difficulty' },
  { label: '中級 (2手先)', value: 'INTERMEDIATE', name: 'ai-difficulty' },
  { label: '上級 (4手先)', value: 'ADVANCED', name: 'ai-difficulty' },
]

export const AiDifficultySelector: React.FC<Props> = ({ difficulty, onChange, disabled }) => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: token('space.100', '8px') }}>
      <span style={{ fontWeight: 600, color: token('color.text', '#172B4D'), fontSize: 14 }}>
        AI難易度
      </span>
      <RadioGroup
        options={DIFFICULTY_OPTIONS}
        value={difficulty}
        onChange={(e) => onChange(e.currentTarget.value as AiDifficulty)}
        isDisabled={disabled}
      />
    </div>
  )
}
