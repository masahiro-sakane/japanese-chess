import { Link, Outlet } from 'react-router-dom'

export function Layout() {
  return (
    <div className="layout">
      <header className="header">
        <div className="header-content">
          <Link to="/" className="logo">
            <h1>将棋アプリ</h1>
          </Link>
          <nav className="nav">
            <Link to="/" className="nav-link">
              対局一覧
            </Link>
            <Link to="/statistics" className="nav-link">
              統計
            </Link>
          </nav>
        </div>
      </header>

      <main className="main">
        <div className="container">
          <Outlet />
        </div>
      </main>

      <footer className="footer">
        <div className="footer-content">
          <p>CQRS/イベントソーシングを用いた将棋アプリケーション</p>
        </div>
      </footer>
    </div>
  )
}
