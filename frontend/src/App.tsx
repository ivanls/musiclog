
import { Routes, Route } from 'react-router-dom'
import Header from './components/Header'
import ArtistDetails from './pages/ArtistDetails'
import AlbumDetails from './pages/AlbumDetails'
import TrackDetails from './pages/TrackDetails'
import Login from './pages/Login'
import Register from './pages/Register'
import RecentAlbums from './components/RecentAlbums'
import Home from './pages/Home'

function App() {
  return (
    <div>
      <Header title="MusicLog" />

      <Routes>
        <Route path="/" element={<Home />} />

        <Route path="/artists/:id" element={<ArtistDetails />} />
        <Route path="/albums/:id" element={<AlbumDetails />} />
        <Route path="/tracks/:id" element={<TrackDetails />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
      </Routes>
    </div>
  )
}

export default App
