// @vitest-environment jsdom
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, fireEvent, waitFor, cleanup } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { CreateGamePage } from '../CreateGamePage'

afterEach(cleanup)

const mockNavigate = vi.fn()

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

const mockCreateGame = vi.fn()

vi.mock('../../services/gameService', () => ({
  gameService: {
    createGame: (...args: unknown[]) => mockCreateGame(...args),
  },
}))

// Mock fetch for game polling
global.fetch = vi.fn()

function renderPage() {
  return render(
    <BrowserRouter>
      <CreateGamePage />
    </BrowserRouter>
  )
}

describe('CreateGamePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    ;(global.fetch as ReturnType<typeof vi.fn>).mockResolvedValue({ ok: true })
  })

  describe('initial rendering', () => {
    it('renders page title', () => {
      const { container } = renderPage()
      expect(container.querySelector('h1')?.textContent).toBe('新規対局の作成')
    })

    it('renders game mode radio buttons', () => {
      const { container } = renderPage()
      const radios = container.querySelectorAll<HTMLInputElement>('input[type="radio"]')
      expect(radios).toHaveLength(2)
    })

    it('defaults to 対人戦 mode (first radio checked)', () => {
      const { container } = renderPage()
      const radios = container.querySelectorAll<HTMLInputElement>('input[type="radio"]')
      expect(radios[0].checked).toBe(true)
      expect(radios[1].checked).toBe(false)
    })

    it('renders black player ID input', () => {
      const { container } = renderPage()
      const inputs = container.querySelectorAll<HTMLInputElement>('input[type="text"]')
      // At least one text input for black player ID
      expect(inputs.length).toBeGreaterThanOrEqual(1)
    })

    it('shows white player ID section in 対人戦 mode', () => {
      const { container } = renderPage()
      const inputs = container.querySelectorAll<HTMLInputElement>('input[type="text"]')
      // In 対人戦 mode: both black and white inputs visible
      expect(inputs.length).toBe(2)
    })

    it('renders submit and cancel buttons', () => {
      const { container } = renderPage()
      const buttons = container.querySelectorAll('button')
      const buttonTexts = Array.from(buttons).map(b => b.textContent)
      expect(buttonTexts.some(t => t?.includes('対局を開始'))).toBe(true)
      expect(buttonTexts.some(t => t?.includes('キャンセル'))).toBe(true)
    })
  })

  describe('AI mode selection', () => {
    it('shows AI difficulty selector when AI mode selected', async () => {
      const { container } = renderPage()
      const radios = container.querySelectorAll<HTMLInputElement>('input[type="radio"]')
      fireEvent.click(radios[1]) // AI mode

      await waitFor(() => {
        expect(container.textContent).toContain('AI難易度')
      })
    })

    it('hides white player ID section when AI mode selected', async () => {
      const { container } = renderPage()
      const radios = container.querySelectorAll<HTMLInputElement>('input[type="radio"]')
      fireEvent.click(radios[1]) // AI mode

      await waitFor(() => {
        const inputs = container.querySelectorAll<HTMLInputElement>('input[type="text"]')
        // Only black player ID input remains
        expect(inputs.length).toBe(1)
      })
    })

    it('changes submit button text to "AIと対局を開始" in AI mode', async () => {
      const { container } = renderPage()
      const radios = container.querySelectorAll<HTMLInputElement>('input[type="radio"]')
      fireEvent.click(radios[1]) // AI mode

      await waitFor(() => {
        const submitBtn = container.querySelector('button[type="submit"]')
        expect(submitBtn?.textContent).toContain('AIと対局を開始')
      })
    })

    it('returns to 対人戦 mode when first radio clicked again', async () => {
      const { container } = renderPage()
      const radios = container.querySelectorAll<HTMLInputElement>('input[type="radio"]')
      fireEvent.click(radios[1]) // Switch to AI
      fireEvent.click(radios[0]) // Switch back

      await waitFor(() => {
        expect(container.textContent).not.toContain('AI難易度')
        const submitBtn = container.querySelector('button[type="submit"]')
        expect(submitBtn?.textContent).toContain('対局を開始')
      })
    })
  })

  describe('form submission - 対人戦', () => {
    it('calls createGame with aiGame=false in 対人戦 mode', async () => {
      mockCreateGame.mockResolvedValue({ gameId: 'test-game-id' })
      const { container } = renderPage()

      const form = container.querySelector('form')!
      fireEvent.submit(form)

      await waitFor(() => {
        expect(mockCreateGame).toHaveBeenCalledWith(
          expect.objectContaining({
            aiGame: false,
            aiDifficulty: undefined,
          })
        )
      })
    })

    it('navigates to game page on success', async () => {
      mockCreateGame.mockResolvedValue({ gameId: 'test-game-id' })
      const { container } = renderPage()

      fireEvent.submit(container.querySelector('form')!)

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/game/test-game-id')
      })
    })

    it('shows error when createGame fails', async () => {
      mockCreateGame.mockRejectedValue(new Error('サーバーエラー'))
      const { container } = renderPage()

      fireEvent.submit(container.querySelector('form')!)

      await waitFor(() => {
        expect(container.textContent).toContain('サーバーエラー')
      })
    })
  })

  describe('form submission - AI対戦', () => {
    it('calls createGame with aiGame=true and AI_PLAYER in AI mode', async () => {
      mockCreateGame.mockResolvedValue({ gameId: 'ai-game-id' })
      const { container } = renderPage()

      const radios = container.querySelectorAll<HTMLInputElement>('input[type="radio"]')
      fireEvent.click(radios[1]) // Switch to AI mode

      await waitFor(() => {
        expect(container.textContent).toContain('AI難易度')
      })

      fireEvent.submit(container.querySelector('form')!)

      await waitFor(() => {
        expect(mockCreateGame).toHaveBeenCalledWith(
          expect.objectContaining({
            whitePlayerId: 'AI_PLAYER',
            aiGame: true,
            aiDifficulty: 'BEGINNER',
          })
        )
      })
    })
  })

  describe('cancel button', () => {
    it('navigates to home when cancel clicked', () => {
      const { container } = renderPage()
      const buttons = container.querySelectorAll('button')
      const cancelBtn = Array.from(buttons).find(b => b.textContent?.includes('キャンセル'))!
      fireEvent.click(cancelBtn)
      expect(mockNavigate).toHaveBeenCalledWith('/')
    })
  })

  describe('random ID generation', () => {
    it('regenerates black player ID when ランダム生成 clicked', () => {
      const { container } = renderPage()
      const inputs = container.querySelectorAll<HTMLInputElement>('input[type="text"]')
      const originalId = inputs[0].value

      const buttons = container.querySelectorAll('button')
      const randomBtn = Array.from(buttons).find(b => b.textContent?.includes('ランダム生成'))!
      fireEvent.click(randomBtn)

      const newId = (container.querySelectorAll<HTMLInputElement>('input[type="text"]')[0]).value
      expect(newId).not.toBe(originalId)
    })
  })
})
