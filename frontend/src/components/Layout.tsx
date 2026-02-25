import { useNavigate, useLocation, Outlet } from 'react-router-dom'
import {
  AtlassianNavigation,
  PrimaryButton,
} from '@atlaskit/atlassian-navigation'
import { token } from '@atlaskit/tokens'

function ShogiHome() {
  const navigate = useNavigate()
  return (
    <button
      onClick={() => navigate('/')}
      style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'white', fontSize: 18, fontWeight: 700, padding: '0 16px' }}
    >
      将棋アプリ
    </button>
  )
}

export function Layout() {
  const navigate = useNavigate()
  const location = useLocation()

  const isActive = (path: string) => location.pathname === path

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: '#F4F5F7' }}>
      <AtlassianNavigation
        label="将棋アプリ ナビゲーション"
        renderProductHome={ShogiHome}
        primaryItems={[
          <PrimaryButton
            key="games"
            isHighlighted={isActive('/')}
            onClick={() => navigate('/')}
          >
            対局一覧
          </PrimaryButton>,
          <PrimaryButton
            key="statistics"
            isHighlighted={isActive('/statistics')}
            onClick={() => navigate('/statistics')}
          >
            統計
          </PrimaryButton>,
        ]}
      />

      <main style={{ flex: 1, padding: token('space.300', '24px') }}>
        <div style={{ maxWidth: 1200, margin: '0 auto', width: '100%' }}>
          <Outlet />
        </div>
      </main>

      <footer style={{
        backgroundColor: token('color.background.neutral', '#DFE1E6'),
        padding: `${token('space.200', '16px')} ${token('space.300', '24px')}`,
        textAlign: 'center',
        color: token('color.text.subtle', '#6B778C'),
        fontSize: 14,
      }}>
        CQRS/イベントソーシングを用いた将棋アプリケーション
      </footer>
    </div>
  )
}
