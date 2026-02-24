// @vitest-environment jsdom
import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { AiThinkingIndicator } from '../AiThinkingIndicator'

describe('AiThinkingIndicator', () => {
  it('shows thinking message with BEGINNER difficulty', () => {
    render(<AiThinkingIndicator difficulty="BEGINNER" />)
    expect(screen.getByText(/AI.*初級.*考え中/)).toBeTruthy()
  })

  it('shows thinking message with INTERMEDIATE difficulty', () => {
    render(<AiThinkingIndicator difficulty="INTERMEDIATE" />)
    expect(screen.getByText(/AI.*中級.*考え中/)).toBeTruthy()
  })

  it('shows thinking message with ADVANCED difficulty', () => {
    render(<AiThinkingIndicator difficulty="ADVANCED" />)
    expect(screen.getByText(/AI.*上級.*考え中/)).toBeTruthy()
  })

  it('shows fallback when difficulty is null', () => {
    render(<AiThinkingIndicator difficulty={null} />)
    expect(screen.getByText(/AI.*\?.*考え中/)).toBeTruthy()
  })

  it('shows difficulty label as-is when unknown difficulty string', () => {
    render(<AiThinkingIndicator difficulty="EXPERT" />)
    expect(screen.getByText(/EXPERT.*考え中/)).toBeTruthy()
  })
})
