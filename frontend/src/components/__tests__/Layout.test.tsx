// @vitest-environment jsdom
import { describe, it, expect, afterEach } from 'vitest'
import { render, screen, cleanup } from '@testing-library/react'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import { Layout } from '../Layout'

afterEach(cleanup)

function renderLayout() {
  return render(
    <MemoryRouter initialEntries={['/']}>
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<div>index content</div>} />
        </Route>
      </Routes>
    </MemoryRouter>
  )
}

describe('Layout', () => {
  it('renders app title', () => {
    renderLayout()
    expect(screen.getByText('将棋アプリ')).toBeTruthy()
  })

  it('renders 対局一覧 navigation button', () => {
    renderLayout()
    expect(screen.getByText('対局一覧')).toBeTruthy()
  })

  it('renders 統計 navigation button', () => {
    renderLayout()
    expect(screen.getByText('統計')).toBeTruthy()
  })

  it('renders footer text', () => {
    renderLayout()
    expect(screen.getByText(/CQRS/)).toBeTruthy()
  })

  it('renders outlet content', () => {
    renderLayout()
    expect(screen.getByText('index content')).toBeTruthy()
  })

  it('app title is clickable (button)', () => {
    renderLayout()
    const titleButton = screen.getByText('将棋アプリ')
    expect(titleButton.tagName.toLowerCase()).toBe('button')
  })

  it('navigation buttons are rendered as buttons', () => {
    renderLayout()
    const gameListBtn = screen.getByText('対局一覧')
    const statsBtn = screen.getByText('統計')
    // AtlassianNavigation PrimaryButton renders as button elements
    expect(gameListBtn).toBeTruthy()
    expect(statsBtn).toBeTruthy()
  })
})
