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

  it('renders 対局一覧 navigation link', () => {
    renderLayout()
    expect(screen.getByText('対局一覧')).toBeTruthy()
  })

  it('renders 統計 navigation link', () => {
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

  it('logo links to /', () => {
    const { container } = renderLayout()
    const logoLink = container.querySelector('.logo')
    expect(logoLink?.getAttribute('href')).toBe('/')
  })

  it('nav links point to correct paths', () => {
    const { container } = renderLayout()
    const navLinks = container.querySelectorAll('.nav-link')
    const hrefs = Array.from(navLinks).map((l) => l.getAttribute('href'))
    expect(hrefs).toContain('/')
    expect(hrefs).toContain('/statistics')
  })
})
