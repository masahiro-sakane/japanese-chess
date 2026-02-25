import React from 'react'
import Spinner from '@atlaskit/spinner'
import InlineMessage from '@atlaskit/inline-message'
import { token } from '@atlaskit/tokens'

interface Props {
  difficulty: string | null
}

const DIFFICULTY_LABELS: Record<string, string> = {
  BEGINNER: '初級',
  INTERMEDIATE: '中級',
  ADVANCED: '上級',
}

export const AiThinkingIndicator: React.FC<Props> = ({ difficulty }) => {
  const label = difficulty ? DIFFICULTY_LABELS[difficulty] || difficulty : '?'

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      gap: token('space.150', '12px'),
      padding: `${token('space.150', '12px')} ${token('space.200', '16px')}`,
      backgroundColor: token('color.background.warning', '#FFFAE6'),
      border: `1px solid ${token('color.border.warning', '#FF991F')}`,
      borderRadius: 8,
      marginBottom: token('space.150', '12px'),
    }}>
      <Spinner size="medium" label="AI思考中" />
      <InlineMessage appearance="warning" title={`AI (${label}) が考え中...`} />
    </div>
  )
}
