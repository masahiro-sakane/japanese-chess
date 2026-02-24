// @vitest-environment jsdom
import { describe, it, expect, vi, afterEach } from 'vitest'
import { render, fireEvent, cleanup } from '@testing-library/react'
import { AiDifficultySelector } from '../AiDifficultySelector'

afterEach(cleanup)

describe('AiDifficultySelector', () => {
  it('renders all difficulty options', () => {
    const { container } = render(
      <AiDifficultySelector difficulty="BEGINNER" onChange={vi.fn()} />
    )
    expect(container.textContent).toContain('初級')
    expect(container.textContent).toContain('中級')
    expect(container.textContent).toContain('上級')
  })

  it('shows BEGINNER as checked when difficulty=BEGINNER', () => {
    const { container } = render(
      <AiDifficultySelector difficulty="BEGINNER" onChange={vi.fn()} />
    )
    const radios = container.querySelectorAll<HTMLInputElement>('input[type="radio"]')
    const checked = Array.from(radios).filter(r => r.checked)
    expect(checked).toHaveLength(1)
    expect(checked[0].value).toBe('BEGINNER')
  })

  it('shows INTERMEDIATE as checked when difficulty=INTERMEDIATE', () => {
    const { container } = render(
      <AiDifficultySelector difficulty="INTERMEDIATE" onChange={vi.fn()} />
    )
    const radios = container.querySelectorAll<HTMLInputElement>('input[type="radio"]')
    const checked = Array.from(radios).filter(r => r.checked)
    expect(checked).toHaveLength(1)
    expect(checked[0].value).toBe('INTERMEDIATE')
  })

  it('shows ADVANCED as checked when difficulty=ADVANCED', () => {
    const { container } = render(
      <AiDifficultySelector difficulty="ADVANCED" onChange={vi.fn()} />
    )
    const radios = container.querySelectorAll<HTMLInputElement>('input[type="radio"]')
    const checked = Array.from(radios).filter(r => r.checked)
    expect(checked).toHaveLength(1)
    expect(checked[0].value).toBe('ADVANCED')
  })

  it('calls onChange when INTERMEDIATE selected', () => {
    const onChange = vi.fn()
    const { container } = render(
      <AiDifficultySelector difficulty="BEGINNER" onChange={onChange} />
    )
    const radios = container.querySelectorAll<HTMLInputElement>('input[type="radio"]')
    const intermediateRadio = Array.from(radios).find(r => r.value === 'INTERMEDIATE')!
    fireEvent.click(intermediateRadio)
    expect(onChange).toHaveBeenCalledWith('INTERMEDIATE')
  })

  it('calls onChange when ADVANCED selected', () => {
    const onChange = vi.fn()
    const { container } = render(
      <AiDifficultySelector difficulty="BEGINNER" onChange={onChange} />
    )
    const radios = container.querySelectorAll<HTMLInputElement>('input[type="radio"]')
    const advancedRadio = Array.from(radios).find(r => r.value === 'ADVANCED')!
    fireEvent.click(advancedRadio)
    expect(onChange).toHaveBeenCalledWith('ADVANCED')
  })

  it('disables all radios when disabled=true', () => {
    const { container } = render(
      <AiDifficultySelector difficulty="BEGINNER" onChange={vi.fn()} disabled={true} />
    )
    const radios = container.querySelectorAll<HTMLInputElement>('input[type="radio"]')
    expect(radios).toHaveLength(3)
    radios.forEach(radio => {
      expect(radio.disabled).toBe(true)
    })
  })

  it('enables all radios when disabled=false', () => {
    const { container } = render(
      <AiDifficultySelector difficulty="BEGINNER" onChange={vi.fn()} disabled={false} />
    )
    const radios = container.querySelectorAll<HTMLInputElement>('input[type="radio"]')
    radios.forEach(radio => {
      expect(radio.disabled).toBe(false)
    })
  })
})
