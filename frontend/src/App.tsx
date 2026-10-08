import { Routes, Route } from 'react-router-dom'
import Header from './components/Header'
import SearchBar from './components/SearchBar'
import ArtistDetails from './pages/ArtistDetails'
import AlbumDetails from './pages/AlbumDetails'
import TrackDetails from './pages/TrackDetails'


function App() {
  return (
    <div>
      <Header title="MusicLog" />

      <Routes>
        <Route
          path="/"
          element={
            <>
              <p>Discover, review and track your music.</p>
              <SearchBar />
            </>
          }
        />

        <Route path="/artists/:id" element={<ArtistDetails />} />
        <Route path='/albums/:id' element={<AlbumDetails />} />
        <Route path='/tracks/:id' element={<TrackDetails />} />
      </Routes>
    </div>
  )
}

export default App