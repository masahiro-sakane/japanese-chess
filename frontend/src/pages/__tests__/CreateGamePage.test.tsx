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

// Mock @atlaskit/form to pass through native form submission
// @atlaskit/form intercepts submit and calls onSubmit(formValues) instead of onSubmit(event),
// which breaks components that expect React.FormEvent. We mock it to render a native form.
vi.mock('@atlaskit/form', () => {
  const React = require('react')
  const Form = ({ onSubmit, children }: { onSubmit: (...args: unknown[]) => void; children: (props: { formProps: Record<string, unknown> }) => React.ReactNode }) => {
    const formProps = {
      onSubmit: (e: React.FormEvent) => {
        e.preventDefault()
        onSubmit(e)
      },
    }
    return React.createElement(React.Fragment, null, children({ formProps }))
  }
  const Field = ({ children, name, label }: { children: (props: { fieldProps: Record<string, unknown> }) => React.ReactNode; name: string; label: string }) => {
    return React.createElement(React.Fragment, null, children({ fieldProps: { id: name, name } }))
  }
  const FormSection = ({ children, title }: { children: React.ReactNode; title: string }) => {
    return React.createElement('div', null, React.createElement('h3', null, title), children)
  }
  const HelperMessage = ({ children }: { children: React.ReactNode }) => {
    return React.createElement('span', null, children)
  }
  return {
    __esModule: true,
    default: Form,
    Field,
    FormSection,
    HelperMessage,
  }
})

// Mock fetch for game polling
global.fetch = vi.fn()

function renderPage() {
  return render(
    <BrowserRouter>
      <CreateGamePage />
    </BrowserRouter>
  )
}

function getTextInputs(container: HTMLElement): HTMLInputElement[] {
  // Atlaskit TextField renders <input> without explicit type="text" attribute
  const allInputs = container.querySelectorAll<HTMLInputElement>('input')
  return Array.from(allInputs).filter(i => i.type === 'text' || i.type === '')
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
      const textInputs = getTextInputs(container)
      expect(textInputs.length).toBeGreaterThanOrEqual(1)
    })

    it('shows white player ID section in 対人戦 mode', () => {
      const { container } = renderPage()
      const textInputs = getTextInputs(container)
      // In 対人戦 mode: both black and white inputs visible
      expect(textInputs.length).toBe(2)
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
        const textInputs = getTextInputs(container)
        // Only black player ID input remains
        expect(textInputs.length).toBe(1)
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

      // Click the submit button instead of fireEvent.submit
      // @atlaskit/form intercepts native submit and manages its own flow
      const submitBtn = container.querySelector('button[type="submit"]')!
      fireEvent.click(submitBtn)

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

      const submitBtn = container.querySelector('button[type="submit"]')!
      fireEvent.click(submitBtn)

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/game/test-game-id')
      })
    })

    it('shows error when createGame fails', async () => {
      mockCreateGame.mockRejectedValue(new Error('サーバーエラー'))
      const { container } = renderPage()

      const submitBtn = container.querySelector('button[type="submit"]')!
      fireEvent.click(submitBtn)

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

      const submitBtn = container.querySelector('button[type="submit"]')!
      fireEvent.click(submitBtn)

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
      const textInputs = getTextInputs(container)
      const originalId = textInputs[0].value

      const buttons = container.querySelectorAll('button')
      const randomBtn = Array.from(buttons).find(b => b.textContent?.includes('ランダム生成'))!
      fireEvent.click(randomBtn)

      const updatedInputs = getTextInputs(container)
      expect(updatedInputs[0].value).not.toBe(originalId)
    })
  })
})
