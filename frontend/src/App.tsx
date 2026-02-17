import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { Layout } from './components/Layout'
import { GameListPage } from './pages/GameListPage'
import { GameDetailPage } from './pages/GameDetailPage'
import { GameReplayPage } from './pages/GameReplayPage'
import { StatisticsPage } from './pages/StatisticsPage'
import { CreateGamePage } from './pages/CreateGamePage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<GameListPage />} />
          <Route path="create" element={<CreateGamePage />} />
          <Route path="game/:gameId" element={<GameDetailPage />} />
          <Route path="replay/:gameId" element={<GameReplayPage />} />
          <Route path="statistics" element={<StatisticsPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
